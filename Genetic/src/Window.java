import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Window extends JFrame implements ActionListener {
    private JTextField widthField, heightField, verticesField, edgesField;
    private JRadioButton sequentialButton, parallelButton, distributiveButton;
    private JButton runButton;
    private final int p = 100;
    private String[] args;

    public Window(String[] args) {
        this.args = args;
        setTitle("Choose initial values!");
        setSize(600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        // Create layout and components
        JPanel parametersPanel = new JPanel();
        parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
        parametersPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around the panel
        // Mode buttons
        sequentialButton = new JRadioButton("Sequential", true);
        parallelButton = new JRadioButton("Parallel");
        distributiveButton = new JRadioButton("Distributive");

        runButton = new JButton("Run");
        runButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button
        runButton.setFont(new Font("Arial", Font.BOLD, 14)); // Change font
        runButton.setBackground(new Color(150, 100, 200)); // Set button color
        runButton.setForeground(Color.WHITE); // Set text color
        runButton.addActionListener(this);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(sequentialButton);
        modeGroup.add(parallelButton);
        modeGroup.add(distributiveButton);

        parametersPanel.add(sequentialButton);
        parametersPanel.add(parallelButton);
        parametersPanel.add(distributiveButton);

        parametersPanel.add(createInputField("Enter width of the desired window:", widthField = new JTextField()));
        parametersPanel.add(createInputField("Enter height of the desired window:", heightField = new JTextField()));
        parametersPanel.add(createInputField("Enter the number of vertices of the initial graph:", verticesField = new JTextField()));
        parametersPanel.add(createInputField("Enter the number of edges of the desired graph:", edgesField = new JTextField()));
        parametersPanel.add(runButton);
        add(parametersPanel);
    }

    private JPanel createInputField(String label, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(label));
        //textField.setPreferredSize(new Dimension(200, 30)); // Set a preferred size for uniformity
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        textField.setFont(new Font("Arial", Font.CENTER_BASELINE, 14));
        panel.add(textField);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            int numNodes = Integer.parseInt(verticesField.getText());
            int numEdges = Integer.parseInt(edgesField.getText());
            int windowHeight = Integer.parseInt(heightField.getText());
            int windowWidth = Integer.parseInt(widthField.getText());
            Random random = new Random();
            HashSet<Edge> uniqueEdges = new HashSet<>();

            for (int i = 0; i < numEdges; i++) {
                int origin = random.nextInt(numNodes);
                int destination = random.nextInt(numNodes);
                while (destination == origin || uniqueEdges.contains(new Edge(origin, destination))) {
                    //TODO PROBLEM IF COMPLETE GRAPH
                    destination = random.nextInt(numNodes);
                }
                uniqueEdges.add(new Edge(origin, destination));
            }

            ArrayList<Edge> edges = new ArrayList<>(uniqueEdges);
            Graph initialGraph = new Graph(numNodes, edges, windowWidth, windowHeight);
            int processors = sequentialButton.isSelected() ? 1 : Runtime.getRuntime().availableProcessors();
            GeneticAlgorithm computation = new GeneticAlgorithm(initialGraph, p,processors);
            if (distributiveButton.isSelected()) {
                GeneticAlgorithmDistributed distributed = new GeneticAlgorithmDistributed(computation);
                distributed.execute(args);
            } else {
                computation.compute();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "The values must be integers!");
        }
    }
}


