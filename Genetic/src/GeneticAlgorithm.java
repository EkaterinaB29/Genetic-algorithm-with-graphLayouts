import javax.swing.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class GeneticAlgorithm implements Runnable{
    //population parameters
    ArrayList<Graph> population = new ArrayList<>();
    private Graph initialGraph;
    private GraphPanel panel;
    private int populationSize;
    int iterations= 10;

    //synchronization tools & other parameters
    static Random random = new Random();
    static final double MUTATION_PROBABILITY = 0.001;
    private final Mode executionMode;
    int processorCount;
    private final ExecutorService executor;
    private final Semaphore semaphore; /*main thread can be faster since it needs populationSize +1 permits of the semaphore to proceed and may aquire them faster*/


    public GeneticAlgorithm(Graph initialGraph, GraphPanel panel, int populationSize, Mode executionMode, int processorCount) {
        this.initialGraph = initialGraph;
        this.panel = panel;
        this.populationSize = populationSize;
        initialGraphPopulation(initialGraph);
        //fitnessScoreEvaluate(population);
        this.executionMode = executionMode;
        this.processorCount = processorCount;
        this.executor = Executors.newFixedThreadPool(processorCount);
        semaphore = new Semaphore(0);
    }




    // function to create the population of graphs according to the first one
    private void initialGraphPopulation(Graph initialGraph) {
        for (int i = 0; i < populationSize; i++) {
            population.add(new Graph(initialGraph.nodes, initialGraph.edges, initialGraph.getH(), initialGraph.getW()));
        }
    }

    public void fitnessScoreEvaluate(ArrayList<Graph> population) {
       population.stream().forEach(graph -> graph.fitnessEvaluation());
       population.sort(Comparator.comparingDouble(Graph::getFitnessScore)); // todo redundant
    }
    public void  selection() {
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        Collections.reverse(population);
        ArrayList<Graph> selectedGraphs = new ArrayList<>();
        population.subList(0, population.size() / 2).forEach(graph ->selectedGraphs.add(new Graph(graph.getNodes(), graph.getEdges(), graph.getH(), graph.getW())));
        this.population = selectedGraphs;
    }


    public void crossover() {
        Graph parent1;
        Graph parent2;
        ArrayList<Graph> children = new ArrayList<>();

        for (int i = 0; i < population.size()/2; i += 2) {
            parent1 = population.get(i);
            parent2 = population.get(i + 1);

            parent1.getNodes().sort(Comparator.comparingInt(Node::getId));
            parent2.getNodes().sort(Comparator.comparingInt(Node::getId));
            ArrayList<Node> firstChildNodes = new ArrayList<>();
            ArrayList<Edge> firstChildEdges = new ArrayList<>();
            ArrayList<Node> secondChildNodes = new ArrayList<>();
            ArrayList<Edge> secondChildEdges = new ArrayList<>();
            int separator = random.nextInt(parent2.getNodes().size()); //so we always have a bound

            for (int j = 0; j < parent1.getNodes().size(); j++) {
                if (j <= separator) {
                    firstChildNodes.add(new Node(parent2.getNodes().get(j)));
                    secondChildNodes.add(new Node(parent1.getNodes().get(j)));
                } else {
                    //then copy of parent2
                    secondChildNodes.add(new Node(parent2.getNodes().get(j)));
                    firstChildNodes.add(new Node(parent1.getNodes().get(j)));
                }
            }
            parent2.getEdges().forEach(nodes -> {
                connectEdge(firstChildNodes, firstChildEdges, nodes);
                connectEdge(secondChildNodes, secondChildEdges, nodes);
            });
            population.add(new Graph(firstChildNodes, firstChildEdges, parent1.getH(), parent1.getW()));
            population.add(new Graph(secondChildNodes, secondChildEdges, parent1.getH(), parent1.getW()));
        }

    }

    private void connectEdge(ArrayList<Node> childNodes, ArrayList<Edge> firstChildEdges, Edge nodes) {
        Node firstOrigin = childNodes.stream().filter(node -> node.id == nodes.origin.id).toList().getFirst();
        Node firstDestination = childNodes.stream().filter(node -> node.id == nodes.destination.id).toList().getFirst();
        firstChildEdges.add(new Edge(firstOrigin, firstDestination));
    }

    public void mutation(double mutationRate) {
        for (Graph g : this.population) {
            double randomValue = new Random().nextDouble();
            if (randomValue <= mutationRate) {
                g.mutation();
            }
        }
    }
    @Override
    public void run() {
        // This assumes semaphore has enough permits to control task completion.
        final int totalTasks = populationSize+1;
        //semaphore.drainPermits(); // Ensures semaphore starts at zero available permits.

        for (Graph graph : this.population) {
            executor.submit(() -> {
                try {
                    graph.fitnessEvaluation(); // Perform fitness evaluation
                    System.out.println("Evaluated graph with fitness: " + graph.getFitnessScore());
                } catch (Exception e) {
                    System.err.println("Error during fitness evaluation: " + e.getMessage());
                } finally {
                    semaphore.release(); // Signal task completion.
                }
            });
        }

        try {
            semaphore.acquire(totalTasks);
            System.out.println("All evaluations have completed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread was interrupted while waiting for evaluations to complete: " + e.getMessage());
        }

    }


    public void compute()
    {
        long startTime = System.currentTimeMillis();
        while(iterations!=0)
        {
            new Thread(this).start();

            selection();
            crossover();
            mutation(MUTATION_PROBABILITY);
            getBestGraph(this.population);
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

    private Graph getBestGraph(ArrayList<Graph> population) {
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        return population.getLast();
    }
    private void displayElapsedTime(long elapsedTime) {
        JFrame frame = new JFrame();
        String message = String.format("Time: %d milliseconds", elapsedTime);
        JOptionPane.showMessageDialog(frame, message, "Computation Time", JOptionPane.INFORMATION_MESSAGE);
    }

}
