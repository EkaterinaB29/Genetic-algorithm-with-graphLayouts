import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class GraphPopulation {
    static Random random = new Random();
    ArrayList<Graph> population = new ArrayList<>();
    static final double MUTATION_PROBABILITY = 0.001;
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
            population.add(new Graph(initialGraph.nodes,initialGraph.edges, initialGraph.getH(), initialGraph.getW()));
            // mutation(1);
        }
    }

    public void fitnessScoreEvaluate(ArrayList<Graph> population) {
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
    }

    public GraphPopulation selection() {
        ArrayList<Graph> selectedGraphs = new ArrayList<>();
        //sort the population based on fitness score

        //List<Graph> parentGraphs = new ArrayList<>();
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        Collections.reverse(population);

        List<Graph> parentGraphs = new ArrayList<>(population.subList(0,population.size()/2));

        for (Graph g : parentGraphs) {
            // idea create copies of the best graphs to fill the bound for populationSize
            Graph copyGraph = new Graph(g.getNodes(),g.getEdges(),g.getH(),g.getW());
            // add the copies to ArrayList
            selectedGraphs.add(copyGraph);
        }
        return new GraphPopulation(selectedGraphs,populationSize,panel);
    }
    public Graph getInitialGraph() {
        return initialGraph;
    }

    public GraphPopulation addNewGraphs(ArrayList<Graph> children)
    {
        population.addAll(children);
        return this;
    }


    public ArrayList<Graph> combine() {
        Graph parent1;
        Graph parent2;
        ArrayList<Graph> children = new ArrayList<>();
        for (int i = 0; i < population.size(); i+=2) {
            parent1 = this.getPopulation().get(i);
            parent2 = this.getPopulation().get(i+1);
            ArrayList<Node> nodesParent1= parent1.getNodes();
            ArrayList<Node[]> edgesParent1 = parent1.getEdges();
            ArrayList<Node> nodesParent2= parent2.getNodes();
            ArrayList<Node[]> edgesParent2 = parent2.getEdges();

            nodesParent1.sort(Comparator.comparingInt(Node::getId));
            nodesParent2.sort(Comparator.comparingInt(Node::getId));
            ArrayList<Node> firstChildNodes= new ArrayList<>();
            ArrayList<Node []> firstChildEdges= new ArrayList<>();

            ArrayList<Node> secondChildNodes= new ArrayList<>();
            ArrayList<Node []> secondchildEdges= new ArrayList<>();


            int separator= random.nextInt(nodesParent2.size()); //so we always have a bound


            for (int j =0; j< nodesParent1.size(); j++) {
                if (j <= separator) {
                    firstChildNodes.add(new Node(nodesParent2.get(j)));
                    secondChildNodes.add(new Node(nodesParent1.get(j)));
                } else {
                    //then copy of parent2
                    secondChildNodes.add(new Node(nodesParent2.get(j)));
                    firstChildNodes.add(new Node(nodesParent1.get(j)));
                }
            }
            edgesParent2.forEach(nodes -> {
                Node firstOrigin = firstChildNodes.stream().filter(node -> node.id == nodes[0].id).toList().getFirst();
                Node firstDestination = firstChildNodes.stream().filter(node -> node.id == nodes[1].id).toList().getFirst();
                firstChildEdges.add(new Node[] {firstOrigin,firstDestination});

                Node secondOrigin = secondChildNodes.stream().filter(node -> node.id == nodes[0].id).toList().getFirst();
                Node secondDestination = secondChildNodes.stream().filter(node -> node.id == nodes[1].id).toList().getFirst();
                secondchildEdges.add(new Node[] {secondOrigin,secondDestination});
            });

            children.add(new Graph(firstChildNodes, firstChildEdges,parent1.getH(),parent1.getW()));
            children.add(new Graph(secondChildNodes, secondchildEdges,parent1.getH(),parent1.getW()));
        }
        return children;
    }



    public GraphPopulation mutation(double mutationRate)
    {
        for (Graph g:this.population) {
            double randomValue = new Random().nextDouble();

            if(randomValue <= mutationRate) {
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
