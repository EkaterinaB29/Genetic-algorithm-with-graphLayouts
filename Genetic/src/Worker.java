import java.util.List;

public class Worker {

    public Worker() {
        // Constructor for the worker
    }

    public void calculateFitnessForSubset(List<Graph> subPopulation) {
        // Calculate fitness for the subset of the population
        for (Graph graph : subPopulation) {
            graph.fitnessEvaluation(); // Perform fitness evaluation
        }
        // Send fitness results back to the master
        // (You will use MPI calls here to send data back)
    }

    // Other methods such as crossoverForSubset, mutationForSubset, etc.
}