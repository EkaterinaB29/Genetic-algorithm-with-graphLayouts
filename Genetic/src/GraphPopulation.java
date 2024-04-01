import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

public class GraphPopulation implements Runnable {
    static Random random = new Random();
    ArrayList<Graph> population = new ArrayList<>();
    static final double MUTATION_PROBABILITY = 0.001;
    private Graph initialGraph;
    private final GraphPanel panel;
    private final int populationSize;
    int processorCount;
    private ExecutorService executor;
    //private  CyclicBarrier barrier;
    private Semaphore semaphore;
    private CountDownLatch latch;

    public GraphPopulation(Graph initialGraph, int populationSize, GraphPanel panel, int processorCount) {
        this.processorCount = processorCount;
        this.populationSize = populationSize;
        this.panel=panel;
        executor = Executors.newFixedThreadPool(processorCount);
       // barrier = new CyclicBarrier(populationSize + 1);
        initialGraphPopulation(initialGraph);
        //fitnessScoreEvaluate(population);
        semaphore = new Semaphore(populationSize + 1);
        latch = new CountDownLatch(populationSize+1);

    }

    public GraphPopulation(ArrayList<Graph> selectedIndividuals, int populationSize, GraphPanel panel, int processorCount) {
        this.population = selectedIndividuals;
        this.processorCount = processorCount;
        this.populationSize = populationSize;
        this.panel= panel;
        //barrier = new CyclicBarrier(populationSize + 1); //
        executor = Executors.newFixedThreadPool(processorCount);
        semaphore = new Semaphore(populationSize + 1);
        latch = new CountDownLatch(populationSize+1);
        //fitnessScoreEvaluate(population); //
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
    private void initialGraphPopulation(Graph initialGraph) {
        for (int i = 0; i < populationSize; i++) {
            population.add(new Graph(initialGraph.nodes, initialGraph.edges, initialGraph.getH(), initialGraph.getW()));
        }
    }

    public void fitnessScoreEvaluate(ArrayList<Graph> population) {
        for (Graph g : population) {
            g.fitnessEvaluation();
        }
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore)); // todo redundant
    }

    public GraphPopulation selection() {
        ArrayList<Graph> selectedGraphs = new ArrayList<>();
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        Collections.reverse(population);

        List<Graph> parentGraphs = new ArrayList<>(population.subList(0, population.size() / 2));

        for (Graph g : parentGraphs) {
            Graph copyGraph = new Graph(g.getNodes(), g.getEdges(), g.getH(), g.getW());
            selectedGraphs.add(copyGraph);
        }
        return new GraphPopulation(selectedGraphs, populationSize, panel, processorCount);
    }

    public Graph getInitialGraph() {
        return initialGraph;
    }

    public GraphPopulation addNewGraphs(ArrayList<Graph> children) {
        population.addAll(children);
        return this;
    }

    //two parents-graph reproduce and create 2 children
    public ArrayList<Graph> combine() {
        Graph parent1;
        Graph parent2;
        ArrayList<Graph> children = new ArrayList<>();


        for (int i = 0; i < population.size(); i += 2) {
            parent1 = this.getPopulation().get(i);
            parent2 = this.getPopulation().get(i + 1);
            ArrayList<Node> nodesParent1 = parent1.getNodes();
            ArrayList<Edge> edgesParent1 = parent1.getEdges();
            ArrayList<Node> nodesParent2 = parent2.getNodes();
            ArrayList<Edge> edgesParent2 = parent2.getEdges();
            nodesParent1.sort(Comparator.comparingInt(Node::getId));
            nodesParent2.sort(Comparator.comparingInt(Node::getId));
            ArrayList<Node> firstChildNodes = new ArrayList<>();
            ArrayList<Edge> firstChildEdges = new ArrayList<>();
            ArrayList<Node> secondChildNodes = new ArrayList<>();
            ArrayList<Edge> secondChildEdges = new ArrayList<>();
            int separator = random.nextInt(nodesParent2.size()); //so we always have a bound

            for (int j = 0; j < nodesParent1.size(); j++) {
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
                connectEdge(firstChildNodes, firstChildEdges, nodes);
                connectEdge(secondChildNodes, secondChildEdges, nodes);
            });
            children.add(new Graph(firstChildNodes, firstChildEdges, parent1.getH(), parent1.getW()));
            children.add(new Graph(secondChildNodes, secondChildEdges, parent1.getH(), parent1.getW()));
        }
        return children;
    }

    private void connectEdge(ArrayList<Node> firstChildNodes, ArrayList<Edge> firstChildEdges, Edge nodes) {
        Node firstOrigin = firstChildNodes.stream().filter(node -> node.id == nodes.origin.id).toList().getFirst();
        Node firstDestination = firstChildNodes.stream().filter(node -> node.id == nodes.destination.id).toList().getFirst();
        firstChildEdges.add(new Edge(firstOrigin, firstDestination));
    }


    public GraphPopulation mutation(double mutationRate) {
        for (Graph g : this.population) {
            double randomValue = new Random().nextDouble();

            if (randomValue <= mutationRate) {
                g.mutation();
            }
        }
        return this;

    }
    /**Not best approach
    @Override
    public void run() {
        try {
            //AtomicInteger successfulEvaluations = new AtomicInteger(0); // to count successful evaluations

            // Submit fitness evaluation tasks for each graph
            for (int i = 0; i < populationSize; i++) {
                Graph graph = population.get(i);
                int finalI = i;
                executor.submit(() -> {
                    try {
                        graph.fitnessEvaluation(); // Perform fitness evaluation
                        System.out.println("Fitness evaluated for graph at index:" + finalI);
                        //successfulEvaluations.incrementAndGet();
                    } catch (Exception e) {

                        System.err.println("Error evaluating fitness for graph at index:" + finalI);
                        e.printStackTrace();
                    }
                });
            }
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                executor.shutdownNow(); // Force shutdown if tasks did not complete in time
            }

          /*  if (successfulEvaluations.get() == populationSize) {
                System.out.println("All fitness evaluations completed successfully.");
            } else {
                System.out.println("Some fitness evaluations failed.");
            }
        } catch (InterruptedException e) {
            // Handle InterruptedException
            Thread.currentThread().interrupt(); // Reset the interrupted status
            throw new RuntimeException(e);
        }
    ****/


    /**some threads evaluate later main is faster than the last threads*/
    @Override
    /**
    public void run() {
        for (Graph graph : population) {
            new Thread(() -> {
                try {
                    graph.fitnessEvaluation();
                    System.out.println("Fitness evaluated for graph at index:" + population.indexOf(graph));
                } finally {
                    semaphore.release();
                }
            }).start();
        }
        try {
            semaphore.acquire(populationSize + 1);
            System.out.println("Waiting for all evaluations to complete...");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Proceed with further processing after all evaluations are complete
        System.out.println("All fitness evaluations completed successfully.");
        Collections.sort(population, Comparator.comparingDouble(Graph::getFitnessScore).reversed());

        // Further processing can go here
    }**/



    public void run() {
        CountDownLatch latch = new CountDownLatch(populationSize);

        for (Graph graph : population) {
            new Thread(() -> {
                try {
                    graph.fitnessEvaluation();
                    System.out.println("Fitness evaluated for graph at index:" + population.indexOf(graph));
                } finally {
                    latch.countDown(); // decrease
                }
            }).start();
        }

        try {
            latch.await(); //wait for 0
            System.out.println("Waiting for all evaluations to complete...");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("All fitness evaluations completed successfully.");
        Collections.sort(population, Comparator.comparingDouble(Graph::getFitnessScore).reversed());

    }

}








