import mpi.MPI;

import java.util.ArrayList;

public class Master {
    GeneticAlgorithm geneticAlgorithm;

    public Master(GeneticAlgorithm geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }

    public void distributeWork() {
        // Distribute work to the workers
        int populationSize = geneticAlgorithm.populationSize;
        int size = MPI.COMM_WORLD.Size(); //total number of processes in the communicator
        int myRank = MPI.COMM_WORLD.Rank(); //rank of the calling process in the channel;address in netw

        //Calculate the subPopulation size for each worker
        int chunkSize = populationSize / size;
        int remaining = populationSize % size;

        for (int i = 0; i < size; i++) {
            int startIndex = i * chunkSize + Math.min(i, remaining);
            int endIndex = startIndex + chunkSize;
            if (i < remaining) {
                endIndex++;
            }

            // Prepare the subpopulation for each worker
            ArrayList<Graph> subPopulation = (ArrayList<Graph>) geneticAlgorithm.population.subList(startIndex, endIndex);

            //If we are master we send
            if (myRank == 0) {
                if (i > 0) {
                    //send subpopulation to worker i
                    //need of serialization convert the object into a byte stream ?

                    MPI.COMM_WORLD.Send(subPopulation, 0, subPopulation.size(), MPI.OBJECT, i, 0);
                }
            } else if (myRank == i) {
                // Worker receives its subpopulation
                // need of deserialization convert the byte stream back into an object ?
                MPI.COMM_WORLD.Recv(subPopulation, 0, subPopulation.size(), MPI.OBJECT, 0, 0);
            }
        }

    }

    public void collectResults() {
        // Collect results from the workers
    }
}
