import mpi.*;

import java.util.ArrayList;

public class Worker {
    private ArrayList<Graph> subPopulation;
    private final GeneticAlgorithm ga;

    public Worker(GeneticAlgorithm ga) {
        this.ga = ga;

    }

    public void receivePopulationChunk() throws MPIException {

        int myRank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        //needs further work , if the population is one object or each graph should be sent separately

        Object[] receiveBuffer = new Object[1]; // If sending one object at a time

        //get the population chunk from the master
        MPI.COMM_WORLD.Recv(receiveBuffer, 0, 1, MPI.OBJECT, 0, 0);

        //convert the received data back to an ArrayList<Graph>
        this.subPopulation = (ArrayList<Graph>) receiveBuffer[0];

        // Now that the subPopulation has been received, we can perform operations on it
        calculateFitnessForSubset();
    }

    public void calculateFitnessForSubset() {
        for (Graph graph : this.subPopulation) {
            graph.fitnessEvaluation();
        }
    }
}
