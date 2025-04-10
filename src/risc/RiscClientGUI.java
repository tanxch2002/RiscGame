package risc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RiscClientGUI extends JFrame {
    private JTextArea textArea;
    private MapPanel mapPanel;
    private JTextField inputField;
    private JButton sendButton;
    private JButton connectButton;

    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;

    private String host = "localhost";
    private int port = 12345;

    private static final Map<String, Color> playerColors = new HashMap<>();
    private static final Color[] defaultColors = {
            Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN
    };
    private static int colorIndex = 0;

    public RiscClientGUI() {
        super("RISC Client GUI");
        setupUI();
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private Color getColorForPlayer(String playerName) {
        if (playerName == null || playerName.equalsIgnoreCase("None")) {
            return Color.LIGHT_GRAY;
        }
        return playerColors.computeIfAbsent(playerName, k -> {
            Color assignedColor = defaultColors[colorIndex % defaultColors.length];
            colorIndex++;
            return assignedColor;
        });
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Host:"));
        JTextField hostField = new JTextField(host, 10);
        topPanel.add(hostField);
        topPanel.add(new JLabel("Port:"));
        JTextField portField = new JTextField(String.valueOf(port), 5);
        topPanel.add(portField);
        connectButton = new JButton("Connect");
        topPanel.add(connectButton);

        mapPanel = new MapPanel();
        JScrollPane mapScrollPane = new JScrollPane(mapPanel);
        mapScrollPane.setBorder(BorderFactory.createTitledBorder("Game Map"));

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane messageScrollPane = new JScrollPane(textArea);
        messageScrollPane.setBorder(BorderFactory.createTitledBorder("Server Messages"));
        messageScrollPane.setPreferredSize(new Dimension(200, 200));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                mapScrollPane, messageScrollPane);
        splitPane.setResizeWeight(0.7);
        splitPane.setOneTouchExpandable(true);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        inputField.setEnabled(false);
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        getContentPane().add(mainPanel);

        connectButton.addActionListener(e -> {
            this.host = hostField.getText().trim();
            try {
                this.port = Integer.parseInt(portField.getText().trim());
                if (!this.host.isEmpty() && this.port > 0) {
                    handleConnect();
                } else {
                    appendMessage("Please enter a valid host and port.\n");
                }
            } catch (NumberFormatException ex) {
                appendMessage("Invalid port number.\n");
            }
        });

        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSend();
            }
        };
        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);
    }

    /**
     * Initializes default map data based on MapPanel.territoryPositions.
     * This ensures the map is displayed even if the server does not send any map state.
     */
    private void initializeDefaultMapData() {
        Map<String, ClientTerritoryData> defaultData = new HashMap<>();
        for (String territoryName : MapPanel.territoryPositions.keySet()) {
            Point pos = MapPanel.territoryPositions.get(territoryName);
            ClientTerritoryData data = new ClientTerritoryData(territoryName, pos.x, pos.y);
            data.ownerName = "None";
            data.ownerColor = Color.LIGHT_GRAY;
            // Optionally, initialize default neighbors or units here.
            defaultData.put(territoryName, data);
        }
        SwingUtilities.invokeLater(() -> mapPanel.updateMapData(defaultData));
    }

    private void handleConnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                try { socket.close(); } catch (IOException ignored) {}
            }
            socket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new PrintWriter(socket.getOutputStream(), true);

            appendMessage("Connected to server: " + host + ":" + port + "\n");
            connectButton.setEnabled(false);
            sendButton.setEnabled(true);
            inputField.setEnabled(true);

            // Initialize default map data so that the map panel is not empty.
            initializeDefaultMapData();

            Thread readerThread = new Thread(this::readServerMessages);
            readerThread.start();

        } catch (IOException ex) {
            appendMessage("Connection failed: " + ex.getMessage() + "\n");
            connectButton.setEnabled(true);
            sendButton.setEnabled(false);
            inputField.setEnabled(false);
        }
    }

    private void readServerMessages() {
        // Pattern for parsing move order messages:
        Pattern moveOrderPattern = Pattern.compile("Move order added: level (\\d+) x (\\d+) from (\\w+) -> (\\w+)");
        try {
            String serverMsg;
            // Map parsing state variables.
            boolean readingMap = false;
            Map<String, ClientTerritoryData> currentMapParsedData = new HashMap<>();
            ClientTerritoryData currentTerritoryBeingParsed = null;

            // Regular expressions for parsing the map state.
            Pattern terrNameOwnerPattern = Pattern.compile("^(\\w+)\\s*\\(([^)]+)\\)");
            Pattern sizeNeighborsPattern = Pattern.compile("Size:\\s*(\\d+),\\s*Neighbors:\\s*(.*)");
            Pattern unitsPattern = Pattern.compile("Units:\\s*(.*)");
            Pattern unitDetailPattern = Pattern.compile("Level\\s*(\\d+):\\s*(\\d+)\\s*units");

            while ((serverMsg = serverIn.readLine()) != null) {
                // Check if this is a move order message.
                Matcher moveMatcher = moveOrderPattern.matcher(serverMsg);
                if (moveMatcher.find()) {
                    int level = Integer.parseInt(moveMatcher.group(1));
                    int numUnits = Integer.parseInt(moveMatcher.group(2));
                    String source = moveMatcher.group(3);
                    String dest = moveMatcher.group(4);
                    // For simplicity, use a dummy player ID (e.g., 0).
                    MoveOrder order = new MoveOrder(0, source, dest, level, numUnits);
                    // Add the move order to the map panel.
                    SwingUtilities.invokeLater(() -> mapPanel.addMoveOrder(order));
                    appendMessage(serverMsg + "\n");
                    continue;
                }

                // Start of map state marker.
                if (serverMsg.contains("===== Current Map State =====") ||
                        serverMsg.contains("Map state after") ||
                        serverMsg.contains("Current map state:")) {
                    readingMap = true;
                    currentMapParsedData.clear();
                    currentTerritoryBeingParsed = null;
                    appendMessage(serverMsg + "\n");
                    continue;
                }
                // End of map state marker.
                if (serverMsg.contains("=============================")) {
                    readingMap = false;
                    if (!currentMapParsedData.isEmpty()) {
                        final Map<String, ClientTerritoryData> dataToSend = new HashMap<>(currentMapParsedData);
                        SwingUtilities.invokeLater(() -> mapPanel.updateMapData(dataToSend));
                    }
                    appendMessage(serverMsg + "\n");
                    continue;
                }
                // Parsing map content.
                if (readingMap) {
                    serverMsg = serverMsg.trim();
                    Matcher nameOwnerMatcher = terrNameOwnerPattern.matcher(serverMsg);
                    Matcher sizeNeighborsMatcher = sizeNeighborsPattern.matcher(serverMsg);
                    Matcher unitsMatcher = unitsPattern.matcher(serverMsg);

                    if (nameOwnerMatcher.matches()) {
                        // Example line: "A (Player1)"
                        String name = nameOwnerMatcher.group(1);
                        String owner = nameOwnerMatcher.group(2);
                        Point pos = MapPanel.territoryPositions.getOrDefault(name, new Point(50, 50));
                        currentTerritoryBeingParsed = currentMapParsedData.computeIfAbsent(name,
                                k -> new ClientTerritoryData(k, pos.x, pos.y));
                        currentTerritoryBeingParsed.ownerName = owner;
                        currentTerritoryBeingParsed.ownerColor = getColorForPlayer(owner);
                        // Clear neighbors and units for fresh parsing.
                        currentTerritoryBeingParsed.neighborNames.clear();
                        currentTerritoryBeingParsed.units.clear();
                    } else if (sizeNeighborsMatcher.matches() && currentTerritoryBeingParsed != null) {
                        // Example line: "Size: 1, Neighbors: B C"
                        currentTerritoryBeingParsed.size = Integer.parseInt(sizeNeighborsMatcher.group(1));
                        String neighborsStr = sizeNeighborsMatcher.group(2).trim();
                        currentTerritoryBeingParsed.neighborNames.clear();
                        if (!neighborsStr.isEmpty()) {
                            currentTerritoryBeingParsed.neighborNames.addAll(Arrays.asList(neighborsStr.split("\\s+")));
                        }
                        // Update resource production based on size (example logic).
                        currentTerritoryBeingParsed.foodProduction = currentTerritoryBeingParsed.size;
                        currentTerritoryBeingParsed.techProduction = currentTerritoryBeingParsed.size;
                    } else if (unitsMatcher.matches() && currentTerritoryBeingParsed != null) {
                        // Example line: "Units: Level 0: 5 units; Level 1: 2 units" or "Units: No units"
                        String unitsDetails = unitsMatcher.group(1).trim();
                        currentTerritoryBeingParsed.units.clear();
                        if (!unitsDetails.equalsIgnoreCase("No units")) {
                            Matcher unitDetailMatcher = unitDetailPattern.matcher(unitsDetails);
                            while (unitDetailMatcher.find()) {
                                int level = Integer.parseInt(unitDetailMatcher.group(1));
                                int count = Integer.parseInt(unitDetailMatcher.group(2));
                                currentTerritoryBeingParsed.units.put(level, count);
                            }
                        }
                    }
                } else {
                    // Non-map messages are appended to the text area.
                    final String msgToAppend = serverMsg;
                    appendMessage(msgToAppend + "\n");
                }
            }
        } catch (IOException ex) {
            if (socket != null && !socket.isClosed()) {
                appendMessage("Disconnected from server.\n");
            }
        } catch (Exception e) {
            appendMessage("Error processing server messages: " + e.getMessage() + "\n");
            e.printStackTrace();
        } finally {
            SwingUtilities.invokeLater(() -> {
                connectButton.setEnabled(true);
                sendButton.setEnabled(false);
                inputField.setEnabled(false);
                if (socket != null && !socket.isClosed()) {
                    try { socket.close(); } catch (IOException ignored) {}
                }
            });
        }
    }

    private void handleSend() {
        String userMsg = inputField.getText().trim();
        if (userMsg.isEmpty() || serverOut == null || socket == null || socket.isClosed()) {
            return;
        }
        serverOut.println(userMsg);
        inputField.setText("");
    }

    private void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(msg);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Unable to set system look and feel: " + e);
        }
        SwingUtilities.invokeLater(() -> {
            RiscClientGUI clientGUI = new RiscClientGUI();
            clientGUI.setVisible(true);
        });
    }
}
