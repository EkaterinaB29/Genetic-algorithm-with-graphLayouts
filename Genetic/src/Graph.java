import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
     public ArrayList<Point> edgeCrossings() {
        ArrayList<Point> crossingPoints = new ArrayList<>();
        for (int i = 0; i < this.getEdges().size() - 1; ++i) {
            Edge edge1 = this.getEdges().get(i);
            for (int j = i + 1; j < this.getEdges().size(); ++j) {
                Edge edge2 = this.getEdges().get(j);
                if (edge1.intersects(edge2, this.nodes)) {
                    Point crossingPoint = calculateIntersectionPoint(edge1, edge2);
                    if (crossingPoint != null) {
                        crossingPoints.add(crossingPoint);
                    }
                }
            }
        }
        return crossingPoints;
    }

    private Point calculateIntersectionPoint(Edge edge1, Edge edge2) {
        Node p1 = edge1.getOrigin(this.nodes);
        Node p2 = edge1.getDestination(this.nodes);
        Node p3 = edge2.getOrigin(this.nodes);
        Node p4 = edge2.getDestination(this.nodes);

        double d = (p1.getX() - p2.getX()) * (p3.getY() - p4.getY()) - (p1.getY() - p2.getY()) * (p3.getX() - p4.getX());
        if (d == 0) return null;

        double xi = ((p3.getX() - p4.getX()) * (p1.getX() * p2.getY() - p1.getY() * p2.getX()) - (p1.getX() - p2.getX()) * (p3.getX() * p4.getY() - p3.getY() * p4.getX())) / d;
        double yi = ((p3.getY() - p4.getY()) * (p1.getX() * p2.getY() - p1.getY() * p2.getX()) - (p1.getY() - p2.getY()) * (p3.getX() * p4.getY() - p3.getY() * p4.getX())) / d;

        return new Point(xi, yi);
    }

    private boolean hasSymmetricalCrossings(ArrayList<Point> crossingPoints) {
        Set<Point> uniquePoints = new HashSet<>(crossingPoints);
        return uniquePoints.size() < crossingPoints.size();
    }

    protected void fitnessEvaluation() {
        double minNodeDistSum = minimumDistanceNeighbourSum();
        double edgeLenDev = edgeLengthDeviation();
        ArrayList<Point> crossingPoints = this.edgeCrossings();
        double edgeCross = crossingPoints.size();


        // Weights for each component
        double w1 = 2.0; // Weight for Minimum Distance Neighbour Sum
        double w2 = 2.0; // Weight for Edge Length Deviation
        double w3 = 1.0; // Weight for Edge Crossings
        double w4 = 2.5; // Weight for edge length deviation and minimum distance neighbour sum
        double w5 = 0.5; // Weight for node spread
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
        double weightedEdgeLenDevPenalty = w4 * (edgeLenDev / (minNodeDistSum + epsilon));
        double weightedNodeSpread = w5 * numNodes * Math.min(minNodeDistSum * minNodeDistSum, Double.MAX_VALUE);

        // Calculate the fitness score
        double calculatedDiff = weightedMinNodeDistSum + weightedEdgeLenDev -
                weightedEdgeLenDevPenalty -
                weightedNodeSpread +
                weightedEdgeCross;

        this.fitnessScore = 1 + Math.abs(calculatedDiff);
        
            if (hasSymmetricalCrossings(crossingPoints)) {
                this.fitnessScore *= 1.5;
            }
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


    /*public double edgeCrossings() {
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
    }*/

    public void circularMutation() {
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
    }

    public void twoEdgeMutation() {
        Node randomNode = getRandomNode();
        List<Edge> incidentEdges = edges.stream()
                .filter(edge -> edge.getOrigin(nodes) == randomNode || edge.getDestination(nodes) == randomNode)
                .collect(Collectors.toList());

        if (incidentEdges.size() < 2) {
            return; // Not enough edges to mutate
        }

        Edge edge1 = incidentEdges.get(0);
        Edge edge2 = incidentEdges.get(1);

        mutateEdge(edge1, randomNode);
        mutateEdge(edge2, randomNode);

    }

    private void mutateEdge(Edge edge, Node randomNode) {
        Node origin = edge.getOrigin(nodes);
        Node destination = edge.getDestination(nodes);

        double length = Node.euclideanDistance(origin, destination);
        // Calculates the (length)

        double angle = Math.atan2(destination.getY() - origin.getY(), destination.getX() - origin.getX());
        // Calculates the angle of the edge using the arctangent of the differences in y and x coordinates.

        double newX = random.nextDouble() * w;
        double newY = random.nextDouble() * h;
        // Generates new random x and y coordinates within the graph's width and height.

        randomNode.setX(newX);
        randomNode.setY(newY);
        // Sets the random node's coordinates to the new random coordinates.

        destination.setX(newX + length * Math.cos(angle));
        destination.setY(newY + length * Math.sin(angle));
        // Sets the destination node's coordinates to maintain the edge's original length and angle.
    }
    
    public Graph mutationFlipCoordinates() {
        Node randomNode = this.getRandomNode();
        double originalX = randomNode.getX();
        double originalY = randomNode.getY();

        double newX = -originalX;
        double newY = -originalY;

        randomNode.x = Math.max(0, Math.min(w - 1, newX));
        randomNode.y = Math.max(0, Math.min(h - 1, newY));


        Random random = new Random();
        double transformChoice = random.nextDouble();

        if (transformChoice < 0.33) {
            //Flip x to -x, y remains the same
            randomNode.x = -randomNode.x;
        } else if (transformChoice < 0.66) {
            // Flip y to -y, x remains the same
            randomNode.y = -randomNode.y;
        } else {
            //Flip both x to -x and y to -y
            randomNode.x = -randomNode.x;
            randomNode.y = -randomNode.y;
        }

        randomNode.x = Math.max(0, Math.min(w - 1, randomNode.x));
        randomNode.y = Math.max(0, Math.min(h - 1, randomNode.y));

        return this;
    }

}
