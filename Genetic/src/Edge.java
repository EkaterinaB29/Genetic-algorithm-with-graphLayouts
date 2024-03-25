import static java.awt.geom.Line2D.linesIntersect;

public class Edge {
    Node origin;
    Node destination;

    public Edge(Node origin, Node destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public boolean intersects(Edge other) {
        return linesIntersect(
                this.origin.getX(), this.origin.getY(),
                this.destination.getX(), this.destination.getY(),
                other.getOrigin().getX(), other.getOrigin().getY(),
                other.getDestination().getX(), other.getDestination().getY());
    }


    public Node getOrigin() {
        return origin;
    }

    public Node getDestination() {
        return destination;
    }

    public void setOrigin(Node origin) {
        this.origin = origin;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public double getLength() {
        return Node.euclideanDistance(origin, destination);
    }

    public Edge copy() {
        return new Edge(new Node(origin), new Node(destination));
    }
}
