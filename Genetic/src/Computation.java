import javax.swing.*;
import java.util.Collection;
import java.util.Random;

import static java.util.Collections.max;

public class Computation {
    public  GraphPopulation population;
    int memory;
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
                newGeneration.addNewGraph(newGeneration.combine());
                newGeneration.mutation();

            //need to represent each generation's best sollution based on fitness
            Graph best = newGeneration.getGraph(max(newGeneration.fitness_values));
            GraphPanel graphPanel = new GraphPanel(best);
            JFrame frame = new JFrame("Graph Display");
            frame.setSize(best.getH(),best.getW());
            frame.add(graphPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            iterations--;
        }while(iterations>0);

    }
}
