import java.util.ArrayList;

import static java.awt.geom.Line2D.linesIntersect;

public final class Edge {
    int origin;
    int destination;

    public Edge(int origin, int destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public boolean intersects(Edge other, ArrayList<Node> nodes) {
        return linesIntersect(
                nodes.get(other.origin).getX(), nodes.get(other.destination).getY(),
                nodes.get(this.origin).getX(), nodes.get(this.destination).getY(),
                other.getOrigin(nodes).getX(), other.getOrigin(nodes).getY(),
                other.getDestination(nodes).getX(), other.getDestination(nodes).getY());
    }

    public Node getOrigin(ArrayList<Node> nodes) {
        return nodes.get(origin);
    }

    public Node getDestination(ArrayList<Node> nodes) {
        return nodes.get(destination);
    }
}
