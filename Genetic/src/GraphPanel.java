import javax.swing.*;
import java.awt.*;

class GraphPanel extends JPanel {
    private Graph graph;

    public GraphPanel(Graph graph) {
        this.graph = graph;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        super.setSize(graph.getW(), graph.getH());

        double edge = 50;
        double minx = graph.getNodes().getFirst().x;
        double miny = graph.getNodes().getFirst().y;
        double maxx = minx;
        double maxy = miny;
        for (Node node : graph.getNodes()) {
            if (node.getX() > maxx) maxx = node.getX();
            if (node.getX() < minx) minx = node.getX();
            if (node.getY() > maxy) maxy = node.getY();
            if (node.getY() < miny) miny = node.getY();
        }

        double scaleX = graph.getW() / (maxx - minx + 2 * edge);
        double scaleY = graph.getH() / (maxy - miny + 2 * edge);
        double offsetX = -minx + edge;
        double offsetY = -miny + edge;

        // Draw edges
        g.setColor(new Color(125, 10, 200));

        int i = 0;

        while (i < graph.getEdges().size()) {

            Node start = graph.getEdges().get(i).getOrigin(graph.nodes);
            Node end = graph.getEdges().get(i).getDestination(graph.nodes);
            int x = (int) (scaleX * (start.getX() + offsetX));
            int y = (int) (scaleY * (start.getY() + offsetY));
            int width = (int) (scaleX * (end.getX() + offsetX));
            int height = (int) (scaleY * (end.getY() + offsetY));
            g.drawLine(x, y, width, height);
            i++;
        }

        // Draw nodes
        g.setColor(new Color(150, 0, 50));
        for (Node node : graph.getNodes()) {
            double x = scaleX * (node.getX() + offsetX);
            double y = scaleY * (node.getY() + offsetY);
            g.fillOval((int) x - 5, (int) y - 5, 15, 15);
            g.drawString("" + node.getId(), (int) x - 5, (int) y - 5);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Fitness score:" + graph.fitnessScore, 10, 20);


    }

    public void setGraph(Graph bestGraph) {
        this.graph = bestGraph;
        //this.revalidate();
        this.repaint();
    }

}