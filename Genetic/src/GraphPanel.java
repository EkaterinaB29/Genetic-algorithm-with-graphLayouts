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

        //g.translate(0,0);

        // Draw edges
        g.setColor(Color.BLACK);
        int i=0;

        while (i < graph.getEdges().size()) {
            Node start = graph.getEdges().get(i)[0];
            Node end = graph.getEdges().get(i)[1];
            g.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
            i++;
        }

        // Draw nodes
        g.setColor(Color.RED);
        for (Node node : graph.getNodes()) {
            g.fillOval((int) node.getX()-5, (int) node.getY()-5, 15, 15);
        }

    }


}