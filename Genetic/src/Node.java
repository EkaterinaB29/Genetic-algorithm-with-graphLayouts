import java.util.Random;

class Node {
    int id;
    double x;
    double y;
    public static Random random=new Random();
    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    /* A copy constructor. */
    public Node(Node node)
    {
        this.id = node.id;
        this.x = node.x;
        this.y = node.y;
    }

    public int getId() { return id; }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }


    public static double euclideanDistance(Node one, Node two)
    {
        double deltaX = one.getX() - two.getX();
        double deltaY = two.getY()- one.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    public static Node moveNode(Node one, double value1, double value2)
    {
        one.x = value1; // move the node to a random position according width
        one.y =value2; // move the node to a random position according height
        return one;
    }


}