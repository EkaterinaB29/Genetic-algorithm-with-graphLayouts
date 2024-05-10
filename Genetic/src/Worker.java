import mpi.MPI;
import mpi.MPIException;
import java.io.IOException;
import java.util.ArrayList;

public class Worker {
    private GeneticAlgorithm geneticAlgorithm;

    public Worker(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }

    public void processData() throws MPIException, IOException, ClassNotFoundException {
        // Assuming each worker knows the size of data it will receive, which could be communicated by the master
        int size = MPI.COMM_WORLD.Size();

        int populationSize = geneticAlgorithm.populationSize / size; // Assuming equal distribution for simplicity
        byte[] buffer = new byte[populationSize * GeneticAlgorithmDistributed.GRAPH_BYTE_SIZE]; // Buffer size based on population chunk size and graph size


        int[] displs = null;
        MPI.COMM_WORLD.Scatterv(null, 0, null, displs,MPI.BYTE, buffer, 0, buffer.length, MPI.BYTE, 0);

        // Deserialize the received data into an ArrayList of Graph objects
        ArrayList<Graph> receivedPopulation = GeneticAlgorithm.deserializeSubPopulation(buffer);
        geneticAlgorithm.population = receivedPopulation;
        geneticAlgorithm.populationSize = receivedPopulation.size();
        geneticAlgorithm.calculateFitness();

        geneticAlgorithm.selection();
        geneticAlgorithm.crossoverOnePoint();
        geneticAlgorithm.mutation(GeneticAlgorithm.MUTATION_PROBABILITY);

        // Serialize the processed population for sending back to the master
        byte[] processedData = GeneticAlgorithm.serializeSubPopulation(geneticAlgorithm.population);

        // Send the processed data back to the master using Gather
        MPI.COMM_WORLD.Gatherv(processedData, 0, processedData.length, MPI.BYTE, null, 0, null, null, MPI.BYTE, 0);
    }
}