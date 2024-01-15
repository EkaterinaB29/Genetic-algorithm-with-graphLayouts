import javax.swing.*;
import java.util.*;

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
    public double[] rank;

    static Random random = new Random();

    // constructor
    public Graph(int m, int n, int h, int w) {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.lengths= new ArrayList<>();
        this.numNodes = n;
        this.numEdges = m;
        this.h = h;
        this.w = w;
        //maybe will be needed
        //this.rank = new double[]{0, 0};

        addNodes();
        addEdges();
       //minimumDistanceNeighbour();
        this.fitnessScore = fitnessEvaluation();


    }
    //another constructor so it will know which one to use
    //used in the generation of the new population
    public Graph(ArrayList<Node> nodes, ArrayList<Node[]> edges, int h, int w)
    {
        this.nodes=nodes;
        this.edges= edges;
        this.numNodes = nodes.size();
        this.numEdges = edges.size();
        this.lengths= new ArrayList<>();
        this.h = h;
        this.w = w;
        //this.rank = new double[]{0, 0};
        //here I don't
       // minimumDistanceNeighbour(this); // ???calculates the lenghts so
        // I need to call it here?
        this.fitnessScore = fitnessEvaluation();

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

    // method to add nodes

    public void addNodes() {

        while (getNodes().size() < numNodes) {
            double x = random.nextInt(w); //bound so it is visible
            double y = random.nextInt(h);
            Node node = new Node(x, y);
            boolean flag = false;
            for (Node n:getNodes()) {
                // if coordinates have  already been assigned
                if(n.x == x && n.y == y){
                    flag = true;
                    break;
                }
            }
            // If coordinates don't exist, add the node
            if (!flag) {
                getNodes().add(node);
            }
        }
    }


    //
    public void addEdges() {

        int edgesCount = 0;
        while (edgesCount < numEdges) {
            Node sourceNode = getSequentialNode();
            Node destinationNode = null;
            while( destinationNode == null)
            {
                destinationNode= getRandomNode();
                Node[] pair ={sourceNode, destinationNode};
                if(containsEdge(pair))
                {
                    destinationNode=null;
                }
            }
            Node[] pair ={sourceNode, destinationNode};
            edges.add(pair);
            edgesCount++;

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
        if (currentIndex < getNodes().size()) {
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
    /* do it like this or with the collection*/
    public double minimumNodeDistanceSum(Graph graph) {
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

    //I think I don't need this one anymore
    public double minimumNodeDistance() {
        double minNodeD = Double.MAX_VALUE;
        if (!getLengths().isEmpty()) {
            minNodeD = Collections.min(lengths);
        }
        return minNodeD;
    }

    /*Edge Length Deviation: The length of each edge is measured and
    compared to the ”optimal”edge length, which is little more than
    the mini-mum edge length found from the present layout.*/
    ///*assuming here that the true value is the optimal value so the deviation will be
    //* calcualated as a difference between the value of the edge minus optimalEdgeLength
    public double edgeLengthDeviation(Graph graph)
    {

        double optimalEdgeLength =minimumNodeDistance()+5;
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
//count num if intersect - find the all the edges ehwre all the deges are A ,and  when one fo
            //go trough all edges ; for each edge seach all the other edges where one of the components of the pair is present and found how many it is present ,
           // a-> b  tak 1st and loop trough the others and check how many exist  l how many crosses you overcounted and substract
            //geometrcak don't
        }
        //System.out.println(edgeCross);
        return edgeCross;
    }

    //  fintness evaluation
    public double fitnessEvaluation()
    {


       /* double minNodeDist = minimumNodeDistanceSum(this); // It should never return 0.
        double minNodeDist2 = Math.pow(minNodeDist, 2.0);
        double edgeLenDev = edgeLengthDeviation(this);
        double edgeCross = edgeCrossings();


        System.out.println(minNodeDist);
        System.out.println(minNodeDist2);
        System.out.println(edgeLenDev);
        System.out.println(edgeCross);
        //System.out.println(wnd2);*/



     double fitness_score= 2 * minimumNodeDistanceSum(this)
                - 2 * edgeLengthDeviation(this)
                -2.5*(edgeLengthDeviation(this)/minimumNodeDistance()
                -0.25*(Math.pow(minimumNodeDistance(), 2.0) * numNodes)
                -(edgeCrossings())); //increase penalty for crosses
        ;

        return fitness_score;
    }

    //mutation on a single Node

    public Graph mutation() {

        Node random_node = this.getRandomNode();
        for ( Node[] n : edges) {
            double angle=random.nextDouble(360);
            if(n[0] == random_node || random_node == n[1])
            {
                //get the pair [random_node, some other_node]
                double radius = Node.euclideanDistance(n[0],n[1]);
                double newX = (radius * Math.cos( angle * Math.PI / 180)) + (random_node.x);
                double newY = (radius * Math.sin(angle* Math.PI / 180)) + (random_node.y);
                Node.moveNode(random_node, newX,newY);
            }

        }

        return this;
    }


    }

