import javax.swing.Timer;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeneticAlgorithm implements Serializable,GeneticOperations{
    //population parameters
    ArrayList<Graph> population = new ArrayList<>();
    private Graph initialGraph;
    public int populationSize;
    int iterations = 100;
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
        this.executor = Executors.newFixedThreadPool(processorCount);
        this.semaphore = new Semaphore(0);
    }

    // function to create the population of graphs according to the first one
    private void initialGraphPopulation(Graph initialGraph) {
        this.population.add(initialGraph);
        for (int i = 0; i < populationSize - 1; i++) {
            this.population.add(new Graph(initialGraph.nodes, initialGraph.edges, initialGraph.getH(), initialGraph.getW()));
        }
    }

    @Override
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

    @Override
    public void crossoverOnePoint() {
        System.out.print("ONE-Cross-over phase");
        long now = System.currentTimeMillis();
        int size = population.size();
        int limit = size - (size % 2);  // Ensuring we only process pairs

        List<Graph> children = IntStream.range(0, limit)
                .boxed() // box int primitives to Integer objects
                .parallel()
                .filter(i -> i % 2 == 0)
                .flatMap(i -> {
                    Graph parent1 = population.get(i);
                    Graph parent2 = population.get(i + 1);

                    int separator = random.nextInt(parent1.getNodes().size());

                    ArrayList<Node> firstChildNodes = new ArrayList<>(parent2.getNodes().subList(0, separator + 1));
                    firstChildNodes.addAll(parent1.getNodes().subList(separator + 1, parent1.getNodes().size()));

                    ArrayList<Node> secondChildNodes = new ArrayList<>(parent1.getNodes().subList(0, separator + 1));
                    secondChildNodes.addAll(parent2.getNodes().subList(separator + 1, parent2.getNodes().size()));

                    Graph child1 = new Graph(firstChildNodes, parent1.getEdges(), parent1.getH(), parent1.getW());
                    Graph child2 = new Graph(secondChildNodes, parent2.getEdges(), parent2.getH(), parent2.getW());

                    return Arrays.stream(new Graph[]{child1, child2});
                })
                .collect(Collectors.toList());

        // If population size was odd
        if (size % 2 != 0) {
            children.add(population.get(size - 1));
        }
        population.addAll(children);
        System.out.println(" completed in: " + (System.currentTimeMillis() - now));
    }

    @Override
    public void mutation(double mutationRate) {
        System.out.print("Mutation phase");
        long now = System.currentTimeMillis();
        for (Graph g : this.population) {
            double randomValue = new Random().nextDouble();
            if (randomValue <= mutationRate) {
                g.circularMutation(); // change here if another mutation method is used
            }
        }
        System.out.println(" completed in: " + (System.currentTimeMillis() - now));
    }
    @Override
    public void calculateFitness() {
        System.out.print("Calculating fitness");
        long now = System.currentTimeMillis();
        initializeTransientFields(processorCount);
        semaphore.drainPermits(); //
        for (Graph graph : this.population) {
            executor.submit(() -> {
                try {
                    graph.fitnessEvaluation();// Perform fitness evaluation
                    //System.out.println("Evaluated graph with fitness: " + graph.getFitnessScore());
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
        //calculateFitness();
        generationSnapshots.add(initialGraph);
        System.out.println("initial graph fitness: " + initialGraph.getFitnessScore());
        while (iterations != 0) {
            calculateFitness();
            generationSnapshots.add(getBestGraph(population));
            System.out.println("Generation "+generation+" best:" + getBestGraph(this.population).getFitnessScore());
            selection();
            crossoverOnePoint();
            mutation(MUTATION_PROBABILITY);

            iterations--;
            generation++;
        }
        shutdownAndAwaitTermination(executor);
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Total for gen: " + generation + " took " + elapsedTime);
        animateGenerations();

    }

    public void animateGenerations() {
        Timer timer = new Timer(100, null);
        //final int[] index = {0}; //
        AtomicInteger index = new AtomicInteger(0);
        JFrame frame = new JFrame("Graph Display");
        frame.add(renderer);
        timer.addActionListener(e -> {
            if (index.get() < generationSnapshots.size()) {
                showBestGraph(generationSnapshots.get(index.get())); //index.get()
                //index[0]++;
                index.incrementAndGet();
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
        SwingUtilities.invokeLater(() ->
                renderer.setGraph(bestGraph));
    }

    public Graph getBestGraph(ArrayList<Graph> population) {
        population.sort(Comparator.comparingDouble(Graph::getFitnessScore));
        return population.getLast();
    }

    public void shutdownAndAwaitTermination(ExecutorService executor) {
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


    public static ArrayList<Graph> deserializeSubPopulation(byte[] serializedSubPopulation) throws IOException, ClassNotFoundException {

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



}
