import mpi.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class Worker {
    private final GeneticAlgorithm geneticAlgorithm;

    public Worker(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;

    }

    public void receivePopulationChunk() throws MPIException, IOException, ClassNotFoundException {
        // Receive the serialized subpopulation from the master
        byte[] serializedSubPopulation = new byte[1000];
        MPI.COMM_WORLD.Recv(serializedSubPopulation, 0, 1000, MPI.BYTE, 0, 0);

        // Deserialize the subpopulation
        geneticAlgorithm.population = deserializeSubPopulation(serializedSubPopulation);


        // Perform selection, crossover, and mutation
        geneticAlgorithm.calculateFitness();
        geneticAlgorithm.selection();
        geneticAlgorithm.crossover();
        geneticAlgorithm.mutation(GeneticAlgorithm.MUTATION_PROBABILITY);
    }

    public ArrayList<Graph> deserializeSubPopulation(byte[] serializedSubPopulation) throws IOException, ClassNotFoundException {
        ArrayList<Graph> subPopulation;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedSubPopulation); ObjectInputStream ois = new ObjectInputStream(bis)) {
            subPopulation = (ArrayList<Graph>) ois.readObject();
        }
        return subPopulation;
    }


}
