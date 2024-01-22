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

        //g.translate(this.getWidth()/2,this.getHeight()/2);
        // Draw edges
        g.setColor(Color.black);
        int i = 0;

        while (i < graph.getEdges().size()) {
            Node start = graph.getEdges().get(i)[0];
            Node end = graph.getEdges().get(i)[1];

            int x = (int) start.getX();
            int y = (int) start.getY();
            int width = (int) (end.getX());
            int height = (int) (end.getY());
            g.drawLine(x, y, width, height);
            i++;
        }

        // Draw nodes

        g.setColor(Color.RED);
        for (Node node : graph.getNodes()) {
            g.fillOval((int) node.getX()-5, (int) node.getY()-5, 15, 15);
            g.drawString(""+node.id,(int) node.getX()-5, (int) node.getY()-5);

        }

        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Fitness score:" + Double.toString(graph.fitnessScore), 10, 20);

    }


}