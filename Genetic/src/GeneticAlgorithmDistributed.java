import mpi.MPI;

import java.io.IOException;

import static mpi.MPI.Init;

public class GeneticAlgorithmDistributed {

    private final GeneticAlgorithm geneticAlgorithm;

    public GeneticAlgorithmDistributed(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;

    }

    public void execute(String[] args) {
        System.setProperty("mpj.np", "4");
        MPI.Init(args); // Initialize the MPI environment
        int myRank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hello from " + myRank + " of " + size);
        System.out.println("arguments:"+args[0] +args[1]+args[2]+args[3]);
        if (myRank == 0) {
            // This is the master process
            Master master = new Master(geneticAlgorithm);
            master.distributeWork();
            //master.collectResults();
            // Other master-related tasks
        } else {
            // This is a worker process
            Worker worker = new Worker(geneticAlgorithm);
            try {
                worker.receivePopulationChunk();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //worker.calculateFitnessForSubset();

        }


        MPI.Finalize(); // Clean the environment
    }

}