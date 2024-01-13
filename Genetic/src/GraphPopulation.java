import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class GraphPopulation {


    static Random random = new Random();

    ArrayList<Graph> population = new ArrayList<>();
    ArrayList<Double> fitness_values;
    ArrayList<Integer> constantValues = new ArrayList<>();
    public Graph initialGraph;
    public GraphPanel panel;
    public int bound = 1000;
    public int populationSize;


    public GraphPopulation(Graph initialGraph,int populationSize, GraphPanel panel) {

        this.populationSize = populationSize;
         initialGraphPopulation(initialGraph);
         fitnessScoreEvaluate(population);

    }


    public int getSize() {
        return populationSize;
    }


    public GraphPanel getPanel() {
        return panel;
    }

    // function to create the population of graphs according to the first one
    private  void initialGraphPopulation(Graph initialGraph) {
        for (int i = 0; i <  populationSize; i++) {
            population.add(new Graph(initialGraph.numEdges, initialGraph.numNodes, initialGraph.getH(), initialGraph.getW()));
        }

    }

    public ArrayList<double[]> fitnessScoreEvaluate(Collection<Graph> population) {

        fitness_values = new ArrayList<>();
        ArrayList<double[]> rank_values = new ArrayList<>(); //pairs of the rank and the fitness score
        // iterate trough the population
        for (Graph graph : population) {
            fitness_values.add(graph.fitnessScore); // add the scores into arraylist
        }
        Collections.sort(fitness_values,Collections.reverseOrder()); //sort in dsc


        for (Graph graph : population) {
           // double normalizedValue = linearNormalize(graph.fitnessScore,fitness_values);
            graph.setRank( constantValue,graph.fitnessScore); //
            //IT SAYS THE BEST CHROMOSOME GETS A CONSTANT
            constantValue-=2;
            rank_values.add(graph.getRank());

        }
        return rank_values;
        //Chromosomes are then selected
        //to the genetic operations proportionally to the values so obtained.
    }
    public  int constantValue = 1000;

    //linear Scaling. x ′ = ( x − x m i n ) / ( x m a x − x m i n ).
    /*private double linearNormalize(double fitnessScore, ArrayList<Double> sortedFitnessValues) {
        //linear normalization formula
        double minValue = sortedFitnessValues.get(sortedFitnessValues.size() - 1);
        double maxValue = sortedFitnessValues.get(0);
        double normalizedValue = ((fitnessScore - minValue)) / (maxValue - minValue);  //like this?
        return normalizedValue;
    }*/
    /*[E] Is it more correct  for this method to return Collection<Graph> or this as in GraphPopulation*/
    public GraphPopulation selection() {

        for (Graph g : population) {
            //traverse the population
            //and check for each the fitness_score
            //implement a way to select the individuals
            //tournament select since it can be parallelized or rank-based?
            if (g.rank[0] < 40) { //TODO WHAT VALUE SHOULD BE SELECTED , WHAT TO USE FOR THRESHOLD? -> they said that it shouln't be a big values as 1/2
                population.remove(g);
            }

        }
        return this;

    }
    public Graph getInitialGraph() {
        return initialGraph;
    }

    public GraphPopulation addNewGraph(Graph g)
    {
        population.add(g);
        return this;
    }


    public Graph combine() {
        //Why it doesn't want to resolve
        Graph parent1 = population.get(random.nextInt(populationSize));
        Graph parent2 =population.get(random.nextInt(populationSize));

        ArrayList<Node> nodesParent1= parent1.getNodes();
        ArrayList<Node[]> edgesParent1 = parent1.getEdges();
        ArrayList<Node> nodesParent2= parent2.getNodes();
        ArrayList<Node[]> edgesParent2 = parent2.getEdges();


        ArrayList<Node> childGraphNodes= new ArrayList<>();
        ArrayList<Node []> childGraphEdges= new ArrayList<>();
        //childGraphNodes.addAll(0, nodesParent1);

        int separator= random.nextInt(min(nodesParent1.size(),nodesParent2.size())); //so we always have a bound

        for (int i =0; i<= nodesParent1.size(); i++) {


            if (i <= separator) {
                //childGraphNodes.add((Node) nodesParent1.subList(0, random.nextInt(nodesParent1.size())));
                //will this work ?
                childGraphNodes.add(nodesParent2.get(i));
            } else {
                //then copy of parent2
                childGraphNodes.add(nodesParent2.get(i));
            }
        }

                // Traverse the edges from the first parent
               for (Node[] edge1 : edgesParent1) {
                   // if they exists  check  if they are connected
                   if (childGraphNodes.contains(edge1[0]) && childGraphNodes.contains(edge1[1])) {

                       childGraphEdges.add(edge1);
                   }
               }
               //now the same for the second parent
                       for (Node[] edge2 : edgesParent2) {
                           if (childGraphNodes.contains(edge2[0]) && childGraphNodes.contains(edge2[1]))
                           {
                               childGraphEdges.add(edge2);
                           }
                       }


        Graph childGraph = new Graph(childGraphNodes, childGraphEdges,initialGraph.getH(),initialGraph.getW());
       return childGraph;
    }

    private static final double MUTATION_PROBABILITY = 0.001;

    public GraphPopulation mutation()
    {
        for (Graph g:this.population) {
            double randomValue = new Random().nextDouble();
            if(randomValue <= MUTATION_PROBABILITY) {
                g.mutation();
            }
        }
        return this;

    }
    public Graph getGraph(double f)
    {
        int index = 0;
        for (int i = 0; i <populationSize ; i++) {
            if (population.get(i).fitnessScore == f)
            {
                index=i;
            }
        }
        return population.get(index);
    }




    //mutate edge is not neded initially // save for later testing may be useful
    // public Collection<Graph> mutateEdge() {
    //    Collection<Graph> m = selection();
    // for (Graph mutant : m) {
    //        // get a random edge and move it to a random location
    //        Node[] edge = mutant.getEdges().get(random.nextInt(mutant.getEdges().size()));
    //        edge[1] = Node.moveNode(edge[0], panel.getWidth(), panel.getHeight()); // also switch nodes ;)
    //        edge[0] = Node.moveNode(edge[1], panel.getWidth(), panel.getHeight());
    //
    //    }
    //    return m; //return the mutated population
    //}


}

