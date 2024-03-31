import javax.swing.*;
import java.util.Comparator;
import java.util.List;

public class Computation {
    public  GraphPopulation population;
    int iterations= 10;

    public Computation(GraphPopulation population)
    {
        this.population=population;
    }
    public void compute()
    {
        long startTime = System.currentTimeMillis();
        while(iterations!=0)
        {
            population = population.selection();
            population.addNewGraphs(population.combine());
            population.mutation(GraphPopulation.MUTATION_PROBABILITY);
            getBestGraph(population);
            showBestGraph(getBestGraph(population));
            iterations--;
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        displayElapsedTime(elapsedTime);

    }
    private void showBestGraph(Graph bestGraph) {
        JFrame frame = new JFrame("Graph Display");
        frame.setSize(bestGraph.getW(),bestGraph.getH());
        frame.add(new GraphPanel(bestGraph));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private Graph getBestGraph(GraphPopulation population) {
        population.getPopulation().sort(Comparator.comparingDouble(Graph::getFitnessScore));
        return population.getPopulation().getLast();
    }
    private void displayElapsedTime(long elapsedTime) {
        JFrame frame = new JFrame();
        String message = String.format("Time: %d milliseconds", elapsedTime);
        JOptionPane.showMessageDialog(frame, message, "Computation Time", JOptionPane.INFORMATION_MESSAGE);
    }
}
