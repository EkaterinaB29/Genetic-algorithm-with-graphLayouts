import mpi.MPI;
import mpi.MPIException;

import java.io.IOException;
import java.util.ArrayList;

public class Master {
    private GeneticAlgorithm geneticAlgorithm;

    public Master(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }

    public void distributeWork() throws IOException, MPIException, ClassNotFoundException {
        int populationSize = geneticAlgorithm.populationSize;
        int size = MPI.COMM_WORLD.Size();
        int chunkSize = populationSize / size;

        byte[][] populations = new byte[size][]; // Array to hold serialized subpopulations for each process
        // Scatter operation

        // Scatter operation
        int[] displs = new int[size];
        int[] sendCounts = new int[size];
        for (int i = 0; i < size; i++) {
            int startIndex = i * chunkSize;
            int endIndex = (i < size - 1) ? startIndex + chunkSize : populationSize;
            ArrayList<Graph> subPopulation = new ArrayList<>(geneticAlgorithm.population.subList(startIndex, endIndex));
            byte[] serializedSubPopulation = GeneticAlgorithm.serializeSubPopulation(subPopulation);
            populations[i] = serializedSubPopulation;
            sendCounts[i] = serializedSubPopulation.length; // Size of data to be sent to each process
            displs[i] = i * serializedSubPopulation.length; // Calculate displacement for each process
        }
        byte[] localPopulation = new byte[populations[0].length];

        MPI.COMM_WORLD.Scatterv(populations, 0, sendCounts, displs, MPI.OBJECT, localPopulation, 0, geneticAlgorithm.populationSize, MPI.OBJECT, 0);



        geneticAlgorithm.population = GeneticAlgorithm.deserializeSubPopulation(localPopulation);
        geneticAlgorithm.populationSize = geneticAlgorithm.population.size();
        geneticAlgorithm.calculateFitness();
    }

    public void collectAndMergeResults() throws MPIException, IOException, ClassNotFoundException {
        int size = MPI.COMM_WORLD.Size();
        byte[][] receivedPopulations = new byte[size][]; // Buffer to collect all subpopulations
        byte[] serializedPopulation = GeneticAlgorithm.serializeSubPopulation(geneticAlgorithm.population);

        // Gather operation
        MPI.COMM_WORLD.Gatherv(serializedPopulation, 0, serializedPopulation.length, MPI.BYTE, receivedPopulations, 0, new int[]{receivedPopulations[0].length, receivedPopulations[1].length,receivedPopulations[2].length,receivedPopulations[3].length}, null,  MPI.BYTE, 0);

        geneticAlgorithm.population.clear();
        geneticAlgorithm.populationSize = 0;

        for (byte[] populationData : receivedPopulations) {
            ArrayList<Graph> subPopulation = GeneticAlgorithm.deserializeSubPopulation(populationData);
            geneticAlgorithm.population.addAll(subPopulation);
        }
        geneticAlgorithm.populationSize = geneticAlgorithm.population.size();
    }

    public void geneticOperations() throws MPIException, IOException, ClassNotFoundException {
        geneticAlgorithm.selection();
        geneticAlgorithm.crossoverOnePoint();
        geneticAlgorithm.mutation(GeneticAlgorithm.MUTATION_PROBABILITY);
    }

    public void chooseBest() {
        geneticAlgorithm.generationSnapshots.add(geneticAlgorithm.getBestGraph(geneticAlgorithm.population));
    }

    public void finalizeProcessing() {
        geneticAlgorithm.animateGenerations();
    }

}