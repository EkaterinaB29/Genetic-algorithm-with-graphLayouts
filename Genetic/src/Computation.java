import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Computation {
    public  GraphPopulation population;
    //
    // to visualize after each iteration the graph with best fitness_score
    long t0 = System.currentTimeMillis();
    static int iterations= 10;


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
            best.sort(Comparator.comparingDouble(Graph::getFitnessScore));
            Collections.reverse(population.getPopulation());
            Graph bestGraph = best.getFirst();
            GraphPanel graphPanel = new GraphPanel(bestGraph);

            JFrame frame = new JFrame("Graph Display");
            frame.setSize(bestGraph.getW(),bestGraph.getH());
            frame.add(graphPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            /*ArrayList<Graph> best = population.getPopulation();
            // Collections.sort(best, Comparator.comparingDouble(Graph::getFitnessScore));
            // Collections.reverse(population.getPopulation());
            // Graph bestGraph = best.get(best.size() - 1);
           for (int i = 0; i < best.size(); i++) {
                GraphPanel graphPanel = new GraphPanel(best.get(i));
                JFrame frame = new JFrame("Graph Display");
                frame.setSize(best.get(i).getW(), best.get(i).getH());
                frame.add(graphPanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);

            }*/

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
