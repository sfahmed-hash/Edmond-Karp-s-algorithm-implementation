import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class EdmondsK extends JFrame {
    public EdmondsK() {
        // Set up the main window
        setTitle("Edmonds-Karp Max Flow Visualization");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // graph drawing- nodes, edges
        GraphPanel graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER);

        // controls panel hold button and label
        JPanel controls = new JPanel();
        controls.setLayout(new FlowLayout());

        // Button to calculate max flow
        JButton calculateMaxFlowButton = new JButton("Calculate Max Flow");
        controls.add(calculateMaxFlowButton);

        // Label to display the maximum flow
        JLabel maxFlowLabel = new JLabel("Max Flow: ");
        controls.add(maxFlowLabel);

        add(controls, BorderLayout.SOUTH);

        // Action listener for the "Calculate Max Flow" button
        calculateMaxFlowButton.addActionListener(e -> {
            // Ask user for source and sink nodes
            String sourceInput = JOptionPane.showInputDialog(this, "Enter the name of the source node:");
            String sinkInput = JOptionPane.showInputDialog(this, "Enter the name of the sink node:");

            if (sourceInput != null && sinkInput != null) {
                int sourceIndex = graphPanel.getNodeIndexByName(sourceInput);
                int sinkIndex = graphPanel.getNodeIndexByName(sinkInput);

                if (sourceIndex != -1 && sinkIndex != -1) {
                    int maxFlow = graphPanel.runEdmondsKarp(sourceIndex, sinkIndex);
                    maxFlowLabel.setText("Max Flow: " + maxFlow);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid source or sink node name. Please try again.");
                }
            }
        });
    }

    public static void main(String[] args) {
        // Run the GUI
        SwingUtilities.invokeLater(() -> {
            EdmondsK gui = new EdmondsK();
            gui.setVisible(true);
        });
    }
}

class GraphPanel extends JPanel {
    private final ArrayList<Node> nodes = new ArrayList<>(); // List of nodes in the graph
    private final ArrayList<Edge> edges = new ArrayList<>(); // List of edges in the graph
    private final int[][] capacityMatrix; // Capacity matrix of the graph
    private final int[][] residualCapacity; // Residual capacity matrix
    private final ArrayList<String> augmentedPaths; // List of augmented paths found during max flow calculation
    private Node firstSelectedNode = null; // The first node selected for creating an edge

    public GraphPanel() {
        capacityMatrix = new int[50][50]; 
        residualCapacity = new int[50][50]; 
        augmentedPaths = new ArrayList<>(); 
        setBackground(Color.WHITE);

        // Mouse listener to handle clicks for creating nodes and edges
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Node clickedNode = getNodeAt(e.getX(), e.getY());
                if (clickedNode == null) { //NEW NODE CREATION
                    addNode("N " + nodes.size(), e.getX(), e.getY());
                } else {// selecting 2 nodes to create an edge
                    handleNodeSelection(clickedNode);
                }
                repaint();
            }
        });
    }

    // Add a new node at the clicked location
    private void addNode(String name, int x, int y) {
        nodes.add(new Node(name, x, y));
    }

    // Handle the selection of nodes to create an edge
    private void handleNodeSelection(Node clickedNode) {
        if (firstSelectedNode == null) {
            // First node selected
            firstSelectedNode = clickedNode;
            firstSelectedNode.setSelected(true); // color change to red
        } else if (firstSelectedNode != clickedNode) {
            // Second node selected so a prompt for edge capacity
            String capacityInput = JOptionPane.showInputDialog(this, "Enter capacity for the directed edge:");
            try {
                int capacity = Integer.parseInt(capacityInput);
                edges.add(new Edge(firstSelectedNode, clickedNode, capacity));
                capacityMatrix[nodes.indexOf(firstSelectedNode)][nodes.indexOf(clickedNode)] = capacity;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid capacity. Please enter a number.");
            }
            firstSelectedNode.setSelected(false); // color change to green
            firstSelectedNode = null;
        } else {
            // Deselect the node if clicked again
            firstSelectedNode.setSelected(false); 
            firstSelectedNode = null;
        }
    }

    // Get the node at the given (x, y) coordinates
    private Node getNodeAt(int x, int y) {
        for (Node node : nodes) {
            if (node.contains(x, y)) {
                return node;
            }
        }
        return null;
    }

    // Get the index of a node by its name
    public int getNodeIndexByName(String name) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1; // Node not found
    }


    public int runEdmondsKarp(int source, int sink) {
        if (nodes.size() < 2) {
            JOptionPane.showMessageDialog(this, "!! 2 NODES needed atleast.");
            return 0;
        }

        // Initialize residual capacity matrix with the original capacity matrix
        for (int i = 0; i < nodes.size(); i++) {
            System.arraycopy(capacityMatrix[i], 0, residualCapacity[i], 0, nodes.size());
        }

        int maxFlow = 0; // Initialize max flow
        augmentedPaths.clear(); // Clear previous augmented paths

        // Perform BFS to find augmenting paths
        while (true) {
            int[] parent = new int[nodes.size()]; // Array to store the path
            Arrays.fill(parent, -1);
            Queue<Integer> queue = new LinkedList<>();
            queue.add(source);
            parent[source] = source;

            // BFS to find a path from source to sink
            while (!queue.isEmpty() && parent[sink] == -1) {
                int u = queue.poll();
                for (int v = 0; v < nodes.size(); v++) {
                    // parent[v] = -1 indicate that v has not been yet visited
                    if (parent[v] == -1 && residualCapacity[u][v] > 0) {
                        parent[v] = u;
                        queue.add(v);
                    }
                }
            }

            if (parent[sink] == -1) break; // No more augmenting paths

            // Find the minimum residual capacity in the augmenting path
            int flow = Integer.MAX_VALUE;
            ArrayList<String> path = new ArrayList<>();
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                path.add(0, nodes.get(u).getName() + " -> " + nodes.get(v).getName());
                flow = Math.min(flow, residualCapacity[u][v]);
            }

            // Update residual capacities along the augmenting path
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residualCapacity[u][v] -= flow;
                residualCapacity[v][u] += flow;
            }

            // Add the flow to the maximum flow
            maxFlow += flow;

            // Record the augmented path
            augmentedPaths.add("Path: " + String.join(", ", path) + " | Respective Flow: " + flow);
        }

        repaint(); // Repaint the panel to update results
        return maxFlow; 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the original graph
        for (Edge edge : edges) {
            edge.draw(g);
        }
        for (Node node : nodes) {
            node.draw(g);
        }

        // Draw the augmented paths at the bottom of the panel
        g.setColor(Color.BLACK);
        int yOffset = getHeight() - 100;
        g.drawString("Augmented Paths:", 10, yOffset - 50);
        int y = yOffset - 30;
        for (String path : augmentedPaths) {
            g.drawString(path, 10, y);
            y += 15;
        }
    }
}

// Node class represents a vertex in the graph
class Node {
    private final String name;
    private final int x, y;
    private boolean selected;

    public Node(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.selected = false;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean contains(int px, int py) {
        return Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2)) < 20; // if px,py inside the node
    }

    public void draw(Graphics g) {
        g.setColor(selected ? Color.RED : Color.GREEN);
        g.fillOval(x - 15, y - 15, 30, 30);
        g.setColor(Color.BLACK); // for the label of node drawn
        g.drawString(name, x - 15, y - 20);
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

// Edge class represents a directed edge between two nodes
class Edge {
    private final Node from, to;
    private final int capacity;

    public Edge(Node from, Node to, int capacity) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        int x1 = from.getX(), y1 = from.getY();
        int x2 = to.getX(), y2 = to.getY();
        g.drawLine(x1, y1, x2, y2);

        // Draw arrowhead
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 20;// 30 pixels
        int xArrow1 = x2 - (int) (arrowSize * Math.cos(angle - Math.PI / 6));
        int yArrow1 = y2 - (int) (arrowSize * Math.sin(angle - Math.PI / 6));
        int xArrow2 = x2 - (int) (arrowSize * Math.cos(angle + Math.PI / 6));
        int yArrow2 = y2 - (int) (arrowSize * Math.sin(angle + Math.PI / 6));
        g.drawLine(x2, y2, xArrow1, yArrow1);
        g.drawLine(x2, y2, xArrow2, yArrow2);

        // Draw capacity on the edge
        g.drawString(String.valueOf(capacity), (x1 + x2) / 2, (y1 + y2) / 2);
    }
}