import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

class Graph extends JPanel {

    public double fitnessScore;
    ArrayList<Node> nodes;
    ArrayList<Edge> edges;
    ArrayList<Double> lengths;
    int numNodes;
    int numEdges;
    public int h;
    public int w;
    static Random random = new Random();
    //private Map<Node, Point> nodePositions;

    // constructor
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

    //another constructor so it will know which one to use
    //used in the generation of the new population
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
                if (n.x == x + 1 && n.y == y + 1) {
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

    public Node getRandomNode() {
        int randomIndex = random.nextInt(getNodes().size());
        return getNodes().get(randomIndex);
    }

    public double minimumDistanceNeighbourSum() {
        double totalMinDistance;
        totalMinDistance = nodes.stream().mapToDouble(currentNode -> {
            // Stream through edges to find the minimum distance for the current node
            return edges.stream().filter(edge -> (edge.getOrigin(nodes) == currentNode || edge.getDestination(nodes) == currentNode) && edge.getOrigin(nodes) != edge.getDestination(nodes)).mapToDouble(edge -> Node.euclideanDistance(edge.getOrigin(nodes), edge.getDestination(nodes))).min().orElse(Double.MAX_VALUE);
        }).sum();

        return totalMinDistance;
    }

    public void fitnessEvaluation() {
        double minNodeDist = minimumDistanceNeighbourSum();
        //TODO ASK ABOUT THIS how much is little more?
        minNodeDist = (minNodeDist <= 0 || minNodeDist == Double.MAX_VALUE) ? 1 : minNodeDist;
        double edgeLenDev = edgeLengthDeviation();
        double edgeCross = edgeCrossings();
        double calculatedDiff = (edgeLenDev / minNodeDist) - (0.25 * Math.min(minNodeDist * minNodeDist, Double.MAX_VALUE) * numNodes) - edgeCross;
        double diff = Math.max(0, calculatedDiff);
        this.fitnessScore = Math.abs(4 * minNodeDist - 2 * edgeLenDev - 5.0 * diff);

    }

    private double edgeLengthDeviation() {
        double optimalEdgeLength = this.edges.stream().mapToDouble(e -> Node.euclideanDistance(e.getOrigin(nodes), e.getDestination(nodes))).min().orElse(Double.MAX_VALUE) + 5;
        return this.edges.stream().mapToDouble(e -> Math.pow(Node.euclideanDistance(e.getOrigin(nodes), e.getDestination(nodes)) - optimalEdgeLength, 2)).average().orElse(Double.MAX_VALUE);
    }

    public double edgeCrossings() {
        double edgeCross = 0;
        int i = 0;
        List<Edge> edgeList = new ArrayList<>(edges);  // Convert HashSet to ArrayList

        while (i < this.getEdges().size() - 1) {
            //  first edge - first pair
            Edge edge1 = edgeList.get(i);
            int j = i + 1;
            while (j < this.getEdges().size()) {
                Edge edge2 = edgeList.get(j);
                if (edge1.intersects(edge2, nodes)) {
                    edgeCross++;
                }
                j++;
            }
            i++;

        }

        return edgeCross;
    }
    //mutation on a single Node TODO RESTRUCTURE!

    /***
     public Graph mutationNew() {
     Node randomNode = this.getRandomNode();
     for ( int i=0 ; i < edges.size(); i++) {
     double angle=random.nextDouble(180);
     if( this.edges.get(i).origin == randomNode || randomNode == this.edges.get(i).destination)
     {
     //get the pair [random_node, some other_node]
     double radius = Node.euclideanDistance(this.edges.get(i).origin,this.edges.get(i).destination);
     double newX = (radius * Math.cos( angle * Math.PI / 180)) + (randomNode.x);
     double newY = (radius * Math.sin(angle* Math.PI / 180)) + (randomNode.y);
     Node.moveNode(randomNode, newX,newY);
     }
     }
     this.fitnessScore = fitnessEvaluation();
     return this;
     }
     ***/
    public Graph mutation() {
        Node randomNode = this.getRandomNode();
        double angle = random.nextDouble() * 180; //random angle

        // to find those connected to the randomNode and apply mutation
        edges.stream().filter(edge -> edge.getOrigin(nodes) == randomNode || edge.getDestination(nodes) == randomNode).forEach(edge -> {
            Node connectedNode = edge.getOrigin(nodes) == randomNode ? edge.getDestination(nodes) : edge.getOrigin(nodes);
            double radius = Node.euclideanDistance(randomNode, connectedNode);
            double newX = (radius * Math.cos(angle * Math.PI / 180)) + randomNode.getX();
            double newY = (radius * Math.sin(angle * Math.PI / 180)) + randomNode.getY();
            // Ensure the new position is within bounds
            randomNode.x = Math.max(0, Math.min(w - 1, newX));
            randomNode.y = Math.max(0, Math.min(h - 1, newY));

        });
        return this;
    }


}

