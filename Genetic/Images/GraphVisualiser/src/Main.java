import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java GraphVisualizer <nodes CSV file> <edges CSV file>");
            return;
        }

        String nodesFile = args[0];
        String edgesFile = args[1];

        Graph graph = new Graph();
        graph.loadNodesFromCSV(nodesFile);
        graph.loadEdgesFromCSV(edgesFile);

        JFrame frame = new JFrame("Graph Visualizer");
        GraphPanel graphPanel = new GraphPanel(graph);
        frame.setLayout(new BorderLayout());
        frame.add(graphPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save as Image");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = JOptionPane.showInputDialog(frame, "Enter filename:", "Save as Image", JOptionPane.PLAIN_MESSAGE);
                if (filename != null && !filename.trim().isEmpty()) {
                    graphPanel.saveAsImage(filename.trim() + ".png");
                    JOptionPane.showMessageDialog(frame, "Image saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(saveButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
