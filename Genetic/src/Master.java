import mpi.MPI;
import mpi.MPIException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Master {
    private final GeneticAlgorithm geneticAlgorithm;
    static final double MUTATION_PROBABILITY = 0.001;

    public Master(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }

    public void distributeWork() throws IOException {
        int populationSize = geneticAlgorithm.populationSize;
        int size = MPI.COMM_WORLD.Size(); // Total number of processes in the communicator, including master

        // Calculate the subpopulation size for each worker including the master
        int chunkSize = populationSize / size;
        int remaining = populationSize % size;

        // The master node will also compute, so it needs its own subpopulation chunk
        ArrayList<Graph> masterSubPopulation = new ArrayList<>(geneticAlgorithm.population.subList(0, chunkSize + (remaining > 0 ? 1 : 0)));
        geneticAlgorithm.population = masterSubPopulation;

        ArrayList<Graph> subPopulation = null;
        for (int i = 1; i < size; i++) { // Start from 1 since the master is included in computation
            int startIndex = i * chunkSize + Math.min(i, remaining);
            int endIndex = startIndex + chunkSize + (i < remaining ? 1 : 0);

            subPopulation = new ArrayList<>(geneticAlgorithm.population.subList(startIndex, endIndex));

            //Serialize subpopulation
            byte[] serializedSubPopulation = serializeSubPopulation(subPopulation);

            // Send subpopulation to worker i
            MPI.COMM_WORLD.Send(serializedSubPopulation, 0, serializedSubPopulation.length, MPI.BYTE, i, 0);
        }

        geneticAlgorithm.calculateFitness();
        geneticAlgorithm.selection();
        geneticAlgorithm.crossover();
        geneticAlgorithm.mutation(MUTATION_PROBABILITY);
    }

    // Serialization helper method remains the same
    private byte[] serializeSubPopulation(List<Graph> subPopulation) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(subPopulation);
            return bos.toByteArray();
        }
    }

}
