import javax.swing.*;
import java.sql.Array;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

import static java.awt.geom.Line2D.linesIntersect;


class Graph extends JPanel {
    public double fitnessScore;
    ArrayList<Node> nodes;
    ArrayList<Node[]> edges;
    ArrayList<Double> lengths;
    int numNodes;
    int numEdges;
    public int h;
    public int w;
    public boolean flag;

    static Random random = new Random();

    // constructor
    public Graph(int n, int m, int h, int w,boolean flag) {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.lengths= new ArrayList<>();
        this.numNodes = n;
        this.numEdges = m;
        this.h = h;
        this.w = w;
        this.flag=flag;
        addNodes();
        addEdges();
        //minimumDistanceNeighbour();
        this.fitnessScore = fitnessEvaluation();


    }
    //another constructor so it will know which one to use
    //used in the generation of the new population
    public Graph(ArrayList<Node> nodes, ArrayList<Node[]> edges, int h, int w,boolean flag)
    {
        this.nodes= new ArrayList<>(nodes);
        nodes.forEach(node -> {
            node.x += random.nextInt(-1,1) * random.nextInt(4);  // this I am not sure if it is okay?
            node.y += random.nextInt(-1,1) * random.nextInt(4);
        });
        this.edges= edges;
        this.numNodes = nodes.size();
        this.numEdges = edges.size();
        this.lengths= new ArrayList<>();
        this.h = h;
        this.w = w;
        this.flag=flag;
        this.fitnessScore = fitnessEvaluation();

    }

    /* A copy constructor. */

    public Graph(Graph graph)
    {
        nodes = new ArrayList<Node>();
        for (Node node : graph.nodes) {
            nodes.add(new Node(node));
        }

        edges = new ArrayList<Node[]>();
        for (Node[] edge : graph.edges) {
            Node node0 = getNodeId(edge[0].getId());
            Node node1 = getNodeId(edge[1].getId());
            Node[] newEdge = {node0, node1};
            edges.add(newEdge);
        }

        lengths = new ArrayList<Double>();
        lengths.addAll(graph.lengths);

        numNodes = nodes.size();
        numEdges = edges.size();
        flag=graph.flag;
        h = graph.h;
        w = graph.w;

        fitnessScore = graph.fitnessScore;
        currentIndex = graph.currentIndex;
    }

    public double getFitnessScore()
    {
        return fitnessScore;
    }
    public ArrayList<Double> getLengths() {
        return lengths;}
    public ArrayList<Node[]> getEdges() {
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
            Node[] pair = {sourceNode, destinationNode};
            edges.add(pair);
        }
    }

    public boolean containsEdge(Node[] edge) {
        for (Node[] existingEdge : edges) {
            if ((existingEdge[0] == edge[0] && existingEdge[1] == edge[1]) ||
                    (existingEdge[0] == edge[1] && existingEdge[1] == edge[0])) {
                return true;
            }
        }
        return false;
    }
    //  Method to get a random Node
    public Node getRandomNode() {
        int randomIndex = random.nextInt(getNodes().size());
        return getNodes().get(randomIndex);
    }

    private int currentIndex = 0;

    public Node getSequentialNode() {
        // Ensure that currentIndex is within the bounds of the list
        if ( !getNodes().isEmpty() && currentIndex < getNodes().size()) {
            Node node = getNodes().get(currentIndex);
            currentIndex++;
            return node;
        } else {
            // Reset currentIndex to 0 when all nodes have been traversed
            currentIndex = 0;
            return getNodes().get(0); //
        }
    }
    public double minimumDistanceNeighbour(Node startNode ) {
        // Parallel Computation
        if(this.flag)
        {
            // Efficient if you have a list where iteration operations vastly outnumber mutations
            List<Double> edgeLengths = new CopyOnWriteArrayList<>();

            this.getEdges().parallelStream().forEach(edge -> {
                Node initialNode = edge[0];
                Node targetNode = edge[1];
                if (startNode.equals(initialNode) || startNode.equals(targetNode)) {
                    double edgeLength = Node.euclideanDistance(initialNode, targetNode);
                    edgeLengths.add(edgeLength); // add the length of the newly created edge
                }
            });
             return edgeLengths.isEmpty() ? Double.MAX_VALUE : Collections.min(edgeLengths);
        }


        // Sequential computation
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < this.getEdges().size(); i++) {
            Node initialNode = this.getEdges().get(i)[0];
            Node targetNode = this.getEdges().get(i)[1];
            if (startNode == initialNode || startNode == targetNode) {
                double edgeLength = Node.euclideanDistance(initialNode, targetNode);
                getLengths().add(edgeLength); // add the length of the newly created edge
            }
        }
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
        DoubleAdder s = new DoubleAdder();
        // Parallel Computation
        if(this.flag)
        {
            this.getNodes().parallelStream().forEach(node -> {
                double value = minimumDistanceNeighbour(node);
                s.add(value);

            });
            return s.sum();
        }
        // Sequential Computation
        double sum = 0;
        for (Node node : this.getNodes()) {
            sum += minimumDistanceNeighbour(node); // do I send this or graph? And how to know which one?
            //System.out.println(sum);
        }
        return sum;


    }
    /*
     * method that traverses all the nodes and for each node it searches for the nearest neighbour node
     * at the end we have the smallest distance between two nodes overall the graph then calculates by
     * Minimum Node Distance = (Number of Nodes × (Minimum Node Distance)^2)
     *
     */


    /*Edge Length Deviation: The length of each edge is measured and
    compared to the ”optimal”edge length, which is little more than
    the mini-mum edge length found from the present layout.*/
    ///*assuming here that the true value is the optimal value so the deviation will be
    //* calcualated as a difference between the value of the edge minus optimalEdgeLength
    public double edgeLengthDeviation(Graph graph)
    {
        if(graph.flag){

        }
        if (getLengths().isEmpty())
            return 0;

        double optimalEdgeLength =Collections.min(lengths)+5;
        double sum=0;
        Iterator<Double> iterator = lengths.iterator();
        while (iterator.hasNext()) {
            double d = iterator.next() - optimalEdgeLength;
            sum+= Math.pow(d,2);
        }

        return Math.sqrt(sum);
    }

    // Method that counts edge crossings
    public int edgeCrossings() {
        int edgeCross = 0;
        int i = 0;
        while (i < this.getEdges().size() -1) {
            //  first edge - first pair
            int j = i + 1;
            while (j < this.getEdges().size()) {
                if (linesIntersect(this.getEdges().get(i)[0].x, this.getEdges().get(i)[0].y, this.getEdges().get(i)[1].x, this.getEdges().get(i)[1].y,this.getEdges().get(j)[0].x,this.getEdges().get(j)[0].y, this.getEdges().get(j)[1].x, this.getEdges().get(j)[1].y)) {
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
        double edgeLenDev = edgeLengthDeviation(this);
        double edgeCross = edgeCrossings();


        double fitness_score = 4 * minNodeDist - 2 * edgeLenDev - 5.0 * (edgeLenDev/minNodeDist - 0.25 * minNodeDist2 * numNodes - edgeCross );

        return fitness_score;
    }

    //mutation on a single Node

    public Graph mutation() {
        Node random_node = this.getRandomNode();
        for ( int i=0 ; i < edges.size(); i++) {
            double angle=random.nextDouble(180);
            if( this.edges.get(i)[0] == random_node || random_node == this.edges.get(i)[1])
            {
                //get the pair [random_node, some other_node]
                double radius = Node.euclideanDistance(this.edges.get(i)[0],this.edges.get(i)[1]);
                double newX = (radius * Math.cos( angle * Math.PI / 180)) + (random_node.x);
                double newY = (radius * Math.sin(angle* Math.PI / 180)) + (random_node.y);
                Node.moveNode(random_node, newX,newY);
            }
        }
        this.fitnessScore = fitnessEvaluation();
        return this;
    }


}

