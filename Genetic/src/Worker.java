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
        Status status = MPI.COMM_WORLD.Probe(0, MPI.ANY_TAG);
        int count = status.Get_count(MPI.BYTE);


        byte[] buffer = new byte[count];
        MPI.COMM_WORLD.Recv(buffer, 0, count, MPI.BYTE, 0, MPI.ANY_TAG);

        geneticAlgorithm.population = GeneticAlgorithm.deserializeSubPopulation(buffer,count);
        geneticAlgorithm.calculateFitness();
        geneticAlgorithm.selection();
        geneticAlgorithm.crossover();
        geneticAlgorithm.mutation(GeneticAlgorithm.MUTATION_PROBABILITY);


        byte[] processedData = GeneticAlgorithm.serializeSubPopulation(geneticAlgorithm.population);
        MPI.COMM_WORLD.Send(processedData, 0, processedData.length, MPI.BYTE, 0, 0);
    }




}
