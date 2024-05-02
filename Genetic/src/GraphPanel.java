import javax.swing.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Ellipse2D;

class GraphPanel extends JPanel {
    private Graph graph;

    public GraphPanel(Graph graph) {
        this.graph = graph;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        setBackground(new Color(237, 210, 225, 242)); // Set a light background

        // Setup scaling and offset based on node positions
        double edgePadding = 50;
        double minx = Double.MAX_VALUE, maxx = Double.MIN_VALUE;
        double miny = Double.MAX_VALUE, maxy = Double.MIN_VALUE;
        for (Node node : graph.getNodes()) {
            minx = Math.min(minx, node.getX());
            maxx = Math.max(maxx, node.getX());
            miny = Math.min(miny, node.getY());
            maxy = Math.max(maxy, node.getY());
        }

        double scaleX = (getWidth() - 2 * edgePadding) / (maxx - minx);
        double scaleY = (getHeight() - 2 * edgePadding) / (maxy - miny);
        double offsetX = edgePadding - minx * scaleX;
        double offsetY = edgePadding - miny * scaleY;

        // Draw edges with curved paths
        for (Edge edge : graph.getEdges()) {
            Node start = edge.getOrigin(graph.getNodes());
            Node end = edge.getDestination(graph.getNodes());
            int x1 = (int) (start.getX() * scaleX + offsetX);
            int y1 = (int) (start.getY() * scaleY + offsetY);
            int x2 = (int) (end.getX() * scaleX + offsetX);
            int y2 = (int) (end.getY() * scaleY + offsetY);

            QuadCurve2D.Float curve = new QuadCurve2D.Float(x1, y1, (x1 + x2) / 2, ((y1 + y2) / 2) - 60, x2, y2);
            g2d.setStroke(new BasicStroke(2)); // Thicker line for better visibility
            g2d.setPaint(new GradientPaint(x1, y1, new Color(0xFFDC5896, true), x2, y2, new Color(0xDAD82277, true), true));
            g2d.draw(curve);
        }

        // Draw nodes as filled circles with labels
        for (Node node : graph.getNodes()) {
            int x = (int) (node.getX() * scaleX + offsetX);
            int y = (int) (node.getY() * scaleY + offsetY);
            g2d.setPaint(new Color(94, 10, 101, 255));
            Ellipse2D.Double circle = new Ellipse2D.Double(x - 10, y - 10, 20, 20);
            g2d.fill(circle);
            g2d.setPaint(new Color(79, 8, 85, 213));
            g2d.draw(circle);
            g2d.drawString(""+node.getId(), x - 5, y - 15);
        }

        // Fitness score
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Fitness score: " + graph.getFitnessScore(), 10, 30);
    }

    public void setGraph(Graph bestGraph) {
        this.graph = bestGraph;
        repaint();
    }
}
