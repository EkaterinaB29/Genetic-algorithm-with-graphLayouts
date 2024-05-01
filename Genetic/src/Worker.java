import mpi.MPI;
import mpi.MPIException;
import mpi.Status;

import java.io.IOException;

public class Worker {
    private final GeneticAlgorithm geneticAlgorithm;

    public Worker(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }

    public void receivePopulationChunk() throws MPIException, IOException, ClassNotFoundException {
        System.out.println("Probing for incoming data from the master...");
        Status status = MPI.COMM_WORLD.Probe(0, MPI.ANY_TAG);
        int count = status.Get_count(MPI.BYTE);
        System.out.println("Expected data size: " + count + " bytes.");

        byte[] buffer = new byte[count];
        System.out.println("Receiving data... "+ count);
        MPI.COMM_WORLD.Recv(buffer, 0, count, MPI.BYTE, 0, MPI.ANY_TAG);
        System.out.println("Data received successfully.");

        System.out.println("Deserializing data...");
        geneticAlgorithm.population = GeneticAlgorithm.deserializeSubPopulation(buffer, count);
        geneticAlgorithm.populationSize = geneticAlgorithm.population.size();
        System.out.println("Data deserialized. Population size: " + geneticAlgorithm.populationSize + " graphs.");

        geneticAlgorithm.calculateFitness();
        System.out.println("Fitness calculated.");

       /* geneticAlgorithm.selection();
        System.out.println("Selection phase completed.");

        geneticAlgorithm.crossover();
        System.out.println("Crossover phase completed.");

        geneticAlgorithm.mutation(GeneticAlgorithm.MUTATION_PROBABILITY);
        System.out.println("Mutation phase completed.");
*/
        byte[] processedData = GeneticAlgorithm.serializeSubPopulation(geneticAlgorithm.population);
        System.out.println("Serialized processed data. Length: " + processedData.length + " bytes.");

        // Since the worker processes only send data back to the master after processing,
        // there's no need for a loop around MPI.Send here.
        System.out.println("Sending processed data back to master...");
        MPI.COMM_WORLD.Send(processedData, 0, processedData.length, MPI.BYTE, 0, 0);
        System.out.println("Processed data sent successfully.");
    }
}
