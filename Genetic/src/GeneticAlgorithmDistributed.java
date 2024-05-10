import mpi.MPI;

import java.io.*;

public class GeneticAlgorithmDistributed {
    public static final int GRAPH_BYTE_SIZE = (int) new File("graph.ser").length();

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int myRank = MPI.COMM_WORLD.Rank();
        System.out.println(args[args.length - 1]);
        System.out.println(args[args.length - 2]);
        System.out.println(args[args.length - 3]);

        // Deserialize the genetic algorithm data just once at the beginning.
        Graph intialGraph = deserializeGraph(args[args.length - 3]);


        int populationSize = Integer.parseInt(args[args.length - 2]);
        int processors = Integer.parseInt(args[args.length - 1]);
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(intialGraph, populationSize, processors);

        Master master = null;
        Worker worker = null;
        if (myRank == 0) {
            master = new Master(geneticAlgorithm);
        } else {
            worker = new Worker(geneticAlgorithm);
        }
        long startTime = (long) MPI.Wtime();

        for (int currentIteration = 0; currentIteration < geneticAlgorithm.iterations; currentIteration++) {
            long iterationStartTime = (long) MPI.Wtime(); // Start timing this iteration.

            if (myRank == 0) {
                System.out.println("Iteration: " + currentIteration + " - Master is distributing work.");
                master.distributeWork();
                master.collectAndMergeResults();
                master.chooseBest();
                master.geneticOperations();
                System.out.println("End of iteration " + currentIteration);
            } else {
                worker.processData();
            }

            MPI.COMM_WORLD.Barrier();  // Ensure all processes reach this point before finishing the iteration.
            double iterationEndTime = MPI.Wtime(); // End timing this iteration.

            /*if (myRank == 0) {
                //System.out.println("Time taken for iteration " + currentIteration + ": " + (iterationEndTime - iterationStartTime) + " seconds");
            }*/
        }
        double totalEndTime = MPI.Wtime();

        if (myRank == 0) {
            System.out.println("Total time taken: " + (totalEndTime - startTime) + " seconds");
            master.finalizeProcessing();
        }

        MPI.Finalize();
    }

    public static Graph deserializeGraph(String filePath) throws IOException, ClassNotFoundException {
            try (FileInputStream fileIn = new FileInputStream(filePath); ObjectInputStream in = new ObjectInputStream(fileIn)) {
                return (Graph) in.readObject();
            }
        }






}