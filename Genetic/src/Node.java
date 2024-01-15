import java.util.Random;

class Node {
    double x;
    double y;
    public static Random random=new Random();
    public Node(double x, double y) {
        this.x = x;
        this.y = y;

    }
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

// Here it crashes ,Node two is null , it doesn't let the Graph object to initilize
    public static double euclideanDistance(Node one, Node two)
    {
        double deltaX = one.getX() - two.getX();
        double deltaY = two.getY()- one.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
public static  Node moveNode(Node one, double value1, double value2)
{
    one.x = value1; // move the node to a random position according width
    one.y =value2; // move the node to a random position according height
    return one;
}


}