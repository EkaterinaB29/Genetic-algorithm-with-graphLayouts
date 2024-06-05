public interface GeneticOperations {

    void crossoverOnePoint();

    void mutation(double mutationRate);

    void selection();

    void calculateFitness();


}
