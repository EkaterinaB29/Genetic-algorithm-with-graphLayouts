import mpi.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class GeneticAlgorithmDistributed {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int myRank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        GeneticAlgorithm geneticAlgorithm = deserializeData(args[args.length - 1]);

        if (myRank == 0) {
            Master master = new Master(geneticAlgorithm);
            for (int currentIteration = 0; currentIteration < geneticAlgorithm.iterations; currentIteration++) {
                System.out.println("Iteration: " + currentIteration + " - Master is distributing work.");
                master.distributeWork();
                master.collectAndMergeResults();
                master.chooseBest();
                master.geneticOperations();
                System.out.println("End of iteration " + currentIteration );
                MPI.COMM_WORLD.Barrier();
            }
            master.finalizeProcessing();
        } else {
            Worker worker = new Worker(geneticAlgorithm);
            for (int currentIteration = 0; currentIteration < geneticAlgorithm.iterations; currentIteration++) {
                worker.processData();
                MPI.COMM_WORLD.Barrier();
            }
        }

        MPI.Finalize();
    }

    public static GeneticAlgorithm deserializeData(String filePath) throws IOException, ClassNotFoundException {
        try (FileInputStream fileIn = new FileInputStream(filePath); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (GeneticAlgorithm) in.readObject();
        }
    }
}
