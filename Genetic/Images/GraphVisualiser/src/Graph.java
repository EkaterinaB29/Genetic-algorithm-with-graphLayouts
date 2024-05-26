import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Graph {
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private double fitnessScore;

    public Graph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public double getFitnessScore() {
        return fitnessScore;
    }

    public void setFitnessScore(double fitnessScore) {
        this.fitnessScore = fitnessScore;
    }

    public void loadNodesFromCSV(String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            scanner.nextLine(); // Skip header
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                int id = Integer.parseInt(data[0]);
                double x = Double.parseDouble(data[2]);
                double y = Double.parseDouble(data[3]);
                nodes.add(new Node(id, x, y));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadEdgesFromCSV(String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            scanner.nextLine(); // Skip header
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                int source = Integer.parseInt(data[0]);
                int target = Integer.parseInt(data[1]);
                edges.add(new Edge(source, target));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
