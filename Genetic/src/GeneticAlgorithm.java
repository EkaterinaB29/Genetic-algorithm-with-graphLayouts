import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class GeneticAlgorithm implements Serializable {
    //population parameters
    ArrayList<Graph> population = new ArrayList<>();
    private Graph initialGraph;
    public final int populationSize;
    int iterations = 1;
    int generation = 0;

    //synchronization tools & other parameters
    static Random random = new Random();
    static final double MUTATION_PROBABILITY = 0.001;
    int processorCount;
    transient private ExecutorService executor;
    transient private Semaphore semaphore;
    ArrayList<Graph> generationSnapshots = new ArrayList<>();
    GraphPanel renderer;

    public GeneticAlgorithm(Graph initialGraph, int populationSize, int processorCount) {
        this.initialGraph = initialGraph;
        this.populationSize = populationSize;
        initialGraphPopulation(initialGraph);
        this.renderer = new GraphPanel(initialGraph);
        this.processorCount = processorCount;


    }

    private void initializeTransientFields(int processorCount) {
        // Initialize the ExecutorService and Semaphore here
        this.executor = Executors.newFixedThreadPool(processorCount);
        this.semaphore = new Semaphore(0);
    }

    // function to create the population of graphs according to the first one
    private void initialGraphPopulation(Graph initialGraph) {
        for (int i = 0; i < populationSize; i++) {
            this.population.add(new Graph(initialGraph.nodes, initialGraph.edges, initialGraph.getH(), initialGraph.getW()));
        }
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Deserialization
        this.executor = Executors.newFixedThreadPool(processorCount);
        this.semaphore = new Semaphore(0);
    }

    public void selection() {
        System.out.print("Selection phase");
        long now = System.currentTimeMillis();
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        Collections.reverse(population);
        ArrayList<Graph> selectedGraphs = new ArrayList<>();
        population.subList(0, (populationSize / 2)).forEach(graph -> selectedGraphs.add(new Graph(graph.getNodes(), graph.getEdges(), graph.getH(), graph.getW())));
        this.population = selectedGraphs;
        System.out.println(" completed in: " + (System.currentTimeMillis() - now));
    }

    public void crossover() {
        Graph parent1;
        Graph parent2;
        ArrayList<Graph> children = new ArrayList<>();
        System.out.print("Cross-over phase");
        long now = System.currentTimeMillis();

        for (int i = 0; i < population.size(); i += 2) {
            parent1 = population.get(i);
            parent2 = population.get(i + 1);

            parent1.getNodes().sort(Comparator.comparingInt(Node::getId));
            parent2.getNodes().sort(Comparator.comparingInt(Node::getId));

            int separator = random.nextInt(parent2.getNodes().size()); //so we always have a bound
            ArrayList<Node> firstChildNodes = new ArrayList<>(parent2.getNodes().subList(0, separator + 1));
            firstChildNodes.addAll(parent1.getNodes().subList(separator + 1, parent1.getNodes().size()));

            ArrayList<Node> secondChildNodes = new ArrayList<>(parent1.getNodes().subList(0, separator + 1));
            secondChildNodes.addAll(parent2.getNodes().subList(separator + 1, parent2.getNodes().size()));

            children.add(new Graph(firstChildNodes, parent1.getEdges(), parent1.getH(), parent1.getW()));
            children.add(new Graph(secondChildNodes, parent1.getEdges(), parent1.getH(), parent1.getW()));
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
        System.out.println(" completed in: " + (System.currentTimeMillis() - now));
    }

    public void calculateFitness() {
        System.out.print("Calculating fitness");
        long now = System.currentTimeMillis();
        initializeTransientFields(processorCount);
        semaphore.drainPermits(); //
        for (Graph graph : this.population) {
            executor.submit(() -> {
                try {
                    graph.fitnessEvaluation();// Perform fitness evaluation
                    System.out.println("Evaluated graph with fitness: " + graph.getFitnessScore());
                } catch (Exception e) {
                    //System.err.println("Error during fitness evaluation: " + e.getMessage());
                } finally {
                    semaphore.release(); // Increase it
                }
            });
        }
        try {
            semaphore.acquire(populationSize);
            System.out.println(" completed in: " + (System.currentTimeMillis() - now));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void compute() {
        long startTime = System.currentTimeMillis();
        calculateFitness();
        generationSnapshots.add(initialGraph);
        System.out.println("initial graph fitness: " + initialGraph.getFitnessScore());
        while (iterations != 0) {
            selection();
            crossover();
            mutation(MUTATION_PROBABILITY);
            calculateFitness();
            generationSnapshots.add(getBestGraph(population));
            System.out.println("Generation best: " + getBestGraph(this.population).getFitnessScore());
            iterations--;
            generation++;
        }
        shutdownAndAwaitTermination(executor);
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Total for gen: " + generation + " took " + elapsedTime);
        animateGenerations();

    }

    public void animateGenerations() {
        Timer timer = new Timer(1000, null);
        final int[] index = {0}; //AtomicInteger index = new AtomicInteger(0);
        JFrame frame = new JFrame("Graph Display");
        frame.add(renderer);
        timer.addActionListener(e -> {
            if (index[0] < generationSnapshots.size()) {
                showBestGraph(generationSnapshots.get(index[0])); //index.get()
                index[0]++;
                //index.incrementAndGet();
            } else {
                ((Timer) e.getSource()).stop(); // Stop the timer
            }
        });
        frame.setSize(initialGraph.getW(), initialGraph.getH());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        timer.start();
    }

    public void showBestGraph(Graph bestGraph) {
        SwingUtilities.invokeLater(() -> renderer.setGraph(bestGraph));
    }

    public Graph getBestGraph(ArrayList<Graph> population) {
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        return population.getLast();
    }

    private void shutdownAndAwaitTermination(ExecutorService executor) {
        executor.shutdown();
        try {
            // Wait for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate in the specified time.");
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Executor did not terminate II time.");
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static byte[] serializeSubPopulation(ArrayList<Graph> subPopulation) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(subPopulation);
        oos.flush();
        oos.close();
        return bos.toByteArray();
    }


    public static ArrayList<Graph> deserializeSubPopulation(byte[] serializedSubPopulation, int length) throws IOException, ClassNotFoundException {
        if (serializedSubPopulation.length == 0 || serializedSubPopulation.length != length) {
            throw new IOException("Invalid or corrupted data received for deserialization.");
        }

        ArrayList<Graph> subPopulation;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedSubPopulation);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            subPopulation = (ArrayList<Graph>) ois.readObject();
        } catch (StreamCorruptedException sce) {
            System.err.println("Failed to deserialize data: " + sce.getMessage());
            throw sce;
        }
        return subPopulation;
    }


    public Graph getInitialGraph() {
        return initialGraph;
    }
}
