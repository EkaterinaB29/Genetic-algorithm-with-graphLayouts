import mpi.MPI;
import mpi.MPIException;
import mpi.Status;

import java.io.IOException;
import java.util.ArrayList;

public class Worker {
    private GeneticAlgorithm geneticAlgorithm;

    public Worker(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }

    public void processData() throws MPIException, IOException, ClassNotFoundException {
        Status status = MPI.COMM_WORLD.Probe(0, MPI.ANY_TAG);
        int count = status.Get_count(MPI.BYTE);
        byte[] buffer = new byte[count];
        MPI.COMM_WORLD.Recv(buffer, 0, count, MPI.BYTE, 0, MPI.ANY_TAG);

        ArrayList<Graph> receivedPopulation = GeneticAlgorithm.deserializeSubPopulation(buffer);
        geneticAlgorithm.population = receivedPopulation;
        geneticAlgorithm.populationSize = receivedPopulation.size();
        geneticAlgorithm.calculateFitness();

        //TODO ASK IF WORKERS NEEDS TO DO GENETIC OPERATIONS AS WELL
        geneticAlgorithm.selection();
        geneticAlgorithm.crossoverOnePoint();
        geneticAlgorithm.mutation(GeneticAlgorithm.MUTATION_PROBABILITY);

        byte[] processedData = GeneticAlgorithm.serializeSubPopulation(geneticAlgorithm.population);
        MPI.COMM_WORLD.Send(processedData, 0, processedData.length, MPI.BYTE, 0, 0);
    }
}
