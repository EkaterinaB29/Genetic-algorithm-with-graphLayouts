import javax.swing.*;
import java.util.*;

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
            GraphPopulation newGeneration = population.selection();
            //we need to combine and add
            newGeneration.addNewGraphs(newGeneration.combine());
            //we need to mutatenewGeneration.mutation();
            newGeneration.mutation(GraphPopulation.MUTATION_PROBABILITY);
            //need to represent each generation's best sollution based on fitness
            // Assuming newGeneration.getPopulation() returns an ArrayList<Graph>
            ArrayList<Graph> best = newGeneration.getPopulation();

            Collections.sort(best, Comparator.comparingDouble(Graph::getFitnessScore));

            Graph bestGraph = best.getLast(); //if I put getLast() I get infinity scores and plus some of the nodes are without edges?
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
