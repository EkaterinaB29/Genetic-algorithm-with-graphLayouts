import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class GraphPopulation {


    static Random random = new Random();

    ArrayList<Graph> population = new ArrayList<>();
    ArrayList<Double> fitness_values;

    public Graph initialGraph;
    public GraphPanel panel;

    public int populationSize;


    public GraphPopulation(Graph initialGraph,int populationSize, GraphPanel panel) {

        this.populationSize = populationSize;
         initialGraphPopulation(initialGraph);
         fitnessScoreEvaluate(population);

    }
    public GraphPopulation(ArrayList<Graph> selectedIndividuals, int populationSize, GraphPanel panel)
    {
        this.populationSize = populationSize;
        this.population = selectedIndividuals;
        fitnessScoreEvaluate(population); //

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

    public ArrayList<Double> fitnessScoreEvaluate(Collection<Graph> population) {

        fitness_values = new ArrayList<>();

        for (Graph graph : population) {
            fitness_values.add(graph.fitnessScore); // add the scores into arraylist
        }
        Collections.sort(fitness_values,Collections.reverseOrder()); //sort in dsc


        return fitness_values;
        //Chromosomes are then selected
        //to the genetic operations proportionally to the values so obtained.
    }



    public GraphPopulation selection() {
        ArrayList<Graph> selectedGraphs = new ArrayList<>();
        //sort the population based on fitness score
        Collections.sort(population, Comparator.comparingDouble(Graph::getFitnessScore));
        ArrayList<Graph> parentGraphs = (ArrayList<Graph>) population.stream()
                .limit(Math.min(10, population.size()))
                .collect(Collectors.toList());
        for (Graph g : parentGraphs) {
                // idea create copies of the best graphs to fill the bound for populationSize
                Graph copyGraph = new Graph(g.getNodes(),g.getEdges(),g.getH(),g.getW());
                // add the copies to ArrayList
                selectedGraphs.add(copyGraph);
            }
        GraphPopulation newGeneration = new GraphPopulation(selectedGraphs,populationSize,panel);
        return newGeneration;
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
        Graph parent1 = population.get(random.nextInt(population.size()));
        Graph parent2 =population.get(random.nextInt(population.size()));

        ArrayList<Node> nodesParent1= parent1.getNodes();
        ArrayList<Node[]> edgesParent1 = parent1.getEdges();
        ArrayList<Node> nodesParent2= parent2.getNodes();
        ArrayList<Node[]> edgesParent2 = parent2.getEdges();


        ArrayList<Node> childGraphNodes= new ArrayList<>();
        ArrayList<Node []> childGraphEdges= new ArrayList<>();


        int separator= random.nextInt(min(nodesParent1.size(),nodesParent2.size())); //so we always have a bound

        for (int i =0; i< nodesParent1.size(); i++) {
            if (i <= separator) {
                //childGraphNodes.add((Node) nodesParent1.subList(0, random.nextInt(nodesParent1.size())));
                childGraphNodes.add(nodesParent2.get(i));
            } else {
                //then copy of parent2
                childGraphNodes.add(nodesParent1.get(i));
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


        Graph childGraph = new Graph(childGraphNodes, childGraphEdges,parent1.getH(),parent1.getW());
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
        for (int i = 0; i <population.size() ; i++) {
            if (population.get(i).fitnessScore == f)
            {
                index=i;
            }
        }
        return population.get(index);
    }


}

