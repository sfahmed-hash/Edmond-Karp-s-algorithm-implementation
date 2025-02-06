import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class EdmondsKarpGUI2 {

    static final int INF = Integer.MAX_VALUE;
    private static final int RADIUS = 20; // Radius of vertices for GUI
    private static Map<Integer, Point> vertexPositions;
    private static JFrame frame;
    private static GraphPanel graphPanel;
    private static JLabel maxFlowLabel;
    private static JTextArea augmentingPathsTextArea;

    // Function to implement BFS and find an augmenting path
    private static boolean bfs(int[][] capacity, int[][] flow, int source, int sink, int[] parent) {
        int n = capacity.length;
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {
            int current = queue.poll();

            for (int next = 0; next < n; next++) {
                if (!visited[next] && capacity[current][next] - flow[current][next] > 0) {
                    queue.add(next);
                    parent[next] = current;
                    visited[next] = true;

                    if (next == sink) return true; // Sink reached
                }
            }
        }
        return false; // No augmenting path found
    }

    // Function to implement Edmonds-Karp algorithm
    public static int edmondsKarp(int[][] capacity, int source, int sink) {
        int n = capacity.length;
        int[][] flow = new int[n][n]; // Residual flow
        int maxFlow = 0;
        int[] parent = new int[n]; // To store the path

        while (bfs(capacity, flow, source, sink, parent)) {
            int pathFlow = INF;
            int v = sink;
            List<int[]> pathEdges = new ArrayList<>();

            while (v != source) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, capacity[u][v] - flow[u][v]);
                pathEdges.add(new int[]{u, v});
                v = u;
            }

            // Update residual capacities
            v = sink;
            while (v != source) {
                int u = parent[v];
                flow[u][v] += pathFlow;
                flow[v][u] -= pathFlow;
                v = u;
            }

            maxFlow += pathFlow;
            updateGraph(capacity, flow, pathEdges, maxFlow);
        }

        return maxFlow;
    }

    private static void initializeGUI() {
        vertexPositions = new HashMap<>();

        frame = new JFrame("Edmonds-Karp Algorithm Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 700);
        frame.setLayout(new BorderLayout());

        graphPanel = new GraphPanel();
        frame.add(graphPanel, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));

        JLabel verticesLabel = new JLabel("Number of vertices:");
        JTextField verticesField = new JTextField();
        JLabel edgesLabel = new JLabel("Number of edges:");
        JTextField edgesField = new JTextField();

        JButton submitButton = new JButton("Submit");

        inputPanel.add(verticesLabel);
        inputPanel.add(verticesField);
        inputPanel.add(edgesLabel);
        inputPanel.add(edgesField);
        inputPanel.add(submitButton);

        frame.add(inputPanel, BorderLayout.NORTH);

        maxFlowLabel = new JLabel("Max Flow: 0", SwingConstants.CENTER);
        maxFlowLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(maxFlowLabel, BorderLayout.SOUTH);

        // Create text area to display augmenting paths
        augmentingPathsTextArea = new JTextArea(20, 30);
        augmentingPathsTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(augmentingPathsTextArea);
        frame.add(scrollPane, BorderLayout.EAST);

        submitButton.addActionListener(e -> {
            int vertices = Integer.parseInt(verticesField.getText());
            int edges = Integer.parseInt(edgesField.getText());

            int[][] capacity = new int[vertices][vertices];
            vertexPositions.clear();

            for (int i = 0; i < vertices; i++) {
                int x = (int) (400 + 250 * Math.cos(2 * Math.PI * i / vertices));
                int y = (int) (350 + 250 * Math.sin(2 * Math.PI * i / vertices));
                vertexPositions.put(i, new Point(x, y));
            }

            JPanel edgesPanel = new JPanel(new GridLayout(edges + 1, 3));
            edgesPanel.add(new JLabel("From"));
            edgesPanel.add(new JLabel("To"));
            edgesPanel.add(new JLabel("Capacity"));

            JTextField[] fromFields = new JTextField[edges];
            JTextField[] toFields = new JTextField[edges];
            JTextField[] capFields = new JTextField[edges];

            for (int i = 0; i < edges; i++) {
                fromFields[i] = new JTextField();
                toFields[i] = new JTextField();
                capFields[i] = new JTextField();

                edgesPanel.add(fromFields[i]);
                edgesPanel.add(toFields[i]);
                edgesPanel.add(capFields[i]);
            }

            int source = Integer.parseInt(JOptionPane.showInputDialog("Enter source vertex:"));
            int sink = Integer.parseInt(JOptionPane.showInputDialog("Enter sink vertex:"));

            JButton startButton = new JButton("Start");
            startButton.addActionListener(a -> {
                for (int i = 0; i < edges; i++) {
                    int from = Integer.parseInt(fromFields[i].getText());
                    int to = Integer.parseInt(toFields[i].getText());
                    int cap = Integer.parseInt(capFields[i].getText());
                    capacity[from][to] = cap;
                }
                edmondsKarp(capacity, source, sink);
            });

            // Clear previous inputs and show the updated GUI
            frame.getContentPane().removeAll();
            frame.add(graphPanel, BorderLayout.CENTER);
            frame.add(edgesPanel, BorderLayout.NORTH);
            frame.add(maxFlowLabel, BorderLayout.SOUTH);  // Re-add max flow label
            frame.add(scrollPane, BorderLayout.EAST);  // Re-add augmenting paths scroll pane

            // Using BorderLayout.PAGE_END for the Start button to avoid overlap
            frame.add(startButton, BorderLayout.PAGE_END);
            frame.revalidate();
            frame.repaint();
        });

        frame.setVisible(true);
    }

    // Function to update the graph dynamically
    private static void updateGraph(int[][] capacity, int[][] flow, List<int[]> pathEdges, int maxFlow) {
        SwingUtilities.invokeLater(() -> {
            graphPanel.setGraphData(capacity, flow, pathEdges);
            maxFlowLabel.setText("Max Flow: " + maxFlow);

            // Append augmenting path to the text area instead of resetting
            augmentingPathsTextArea.append("Augmenting Path:\n");
            for (int[] edge : pathEdges) {
                augmentingPathsTextArea.append("From " + edge[0] + " to " + edge[1] + "\n");
            }

            graphPanel.repaint();
        });
    }

    // Main method
    public static void main(String[] args) {
        initializeGUI();
    }

    // GraphPanel class for drawing the graph
    static class GraphPanel extends JPanel {
        private int[][] capacity;
        private int[][] flow;
        private List<int[]> pathEdges;

        public void setGraphData(int[][] capacity, int[][] flow, List<int[]> pathEdges) {
            this.capacity = capacity;
            this.flow = flow;
            this.pathEdges = pathEdges;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Draw edges with capacities and flows
            if (capacity != null) {
                for (int u = 0; u < capacity.length; u++) {
                    for (int v = 0; v < capacity.length; v++) {
                        if (capacity[u][v] > 0) {
                            Point p1 = vertexPositions.get(u);
                            Point p2 = vertexPositions.get(v);
                            g2d.setColor(Color.BLACK);
                            g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
                            String label = flow[u][v] + "/" + capacity[u][v];
                            g2d.setColor(Color.BLUE);
                            g2d.drawString(label, (p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
                        }
                    }
                }
            }

            // Highlight augmenting path
            if (pathEdges != null) {
                g2d.setColor(Color.RED);
                for (int[] edge : pathEdges) {
                    Point p1 = vertexPositions.get(edge[0]);
                    Point p2 = vertexPositions.get(edge[1]);
                    g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
                }
            }

            // Draw vertices
            for (Map.Entry<Integer, Point> entry : vertexPositions.entrySet()) {
                int vertex = entry.getKey();
                Point p = entry.getValue();
                g2d.setColor(Color.BLUE);
                g2d.fillOval(p.x - RADIUS / 2, p.y - RADIUS / 2, RADIUS, RADIUS);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(vertex), p.x - 5, p.y + 5);
            }
        }
    }
}
