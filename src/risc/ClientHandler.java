package risc;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * ClientHandler is responsible for communicating with a single client.
 * In the run() method, it sends a basic welcome message, and then further interacts
 * with the user via methods like collectOrders() and collectInitialPlacement().
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private final RiscServer server;
    private final int playerID;
    private final PlayerAccount account; // Added to represent the login account

    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, RiscServer server, int playerID, PlayerAccount account) {
        this.socket = socket;
        this.server = server;
        this.playerID = playerID;
        this.account = account;
    }

    public PlayerAccount getAccount() {
        return account;
    }

    public int getPlayerID() {
        return playerID;
    }

    @Override
    public void run() {
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Welcome, " + account.getUsername() + "! You are player #" + (playerID + 1));
            // Further processing for client handshake and commands can be implemented here.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a text message to the client.
     */
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    /**
     * Collects the player's commands for the current turn (Move/Attack/Upgrade/TechUpgrade/Done).
     */
    public void collectOrders(Game game) {
        try {
            while (true) {
                out.println("Enter an order (M/A/U/T/D): ");
                String line = in.readLine();
                if (line == null) {
                    break; // Client disconnected.
                }
                line = line.trim().toUpperCase();

                if (line.startsWith("M")) {
                    out.println("Enter format: sourceTerritory destinationTerritory level numUnits");
                    String argsLine = in.readLine();
                    if (argsLine == null) break;
                    processMoveOrder(argsLine, game);

                } else if (line.startsWith("A")) {
                    out.println("Enter format: sourceTerritory targetTerritory level numUnits");
                    String argsLine = in.readLine();
                    if (argsLine == null) break;
                    processAttackOrder(argsLine, game);

                } else if (line.startsWith("U")) {
                    out.println("Upgrade format: territory currentLevel targetLevel numUnits");
                    String argsLine = in.readLine();
                    if (argsLine == null) break;
                    processUpgradeOrder(argsLine, game);

                } else if (line.startsWith("T")) {
                    // Initiate maximum technology level upgrade.
                    processTechUpgradeOrder(game);

                } else if (line.startsWith("D")) {
                    out.println("All orders done for this turn.");
                    break;

                } else {
                    out.println("Invalid command, please try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processMoveOrder(String argsLine, Game game) {
        // Expected input format: sourceTerritory destinationTerritory level numUnits
        String[] parts = argsLine.split("\\s+");
        if (parts.length == 4) {
            try {
                String src = parts[0];
                String dest = parts[1];
                int level = Integer.parseInt(parts[2]);
                int units = Integer.parseInt(parts[3]);

                game.addOrder(new MoveOrder(playerID, src, dest, level, units));
                sendMessage("Move order added: level " + level + " x " + units +
                        " from " + src + " -> " + dest);
            } catch (NumberFormatException e) {
                sendMessage("Invalid number format for move order.");
            }
        } else {
            sendMessage("Invalid move order format. Expected 4 arguments.");
        }
    }

    private void processAttackOrder(String argsLine, Game game) {
        // Split the input.
        // Expected format: sourceTerritory targetTerritory level numUnits
        String[] parts = argsLine.split("\\s+");
        if (parts.length == 4) {
            try {
                String src = parts[0];
                String target = parts[1];
                int level = Integer.parseInt(parts[2]);
                int units = Integer.parseInt(parts[3]);

                // Pass the level parameter to the AttackOrder constructor
                game.addOrder(new AttackOrder(playerID, src, target, level, units));
                sendMessage("Attack order added: level " + level + " x " + units +
                        " from " + src + " => " + target);
            } catch (NumberFormatException e) {
                sendMessage("Invalid number format for attack order.");
            }
        } else {
            sendMessage("Invalid attack order format. Expected 4 arguments.");
        }
    }

    /**
     * Processes the unit upgrade command: UpgradeUnitOrder.
     */
    private void processUpgradeOrder(String argsLine, Game game) {
        // Expected input format: territoryName currentLevel targetLevel numUnits
        String[] parts = argsLine.split("\\s+");
        if (parts.length == 4) {
            try {
                String territory = parts[0];
                int currentLevel = Integer.parseInt(parts[1]);
                int targetLevel = Integer.parseInt(parts[2]);
                int numUnits = Integer.parseInt(parts[3]);
                game.addOrder(new UpgradeUnitOrder(playerID, territory, currentLevel, targetLevel, numUnits));
                sendMessage("Upgrade order added.");
            } catch (NumberFormatException e) {
                sendMessage("Invalid number format for upgrade order.");
            }
        } else {
            sendMessage("Invalid format for upgrade order.");
        }
    }

    /**
     * Processes the maximum technology upgrade command: TechUpgradeOrder.
     */
    private void processTechUpgradeOrder(Game game) {
        game.addOrder(new TechUpgradeOrder(playerID));
        sendMessage("Tech upgrade order added.");
    }

    /**
     * Collects the player's initial unit placement, assigning the initial units to the player's territories.
     */
    public void collectInitialPlacement(Game game) {
        int remainingUnits = game.getInitialUnits();
        Player player = game.getPlayer(playerID);
        List<Territory> territories = player.getTerritories();

        for (int i = 0; i < territories.size(); i++) {
            Territory t = territories.get(i);
            if (i < territories.size() - 1) {
                sendMessage("How many units to allocate to territory " + t.getName() +
                        "? (Remaining units: " + remainingUnits + ")");
                try {
                    String input = in.readLine();
                    int units = Integer.parseInt(input.trim());
                    if (units < 0 || units > remainingUnits) {
                        sendMessage("Invalid input, please enter a number between 0 and " + remainingUnits);
                        i--; // Retry
                        continue;
                    }
                    // For demonstration, allocate all as level 0 units.
                    t.addUnits(0, units);
                    remainingUnits -= units;
                } catch (IOException | NumberFormatException e) {
                    sendMessage("Error reading input, please re-enter.");
                    i--;
                }
            } else {
                // Automatically allocate the remaining units to the last territory.
                t.addUnits(0, remainingUnits);
                sendMessage("Territory " + t.getName() + " automatically allocated the remaining " + remainingUnits + " units.");
                remainingUnits = 0;
            }
        }
        sendMessage("Initial unit placement completed!");
    }

    /**
     * Closes the connection with the client.
     */
    public void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            socket.close();
        } catch (IOException e) {
            // Ignore exceptions during close.
        }
    }
}
