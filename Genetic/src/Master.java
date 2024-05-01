import mpi.MPI;
import mpi.MPIException;
import mpi.Status;

import java.io.IOException;
import java.util.ArrayList;

public class Master {
    public GeneticAlgorithm geneticAlgorithm;
    static final double MUTATION_PROBABILITY = 0.001;

    public Master(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }
   // Number of iterations for the genetic algorithm

    public void execute() throws MPIException, IOException, ClassNotFoundException {
        for (int currentIteration = 0; currentIteration < geneticAlgorithm.iterations; currentIteration++) {
            System.out.println("Iteration: " + currentIteration + " - Distributing work.");
            distributeWork();
            collectAndMergeResults();
            geneticAlgorithm.selection();
            geneticAlgorithm.crossover();
            geneticAlgorithm.mutation(GeneticAlgorithm.MUTATION_PROBABILITY);
            geneticAlgorithm.calculateFitness();
            System.out.println("End of iteration " + currentIteration + ". Best fitness: " + geneticAlgorithm.getBestGraph(geneticAlgorithm.population).getFitnessScore());
            MPI.COMM_WORLD.Barrier();

        }
        // After all iterations, process the best graphs for display or analysis
        geneticAlgorithm.animateGenerations();
    }

    public void distributeWork() throws IOException, MPIException {
        int size = MPI.COMM_WORLD.Size();
        int populationSize = geneticAlgorithm.populationSize;
        int chunkSize = populationSize / size;
        int remaining = populationSize % size;

        for (int i = 0; i < size; i++) {
            int startIndex = i * chunkSize + Math.min(i, remaining);
            int endIndex = startIndex + chunkSize + (i < remaining ? 1 : 0);
            ArrayList<Graph> subPopulation = new ArrayList<>(geneticAlgorithm.population.subList(startIndex, endIndex));
            byte[] serializedSubPopulation = GeneticAlgorithm.serializeSubPopulation(subPopulation);
            MPI.COMM_WORLD.Send(serializedSubPopulation, 0, serializedSubPopulation.length, MPI.BYTE, i, 0);
        }
    }

    public void collectAndMergeResults() throws MPIException, IOException, ClassNotFoundException {
        int size = MPI.COMM_WORLD.Size();
        geneticAlgorithm.generationSnapshots.add(geneticAlgorithm.getBestGraph(geneticAlgorithm.population));
        //geneticAlgorithm.population.clear(); // Clear current population to merge results

        for (int i = 0; i < size; i++) {
            Status status = MPI.COMM_WORLD.Probe(i, MPI.ANY_TAG);
            int messageSize = status.Get_count(MPI.BYTE);

            byte[] buffer = new byte[messageSize];
            MPI.COMM_WORLD.Recv(buffer, 0, messageSize, MPI.BYTE, i, MPI.ANY_TAG);
            ArrayList<Graph> subPopulation = GeneticAlgorithm.deserializeSubPopulation(buffer, messageSize);
            geneticAlgorithm.population.addAll(subPopulation);
        }
    }
}
