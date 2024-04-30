import mpi.*;

import java.io.IOException;
import java.util.ArrayList;

public class Worker {
    private ArrayList<Graph> subPopulation;
    private GeneticAlgorithm geneticAlgorithm;

    public Worker(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;

    }

    public void receivePopulationChunk() throws MPIException, IOException, ClassNotFoundException {

    }

    public void calculateFitnessForSubset() {
        for (Graph graph : this.subPopulation) {
            graph.fitnessEvaluation();
        }
    }


}
