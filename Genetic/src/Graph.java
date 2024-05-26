import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class Graph {

    public double fitnessScore;
    ArrayList<Node> nodes;
    ArrayList<Edge> edges;
    ArrayList<Double> lengths;
    int numNodes;
    int numEdges;
    public int h;
    public int w;
    static Random random = new Random();

    public Graph(int n, ArrayList<Edge> edges, int h, int w) {
        this.nodes = new ArrayList<>();
        this.edges = edges;
        this.lengths = new ArrayList<>();
        this.numNodes = n;
        this.numEdges = edges.size();
        this.h = h;
        this.w = w;
        addNodes();
    }

    public Graph(ArrayList<Node> originalnodes, ArrayList<Edge> edges, int h, int w) {
        this.nodes = new ArrayList<>();
        for (Node node : originalnodes) {
            this.nodes.add(new Node(node));  // Using copy constructor
        }
        nodes.forEach(node -> {
            node.x += random.nextInt(-1, 1) * random.nextInt(4);
            node.y += random.nextInt(-1, 1) * random.nextInt(4);
        });
        this.edges = edges;
        this.numNodes = nodes.size();
        this.numEdges = edges.size();
        this.lengths = new ArrayList<>();
        this.h = h;
        this.w = w;
    }

    public double getFitnessScore() {
        return fitnessScore;
    }

    public ArrayList<Double> getLengths() {
        return lengths;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    // method to add nodes
    public void addNodes() {
        int id = 0;
        while (getNodes().size() < numNodes) {
            double x = random.nextInt(w); //bound so it is visible
            double y = random.nextInt(h);
            boolean flag = false;
            for (Node n : getNodes()) {
                // if coordinates have  already been assigned
                if (n.x == x && n.y == y) {
                    flag = true;
                    break;
                }
            }
            Node node = new Node(id, x, y);
            if (!flag) {
                getNodes().add(node);
                id++;
            }
        }
    }
    /*public void addNodes() {
        int id = 0;
        double initialX = w / 2.0;  // Set all nodes at the center of the width
        double initialY = h / 2.0;  // Set all nodes at the center of the height

        for (int i = 0; i < numNodes; i++) {
            Node node = new Node(id, initialX, initialY);
            getNodes().add(node);
            id++;
        }
    }*/

    public Node getRandomNode() {
        int randomIndex = random.nextInt(getNodes().size());
        return getNodes().get(randomIndex);
    }

    public double minimumDistanceNeighbourSum() {
        // Ensure that we have nodes to avoid Division by zero
        if (nodes.isEmpty()) return 0.1;

        double totalMinDistance = nodes.stream()
                .mapToDouble(currentNode -> edges.stream()
                        .filter(edge -> (edge.getOrigin(nodes) == currentNode || edge.getDestination(nodes) == currentNode)
                                && edge.getOrigin(nodes) != edge.getDestination(nodes))
                        .mapToDouble(edge -> Node.euclideanDistance(edge.getOrigin(nodes), edge.getDestination(nodes)))
                        .min().orElse(0.1)) // If there are no edges, return a large number to avoid contributing to the fitness positively
                .sum();

        return totalMinDistance == 0 ? 0.1 : totalMinDistance; // Avoid returning 0 to prevent division by zero in fitness calculation
    }

    protected void fitnessEvaluation() {
        double minNodeDistSum = minimumDistanceNeighbourSum();
        double edgeLenDev = edgeLengthDeviation();
        double edgeCross = edgeCrossings();


        // Weights for each component
        double w1 = 2.0; // Weight for Minimum Distance Neighbour Sum
        double w2 = 2.0; // Weight for Edge Length Deviation
        double w3 = 1.0; // Weight for Edge Crossings


        // Add small epsilon values to avoid division by zero
        double epsilon = 1e-10;

        // Avoid NaN values and handle zero values appropriately
        if (Double.isNaN(minNodeDistSum) || Double.isNaN(edgeLenDev) || Double.isNaN(edgeCross)) {
            this.fitnessScore = 0;
            return;
        }

        // Calculate the components with weights
        double weightedMinNodeDistSum = w1 * minNodeDistSum;
        double weightedEdgeLenDev = w2 * edgeLenDev;
        double weightedEdgeCross = w3 * edgeCross;
        //double weightedNodeOverlapPenalty = w4 * nodeOverlapPenalty;

        // Calculate the fitness score
        double calculatedDiff = weightedMinNodeDistSum + weightedEdgeLenDev -
                2.5 * (edgeLenDev / (minNodeDistSum + epsilon)) -
                (0.25 * numNodes * Math.min(minNodeDistSum * minNodeDistSum, Double.MAX_VALUE)) +
                weightedEdgeCross;

        this.fitnessScore = 1 + calculatedDiff;
    }


    private double edgeLengthDeviation() {
        if (edges.isEmpty()) return 0;

        double optimalEdgeLength = this.edges.stream()
                .mapToDouble(e -> Node.euclideanDistance(e.getOrigin(nodes), e.getDestination(nodes)))
                .filter(d -> !Double.isNaN(d))
                .min().orElse(5) + 5;

        return this.edges.stream()
                .mapToDouble(e -> {
                    double distance = Node.euclideanDistance(e.getOrigin(nodes), e.getDestination(nodes));
                    if (Double.isNaN(distance)) return Double.MAX_VALUE;
                    return Math.pow(distance - optimalEdgeLength, 2);
                })
                .average().orElse(0);
    }


    public double edgeCrossings() {
        double edgeCross = 0;
        int i = 0;

        while (i < this.getEdges().size() - 1) {
            Edge edge1 = this.getEdges().get(i);
            int j = i + 1;
            while (j < this.getEdges().size()) {
                Edge edge2 = this.getEdges().get(j);
                if (edge1.intersects(edge2, nodes)) {
                    edgeCross++;
                }
                j++;
            }
            i++;
        }
        return edgeCross;
    }

    public Graph mutation() {
        Node randomNode = this.getRandomNode();
        double angle = random.nextDouble() * 360; // Use the full circle for angle
        double radians = Math.toRadians(angle);

        // Rotate connected nodes' positions around the randomNode
        for (Edge edge : edges) {
            Node origin = edge.getOrigin(nodes);
            Node destination = edge.getDestination(nodes);

            if (origin == randomNode) {
                double newX = randomNode.getX() + (destination.getX() - randomNode.getX()) * Math.cos(radians) - (destination.getY() - randomNode.getY()) * Math.sin(radians);
                double newY = randomNode.getY() + (destination.getX() - randomNode.getX()) * Math.sin(radians) + (destination.getY() - randomNode.getY()) * Math.cos(radians);
                destination.setX(Math.max(0, Math.min(w - 1, newX)));
                destination.setY(Math.max(0, Math.min(h - 1, newY)));
            } else if (destination == randomNode) {
                double newX = randomNode.getX() + (origin.getX() - randomNode.getX()) * Math.cos(radians) - (origin.getY() - randomNode.getY()) * Math.sin(radians);
                double newY = randomNode.getY() + (origin.getX() - randomNode.getX()) * Math.sin(radians) + (origin.getY() - randomNode.getY()) * Math.cos(radians);
                origin.setX(Math.max(0, Math.min(w - 1, newX)));
                origin.setY(Math.max(0, Math.min(h - 1, newY)));
            }
        }
        return this;
    }

}
