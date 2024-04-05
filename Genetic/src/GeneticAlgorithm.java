import javax.swing.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class GeneticAlgorithm {
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
    private final Semaphore semaphore;

    JFrame frame = new JFrame("Graph Display");
    GraphPanel renderer;



    public GeneticAlgorithm(Graph initialGraph, GraphPanel panel, int populationSize, Mode executionMode, int processorCount) {
        this.initialGraph = initialGraph;
        this.panel = panel;
        this.populationSize = populationSize;
        initialGraphPopulation(initialGraph); //[E] for some reason in sequental it copies the same graph over and over again in 1 generation, yes it should be the same but I use the constructor to move the nodes
        // hence it should be different fitness and not the same one
        //fitnessScoreEvaluate(population);
        this.executionMode = executionMode;
        this.processorCount = processorCount;
        this.executor = Executors.newFixedThreadPool(processorCount);
        semaphore = new Semaphore(0); /*at start maybe it should be 0, right then we should increase it to populationSize +1 for main with release*/


        frame.setSize(initialGraph.getW(),initialGraph.getH());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

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
    public void selection() {
        System.out.print("Selection phase");
        long now = System.currentTimeMillis();
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        Collections.reverse(population);
        ArrayList<Graph> selectedGraphs = new ArrayList<>();
        population.subList(0, (populationSize / 2)).forEach(graph ->selectedGraphs.add(new Graph(graph.getNodes(), graph.getEdges(), graph.getH(), graph.getW())));
        this.population = selectedGraphs;
        System.out.println(" completed in: " + (System.currentTimeMillis()-now));
    }


    public void crossover() {
        Graph parent1;
        Graph parent2;
        ArrayList<Graph> children = new ArrayList<>();
        System.out.print("Cross-over phase");
        long now = System.currentTimeMillis();

        for (int i = 0; i < population.size(); i += 2) {
            //System.out.println("parent1: " + i + " parent2: " + (i + 1));
            parent1 = population.get(i);
            parent2 = population.get(i + 1);

            parent1.getNodes().sort(Comparator.comparingInt(Node::getId));
            parent2.getNodes().sort(Comparator.comparingInt(Node::getId));

            int separator = random.nextInt(parent2.getNodes().size()); //so we always have a bound
            // For nodes, use subList to avoid creating new objects
            ArrayList<Node> firstChildNodes = new ArrayList<>(parent2.getNodes().subList(0, separator + 1));
            firstChildNodes.addAll(parent1.getNodes().subList(separator + 1, parent1.getNodes().size()));

            ArrayList<Node> secondChildNodes = new ArrayList<>(parent1.getNodes().subList(0, separator + 1));
            secondChildNodes.addAll(parent2.getNodes().subList(separator + 1, parent2.getNodes().size()));

            // Create new Graph objects for the children
            children.add(new Graph(firstChildNodes,parent1.getEdges(), parent1.getH(), parent1.getW()));
            children.add(new Graph(secondChildNodes,parent1.getEdges(), parent1.getH(), parent1.getW()));

        /*
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
            children.add(new Graph(firstChildNodes, firstChildEdges, parent1.getH(), parent1.getW()));
            children.add(new Graph(secondChildNodes, secondChildEdges, parent1.getH(), parent1.getW()));


        }
        //first add them in arrayList and then add them to the population
        //so the loop is not messed up since the size of the population is changing
        population.addAll(children);

         */
        }
        population.addAll(children);
        System.out.println(" completed in: " + (System.currentTimeMillis() - now));
    }

    public void mutation(double mutationRate) {
        System.out.print("Mutation phase");
        long now = System.currentTimeMillis();
        for (Graph g : this.population) {
            double randomValue = new Random().nextDouble();
            if (randomValue <= mutationRate) {
                g.mutation();
            }
        }
        System.out.println(" completed in: " + (System.currentTimeMillis()-now));
    }

    public void calculateFitness(){
        System.out.print("Calculating fitness");
        long now = System.currentTimeMillis();
        // This assumes semaphore has enough permits to control task completion.
        semaphore.drainPermits(); //[E] I think this is the correct way to do it because if I want to reuse it i need to reset it right?

        for (Graph graph : this.population) {
            executor.submit(() -> {
                try {
                    graph.fitnessEvaluation(); // Perform fitness evaluation
                    //System.out.println("Evaluated graph with fitness: " + graph.getFitnessScore());
                } catch (Exception e) {
                    //System.err.println("Error during fitness evaluation: " + e.getMessage());
                } finally {
                    semaphore.release(); // Increase it
                }
            });
        }

        try {
            semaphore.acquire(populationSize); // [E] and now check if all of them finished
            //System.out.println("All evaluations have completed.");
            System.out.println(" completed in: " + (System.currentTimeMillis()-now));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            //System.err.println("Thread was interrupted while waiting for evaluations to complete: " + e.getMessage());
        }
    }

    public void compute()
    {
        long startTime = System.currentTimeMillis();
        while(iterations!=0)
        {
            calculateFitness();
            selection();
            crossover();
            mutation(MUTATION_PROBABILITY);
            //getBestGraph(this.population);
            //showBestGraph(getBestGraph(population));
            System.out.println("Generation best: " + getBestGraph(population).fitnessScore);
            iterations--;
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        //displayElapsedTime(elapsedTime);
        System.out.println("Total for gen: " +iterations + " took " +elapsedTime);

    }
    private void showBestGraph(Graph bestGraph) {
        SwingUtilities.invokeLater(() -> renderer.setGraph(bestGraph));
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
