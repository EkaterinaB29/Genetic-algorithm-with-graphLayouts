import javax.swing.*;
import java.util.*;

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
    private int currentIndex = 0;

    // constructor
    public Graph(int n, int m, int h, int w) {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.lengths= new ArrayList<>();
        this.numNodes = n;
        this.numEdges = m;
        this.h = h;
        this.w = w;
        addNodes();
        addEdges();
        this.fitnessScore = fitnessEvaluation();
    }
    //another constructor so it will know which one to use
    //used in the generation of the new population
    public Graph(ArrayList<Node> nodes, ArrayList<Edge> edges, int h, int w)
    {
        this.nodes= new ArrayList<>(nodes);
        nodes.forEach(node -> {
            node.x += random.nextInt(-1,1) * random.nextInt(4);
            node.y += random.nextInt(-1,1) * random.nextInt(4);
        });
        this.edges= edges;
        this.numNodes = nodes.size();
        this.numEdges = edges.size();
        this.lengths= new ArrayList<>();
        this.h = h;
        this.w = w;
        this.fitnessScore = fitnessEvaluation();
    }

    /* A copy constructor. */
    public Graph(ArrayList<Node> nodes, ArrayList<Edge> edges, int h, int w, Graph graph)
    {
        this.edges = new ArrayList<Edge>();
        this.nodes = new ArrayList<Node>();

        for (Node node : graph.nodes) {
            this.nodes.add(new Node(node));
        }
        for (Edge edge : graph.edges) {
            Node node0 = getNodeId(edge.getOrigin().getId());
            Node node1 = getNodeId(edge.getDestination().getId());
            Edge newEdge = new Edge(node0, node1);
            this.edges.add(newEdge);
        }
        this.h = graph.h;
        this.w = graph.w;
        lengths = new ArrayList<Double>();
        lengths.addAll(graph.lengths);
        numNodes = this.nodes.size();
        numEdges = this.edges.size();
        fitnessScore = graph.fitnessScore;
        currentIndex = graph.currentIndex;
    }

    public double getFitnessScore()
    {
        return fitnessScore;
    }
    public ArrayList<Double> getLengths() {
        return lengths;}
    public ArrayList<Edge> getEdges() {
        return edges;
    }
    public int getW() {
        return w;
    }
    public  int getH() {
        return h;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public Node getNodeId(int id) {
        for (Node node : nodes) {
            if (node.getId() == id)
                return node;
        }
        return null;
    }

    // method to add nodes

    public void addNodes() {
        int id = 0;
        while (getNodes().size() < numNodes) {
            double x = random.nextInt(w); //bound so it is visible
            double y = random.nextInt(h);
            Node node = new Node(id, x, y);
            boolean flag = false;
            for (Node n:getNodes()) {
                // if coordinates have  already been assigned
                if(n.x == x && n.y == y){
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                getNodes().add(node);
                id++;
            }
        }
    }

    public void addEdges() {
        for (int i = 0; i < numEdges; i++) {
            Collections.shuffle(nodes);
            Node sourceNode = nodes.getFirst();
            Node destinationNode = nodes.stream().filter(node -> node!=sourceNode).toList().getFirst();
            Edge pair = new Edge(sourceNode, destinationNode);
            edges.add(pair);
        }
    }


    //  Method to get a random Node
    public Node getRandomNode() {
        int randomIndex = random.nextInt(getNodes().size());
        return getNodes().get(randomIndex);
    }

    public double minimumDistanceNeighbour(Node startNode ) {
        double minDistance = Double.MAX_VALUE;
        this.edges.forEach(edge -> {
            if (edge.origin == startNode || edge.destination == startNode) {
                double edgeLength = Node.euclideanDistance(edge.origin, edge.destination);
                getLengths().add(edgeLength); // add the length of the newly created edge
            }
        });
        if (!getLengths().isEmpty()) {
            minDistance = Collections.min(getLengths());
        }
        return minDistance;

    }

    /*
    * Minimum Node Distance Sum: The distance of each node from its
    * near-est neighbour is measured, and the distances are added up. The bigger
    sum the more evenly the nodes are usually distributed over the
    drawing area.*/

    public double minimumNodeDistanceSum(Graph graph) {
        double sum = 0;
        for (Node node : this.getNodes()) {
            sum += minimumDistanceNeighbour(node);
        }
        return sum;
    }

    /*Edge Length Deviation: The length of each edge is measured and
    compared to the ”optimal”edge length, which is little more than
    the minimum edge length found from the present layout.*/
    ///*assuming here that the true value is the optimal value so the deviation will be
    //* calculated as a difference between the value of the edge minus optimalEdgeLength
    public double edgeLengthDeviation()
    {
        if (getLengths().isEmpty())
            return 0;
        double optimalEdgeLength =Collections.min(this.lengths)+5; //TODO CHECK THIS WHEN TESTING SHOULD BE MODIFIED!!!
        double sum=0;
        Iterator<Double> iterator = this.lengths.iterator();
        while (iterator.hasNext()) {
            double d = iterator.next() - optimalEdgeLength;
            sum+= Math.pow(d,2);
        }

        return Math.sqrt(sum);
    }

    // Method that counts edge crossings TODO RESTRUCTURE

   public double edgeCrossings() {
        double edgeCross = 0;
        int i = 0;
        while (i < this.getEdges().size() -1) {
            //  first edge - first pair
            Edge edge1 = edges.get(i);
            int j = i + 1;
            while (j < this.getEdges().size()) {
                Edge edge2 = edges.get(j);
                if (edge1.intersects(edge2)){
                    edgeCross++;
                }
                j++;
            }
            i++;

        }
        return edgeCross;
    }

    //  fitness evaluation
    public double fitnessEvaluation()
    {
        double minNodeDist = minimumNodeDistanceSum(this);
        double minNodeDist2 = Math.pow(minNodeDist, 2.0);
        double edgeLenDev = edgeLengthDeviation();
        double edgeCross = edgeCrossings();
        double ratioEdgeLenDev = edgeLenDev / minNodeDist;

        // Check for to avoid division by zero.
        if (minNodeDist == 0) {
            return Double.MAX_VALUE;
        }
        double calculatedDiff = ratioEdgeLenDev - (0.25 * minNodeDist2 * numNodes) - edgeCross;
        double diff = Math.max(0, calculatedDiff);
        double fitness_score= 4 * minNodeDist - 2 * edgeLenDev - 5.0 * diff;

        if (fitness_score < Double.MIN_VALUE) {
            return Double.MIN_VALUE;
        } else if (fitness_score > Double.MAX_VALUE) {
            return Double.MAX_VALUE;
        }
        return fitness_score;
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
        edges.stream()
                .filter(edge -> edge.getOrigin() == randomNode || edge.getDestination() == randomNode)
                .forEach(edge -> {
                    Node connectedNode = edge.getOrigin()== randomNode ? edge.getDestination() : edge.getOrigin();
                    // if it is the origin node, then find the destination
                    // if it was the destination random node chosen then give its origin mate
                    double radius = Node.euclideanDistance(randomNode, connectedNode);
                    double newX = (radius * Math.cos(angle * Math.PI / 180)) + randomNode.getX();
                    double newY = (radius * Math.sin(angle * Math.PI / 180)) + randomNode.getY();
                    Node.moveNode(randomNode, newX,newY);

                });
        this.fitnessScore = fitnessEvaluation();
        return this;
    }

}

