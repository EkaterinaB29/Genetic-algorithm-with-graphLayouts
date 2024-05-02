import mpi.*;

import java.io.IOException;
import java.util.ArrayList;

public class Master {
    private GeneticAlgorithm geneticAlgorithm;

    public Master(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }

    public void distributeWork() throws IOException, MPIException {
        int size = MPI.COMM_WORLD.Size();
        //System.out.println("Size: " + size);
        int populationSize = geneticAlgorithm.populationSize;
        //System.out.println("Population size: " + populationSize);
        int chunkSize = populationSize / size;
        //System.out.println("Chunk size: " + chunkSize);
        int remaining = populationSize % size;
        //System.out.println("Remaining: " + remaining);
        /*
        System.out.println("Distributing work...");
        System.out.println("Total population size: " + populationSize);
        System.out.println("Each process receives a base chunk size: " + chunkSize);
        System.out.println("Remaining population: " + remaining);
        */

        for (int i = 1; i < size; i++) {
            int startIndex = i * chunkSize + Math.min(i, remaining);
            int endIndex = startIndex + chunkSize + (i < remaining ? 1 : 0);
            ArrayList<Graph> subPopulation = new ArrayList<>(geneticAlgorithm.population.subList(startIndex, endIndex));
            byte[] serializedSubPopulation = GeneticAlgorithm.serializeSubPopulation(subPopulation);
            /*
            System.out.println("Sending to process " + i + ": start index = " + startIndex + ", end index = " + endIndex);
            System.out.println("Sub-population size for process " + i + ": " + subPopulation.size());
            System.out.println("Serialized sub-population length (bytes) for process " + i + ": " + serializedSubPopulation.length);
            */
            MPI.COMM_WORLD.Send(serializedSubPopulation, 0, serializedSubPopulation.length, MPI.BYTE, i, 0);
        }
        geneticAlgorithm.population = new ArrayList<>(geneticAlgorithm.population.subList(0, chunkSize + (remaining > 0 ? 1 : 0)));
        geneticAlgorithm.populationSize = geneticAlgorithm.population.size();
        geneticAlgorithm.calculateFitness();
    }

    public void collectAndMergeResults() throws MPIException, IOException, ClassNotFoundException {
        int size = MPI.COMM_WORLD.Size();
        ArrayList<Graph> masterPopulation = new ArrayList<>(geneticAlgorithm.population);
        geneticAlgorithm.population.clear();
        geneticAlgorithm.populationSize = 0;

        for (int i = 1; i < size; i++) {
            Status status = MPI.COMM_WORLD.Probe(i, MPI.ANY_TAG);
            int messageSize = status.Get_count(MPI.BYTE);
            byte[] buffer = new byte[messageSize];
            MPI.COMM_WORLD.Recv(buffer, 0, messageSize, MPI.BYTE, i, MPI.ANY_TAG);
            ArrayList<Graph> subPopulation = GeneticAlgorithm.deserializeSubPopulation(buffer);
            geneticAlgorithm.population.addAll(subPopulation);
        }
        geneticAlgorithm.population.addAll(masterPopulation);
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
