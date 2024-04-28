import mpi.MPI;

import java.io.IOException;

import static mpi.MPI.Init;

public class GeneticAlgorithmDistributed {

    private final GeneticAlgorithm geneticAlgorithm;

    public GeneticAlgorithmDistributed(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;

    }

    public void execute() {
        System.setProperty("mpj.np", "4");
         // Initialize the MPI environment
        int myRank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hello from " + myRank + " of " + size);
        
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