import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Computation {
    public  GraphPopulation population;
    //
    // to visualize after each iteration the graph with best fitness_score
    long t0 = System.currentTimeMillis();
    static int iterations= 20;


    public Computation(GraphPopulation population)
    {
        this.population=population;
    }
    public void compute()
    {

        do
        {
            population = population.selection();

            population.addNewGraphs(population.combine());
            population.mutation(GraphPopulation.MUTATION_PROBABILITY);

            ArrayList<Graph> best = population.getPopulation();
            Collections.sort(best, Comparator.comparingDouble(Graph::getFitnessScore));
            Collections.reverse(population.getPopulation());
            Graph bestGraph = best.getFirst();
            GraphPanel graphPanel = new GraphPanel(bestGraph);

            JFrame frame = new JFrame("Graph Display");
            frame.setSize(bestGraph.getW(),bestGraph.getH());
            frame.add(graphPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            iterations--;
        }while(iterations>0);


        long t = System.currentTimeMillis();
        long time = t-t0;
        JFrame f = new JFrame();
        JLabel tl = new JLabel("Time:" +time+"miliseconds");
        f.setSize(400,100);
        f.add(tl);
        f.setVisible(true);

    }
}
