import mpi.MPI;
import mpi.MPIException;
import mpi.Status;

import java.io.IOException;
import java.util.ArrayList;

public class Master {
    public GeneticAlgorithm geneticAlgorithm;
    static final double MUTATION_PROBABILITY = 0.001;

    public Master(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }
   // Number of iterations for the genetic algorithm

    public void execute() throws MPIException, IOException, ClassNotFoundException {
        for (int currentIteration = 0; currentIteration < geneticAlgorithm.iterations; currentIteration++) {
            System.out.println("Iteration: " + currentIteration + " - Distributing work.");
            distributeWork();
            collectAndMergeResults();
            /**/

        }
        // After all iterations, process the best graphs for display or analysis
        //geneticAlgorithm.animateGenerations();
    }

    public void distributeWork() throws IOException, MPIException {
        int size = MPI.COMM_WORLD.Size();
        int populationSize = geneticAlgorithm.populationSize;
        int chunkSize = populationSize / size;
        int remaining = populationSize % size;

        System.out.println("Distributing work...");
        System.out.println("Total population size: " + populationSize);
        System.out.println("Each process receives a base chunk size: " + chunkSize);
        System.out.println("Remaining population: " + remaining);

        for (int i = 1; i < size; i++) {
            int startIndex = i * chunkSize + Math.min(i, remaining);
            int endIndex = startIndex + chunkSize + (i < remaining ? 1 : 0);
            ArrayList<Graph> subPopulation = new ArrayList<>(geneticAlgorithm.population.subList(startIndex, endIndex));
            byte[] serializedSubPopulation = GeneticAlgorithm.serializeSubPopulation(subPopulation);

            // Debugging output before sending data
            System.out.println("Sending to process " + i + ": start index = " + startIndex + ", end index = " + endIndex);
            System.out.println("Sub-population size for process " + i + ": " + subPopulation.size());
            System.out.println("Serialized sub-population length (bytes) for process " + i + ": " + serializedSubPopulation.length);


            MPI.COMM_WORLD.Send(serializedSubPopulation, 0, serializedSubPopulation.length, MPI.BYTE, i, 0);
        }
        /*geneticAlgorithm.population = new ArrayList<>(geneticAlgorithm.population.subList(0, chunkSize + (remaining > 0 ? 1 : 0)));
        geneticAlgorithm.populationSize = geneticAlgorithm.population.size();
        geneticAlgorithm.calculateFitness();
        geneticAlgorithm.selection();
        geneticAlgorithm.crossover();
        geneticAlgorithm.mutation(GeneticAlgorithm.MUTATION_PROBABILITY);
*/
        //
    }

    public void collectAndMergeResults() throws MPIException, IOException, ClassNotFoundException {
        int size = MPI.COMM_WORLD.Size();
        geneticAlgorithm.generationSnapshots.add(geneticAlgorithm.getBestGraph(geneticAlgorithm.population));
        //geneticAlgorithm.population.clear(); // Clear current population to merge results

        for (int i = 1; i < size; i++) {
            Status status = MPI.COMM_WORLD.Probe(i, MPI.ANY_TAG);
            int messageSize = status.Get_count(MPI.BYTE);

            byte[] buffer = new byte[messageSize];
            MPI.COMM_WORLD.Recv(buffer, 0, messageSize, MPI.BYTE, i, MPI.ANY_TAG);
            ArrayList<Graph> subPopulation = GeneticAlgorithm.deserializeSubPopulation(buffer, messageSize);
            geneticAlgorithm.population.addAll(subPopulation);
        }
        MPI.COMM_WORLD.Barrier();
    }
}
