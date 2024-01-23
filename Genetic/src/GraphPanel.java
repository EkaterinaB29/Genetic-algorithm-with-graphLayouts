import javax.swing.*;
import java.awt.*;
import java.util.Random;

class GraphPanel extends JPanel {
    private Graph graph;
    public GraphPanel(Graph graph) {
        this.graph = graph;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //g.translate(graph.getW(),graph.getH());
        super.setSize(graph.getW(),graph.getH());

        double edge = 50;
        double minx = graph.getNodes().get(0).x;
        double miny = graph.getNodes().get(0).y;
        double maxx = minx;
        double maxy = miny;
        for (Node node : graph.getNodes()) {
            if (node.getX() > maxx) maxx = node.getX();
            if (node.getX() < minx) minx = node.getX();
            if (node.getY() > maxy) maxy = node.getY();
            if (node.getY() < miny) miny = node.getY();
        }

        double scalex = graph.getW() / (maxx - minx + 2*edge);
        double scaley = graph.getH() / (maxy - miny + 2*edge);
        double offsetx = -minx + edge;
        double offsety = -miny + edge;

        // Draw edges
        g.setColor(Color.blue);
        int i = 0;

        while (i < graph.getEdges().size()) {
            Node start = graph.getEdges().get(i)[0];
            Node end = graph.getEdges().get(i)[1];

            int x = (int) (scalex * (start.getX() + offsetx));
            int y = (int) (scaley * (start.getY() + offsety));
            int width = (int) (scalex * (end.getX() + offsetx));
            int height = (int) (scaley * (end.getY() + offsety));
            g.drawLine(x, y, width, height);
            i++;
        }

        // Draw nodes

        g.setColor(Color.red);
        for (Node node : graph.getNodes()) {
            double x = scalex * (node.getX() + offsetx);
            double y = scaley * (node.getY() + offsety);

            g.fillOval((int) x-5, (int) y-5, 15, 15);
            g.drawString(""+node.id,(int) x-5, (int) y-5);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Fitness score:" + Double.toString(graph.fitnessScore), 10, 20);

    }


}