import mpi.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class GeneticAlgorithmDistributed {

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        MPI.Init(args);
        int myRank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hello from process " + myRank + " of " + size);
        // Deserialize the object from the file
        GeneticAlgorithm geneticAlgorithm = deserializeData(args[args.length - 1]);
        if (myRank == 0) {
            // Master process
            Master master = new Master(geneticAlgorithm);
            master.execute();
        } else {
            // Worker process
            Worker worker = new Worker(geneticAlgorithm);
            try {
                worker.receivePopulationChunk();
            } catch (MPIException e) {
                e.printStackTrace();
            }

        }

        MPI.Finalize();
    }

    public static GeneticAlgorithm deserializeData(String filePath) throws IOException, ClassNotFoundException {
        GeneticAlgorithm geneticAlgorithm;
        try (FileInputStream fileIn = new FileInputStream(filePath); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            geneticAlgorithm = (GeneticAlgorithm) in.readObject();
        }
        return geneticAlgorithm;
    }

}
