import java.util.*;
import java.util.concurrent.*;

public class GraphPopulation implements Runnable{
    static Random random = new Random();
    ArrayList<Graph> population = new ArrayList<>();
    static final double MUTATION_PROBABILITY = 0.001;
    public Graph initialGraph;
    public GraphPanel panel;
    private final int populationSize;
    private final CyclicBarrier barrier;

   int processorCount;
    private  ExecutorService executor;

    public GraphPopulation(Graph initialGraph,int populationSize, GraphPanel panel, int processorCount) {
        this.processorCount = processorCount;
        this.populationSize = populationSize;
        this.barrier = new CyclicBarrier(populationSize+1 );
        executor = Executors.newFixedThreadPool(processorCount);

        initialGraphPopulation(initialGraph);
        //fitnessScoreEvaluate(population);

    }
    public GraphPopulation(ArrayList<Graph> selectedIndividuals, int populationSize, GraphPanel panel,int processorCount)
    {
        this.population = selectedIndividuals;
        this.processorCount = processorCount;
        this.populationSize = populationSize;
        this.barrier = new CyclicBarrier(populationSize+1 );
        executor = Executors.newFixedThreadPool(processorCount);

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
    private  void initialGraphPopulation(Graph initialGraph) {
        for (int i = 0; i <  populationSize; i++) {
            population.add(new Graph(initialGraph.nodes,initialGraph.edges, initialGraph.getH(), initialGraph.getW()));
        }
    }

    public void fitnessScoreEvaluate(ArrayList<Graph> population) {
        for (Graph g : population) {
            g.fitnessEvaluation();
        }
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
    }

    public GraphPopulation selection() {
        ArrayList<Graph> selectedGraphs = new ArrayList<>();
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        Collections.reverse(population);

        List<Graph> parentGraphs = new ArrayList<>(population.subList(0,population.size()/2));

        for (Graph g : parentGraphs) {
            Graph copyGraph = new Graph(g.getNodes(),g.getEdges(),g.getH(),g.getW());
            selectedGraphs.add(copyGraph);
        }
        return new GraphPopulation(selectedGraphs,populationSize,panel,processorCount);
    }
    public Graph getInitialGraph() {
        return initialGraph;
    }

    public GraphPopulation addNewGraphs(ArrayList<Graph> children)
    {
        population.addAll(children);
        return this;
    }

    /*two parents-graph reproduce and create 2 children*/
    public ArrayList<Graph> combine() {
        Graph parent1;
        Graph parent2;
        ArrayList<Graph> children = new ArrayList<>();
        for (int i = 0; i < population.size(); i+=2) {
            parent1 = this.getPopulation().get(i);
            parent2 = this.getPopulation().get(i+1);
            ArrayList<Node> nodesParent1= parent1.getNodes();
            ArrayList<Edge> edgesParent1 = parent1.getEdges();
            ArrayList<Node> nodesParent2= parent2.getNodes();
            ArrayList<Edge> edgesParent2 = parent2.getEdges();

            nodesParent1.sort(Comparator.comparingInt(Node::getId));
            nodesParent2.sort(Comparator.comparingInt(Node::getId));

            ArrayList<Node> firstChildNodes= new ArrayList<>();
            ArrayList<Edge> firstChildEdges= new ArrayList<>();

            ArrayList<Node> secondChildNodes= new ArrayList<>();
            ArrayList<Edge> secondChildEdges= new ArrayList<>();


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
                connectEdge(firstChildNodes, firstChildEdges, nodes);
                connectEdge(secondChildNodes, secondChildEdges, nodes);
            });
            children.add(new Graph(firstChildNodes, firstChildEdges,parent1.getH(),parent1.getW()));
            children.add(new Graph(secondChildNodes, secondChildEdges,parent1.getH(),parent1.getW()));
        }
        return children;
    }

    private void connectEdge(ArrayList<Node> firstChildNodes, ArrayList<Edge> firstChildEdges, Edge nodes) {
        Node firstOrigin = firstChildNodes.stream().filter(node -> node.id == nodes.origin.id).toList().getFirst();
        Node firstDestination = firstChildNodes.stream().filter(node -> node.id == nodes.destination.id).toList().getFirst();
        firstChildEdges.add(new Edge(firstOrigin,firstDestination));
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


    @Override
    public void run() {
        try {
            // Submit tasks for fitness evaluation to the executor service
            for (Graph graph : this.population) {
                executor.submit(() -> {
                    System.out.println("Thread " + Thread.currentThread().getName() + " is evaluating fitness...");
                    graph.fitnessEvaluation(); // Perform the fitness evaluation
                    try {
                        barrier.await(); // Wait for all tasks to reach this point
                    } catch (InterruptedException | BrokenBarrierException e) {
                        Thread.currentThread().interrupt(); // Set the interrupt flag
                        System.err.println("Error waiting at the barrier: " + e.getMessage());
                    }
                });
            }

            // Wait for all submitted tasks to reach the barrier, including this run() execution
            barrier.await();

        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt(); // Set the interrupt flag
            System.err.println("Run method's thread was interrupted or broken barrier: " + e.getMessage());
        } finally {
            // Ensure all tasks are done before shutting down the executor
            System.out.println("Shutting down the executor service...");
            executor.shutdown();
            try {
                // Wait for currently executing tasks to complete
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // Forcefully shutdown if tasks did not complete in time
                }
            } catch (InterruptedException e) {
                executor.shutdownNow(); // Ensure shutdown if current thread is interrupted
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }
    }





}
