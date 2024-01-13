import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public class Window extends JPanel {

     public static int p =100; // Is this okay?
    //Graph initialGraph;

    JFrame frame = new JFrame("Choose initial values!");

    public Window() {


        // Creating components
        JPanel parametersPanel = new JPanel();
        parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));

        // Mode buttons
        JRadioButton seq = new JRadioButton("Sequential");
        seq.setSelected(true);
        JRadioButton par = new JRadioButton("Parallel");
        par.setEnabled(true);
        JRadioButton dis = new JRadioButton("Distributive");
        dis.setEnabled(true);

        JLabel l = new JLabel("Enter width of the desired window:");
        JTextField w = new JTextField();
        w.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));// Set preferred size

        JLabel l1 = new JLabel("Enter height of the desired window:");
        JTextField h = new JTextField();
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Set preferred size

        JLabel l2 = new JLabel("Enter the number of vertices of the initial graph:");
        JTextField vert = new JTextField();
        vert.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Set preferred size

        JLabel l3 = new JLabel("Enter the number of edges of the desired graph:");
        JTextField edg = new JTextField();
        edg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Set preferred size



        JButton run = new JButton("Run");
        run.setEnabled(true);


        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(seq);
        modeGroup.add(par);
        modeGroup.add(dis);

        // Adding components to parametersPanel
        parametersPanel.add(seq);
        parametersPanel.add(par);
        parametersPanel.add(dis);
        parametersPanel.add(l);
        parametersPanel.add(w);
        parametersPanel.add(l1);
        parametersPanel.add(h);
        parametersPanel.add(l2);
        parametersPanel.add(vert);
        parametersPanel.add(l3);
        parametersPanel.add(edg);
        parametersPanel.add(run);

        // Adding parametersPanel to the frame's content pane
        frame.getContentPane().add(parametersPanel);

        // Adding ActionListener to the "Run" button directly in Window class
        run.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // System.out.println("Button clicked");
                /*boolean oneCheck = false;
                boolean twoCheck = false;
                boolean threeCheck = false;
                boolean fourCheck = false;*/

                try {
                    int iterations=10;
                    int numNodes = Integer.parseInt(vert.getText());
                    int numEdges = Integer.parseInt(edg.getText());
                    int windowHeight = Integer.parseInt(h.getText());
                    int windowWidth = Integer.parseInt(w.getText());


                    if (seq.isSelected() && numNodes > 0 && numEdges <= (numNodes * (numNodes - 1)) / 2 && numEdges >= numNodes-1 && windowWidth > 0 && windowHeight > 0) {

                        // if (!oneCheck && !twoCheck && !threeCheck && !fourCheck) {

                        Graph initialGraph = new Graph(numEdges, numNodes, windowHeight, windowWidth);
                        GraphPanel graphPanel = new GraphPanel(initialGraph);

                        JFrame frame = new JFrame("Graph Display");
                        frame.setSize(windowHeight, windowWidth);
                        frame.add(graphPanel);
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setVisible(true);

                        GraphPopulation population = new GraphPopulation(initialGraph,p,graphPanel);
                        Computation computation = new Computation(population);
                        computation.compute();

                        //}

                    } else if (par.isSelected()) {
                        // if (!oneCheck && !twoCheck && !threeCheck && !fourCheck) {



                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "The values must be integers!");
                }
            }
        });
        //Adding
        // Setting up the frame
        frame.pack();
        frame.setSize(600, 500);  // Set an appropriate size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        // frame.setResizable(false);


        frame.setVisible(true);  // Make the frame visible


    }
}
