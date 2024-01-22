import java.util.*;

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

    public ArrayList<Graph> getPopulation() {
        return population;
    }
    public GraphPanel getPanel() {
        return panel;
    }

    // function to create the population of graphs according to the first one
    private  void initialGraphPopulation(Graph initialGraph) {
        for (int i = 0; i <  populationSize; i++) {
            population.add(new Graph(initialGraph.numNodes,initialGraph.numEdges, initialGraph.getH(), initialGraph.getW()));
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

        //List<Graph> parentGraphs = new ArrayList<>();
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        Collections.reverse(population); // I don't know which is the problem I think it is here

        List<Graph> parentGraphs = new ArrayList<>(population.subList(0, 10));

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
        Graph parent1;
        Graph parent2;

        /*ensure that always two different parents are chosen*/
        do {
            parent1 = this.getPopulation().get(random.nextInt(population.size()));
            parent2 =this.getPopulation().get(random.nextInt(population.size()));
        }
        while(parent1.equals(parent2));

        ArrayList<Node> nodesParent1= parent1.getNodes();
        ArrayList<Node[]> edgesParent1 = parent1.getEdges();
        ArrayList<Node> nodesParent2= parent2.getNodes();
        ArrayList<Node[]> edgesParent2 = parent2.getEdges();


        ArrayList<Node> childGraphNodes= new ArrayList<>();
        ArrayList<Node []> childGraphEdges= new ArrayList<>();


        int separator= random.nextInt(nodesParent2.size()); //so we always have a bound

        for (int i =0; i< nodesParent1.size(); i++) {
            if (i <= separator) {
                //childGraphNodes.add((Node) nodesParent1.subList(0, random.nextInt(nodesParent1.size())));
                childGraphNodes.add((nodesParent2.get(i)));
            } else {
                //then copy of parent2
                childGraphNodes.add((nodesParent1.get(i)));
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
                //Node[] edgeN={edge2[0],edge2[1]};
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
