package risc;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Each ClientHandler runs on its own thread to communicate with exactly one client.
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private final RiscServer server;
    private final int playerID;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, RiscServer server, int playerID) {
        this.socket = socket;
        this.server = server;
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return playerID;
    }

    @Override
    public void run() {
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("Welcome, Player " + (playerID + 1) + "!");
            // Additional client messages can be handled here if needed.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    /**
     * Continuously read orders from the client until they type "D" (Done).
     */
    public void collectOrders(Game game) {
        try {
            while (true) {
                out.println("Enter an order (M/A/D): ");
                String line = in.readLine();
                if (line == null) {
                    break; // client disconnected
                }
                line = line.trim().toUpperCase();
                if (line.startsWith("M")) {
                    out.println("Enter format: sourceTerritory destinationTerritory numUnits");
                    String argsLine = in.readLine();
                    if (argsLine == null) break;
                    processMoveOrder(argsLine, game);
                } else if (line.startsWith("A")) {
                    out.println("Enter format: sourceTerritory targetTerritory numUnits");
                    String argsLine = in.readLine();
                    if (argsLine == null) break;
                    processAttackOrder(argsLine, game);
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
        String[] parts = argsLine.split("\\s+");
        if (parts.length == 3) {
            try {
                String src = parts[0];
                String dest = parts[1];
                int units = Integer.parseInt(parts[2]);
                game.addOrder(new MoveOrder(playerID, src, dest, units));
                sendMessage("Move order added: " + src + " -> " + dest + " (" + units + " units)");
            } catch (NumberFormatException e) {
                sendMessage("Invalid number format for move order.");
            }
        } else {
            sendMessage("Invalid move order format.");
        }
    }

    private void processAttackOrder(String argsLine, Game game) {
        String[] parts = argsLine.split("\\s+");
        if (parts.length == 3) {
            try {
                String src = parts[0];
                String target = parts[1];
                int units = Integer.parseInt(parts[2]);
                game.addOrder(new AttackOrder(playerID, src, target, units));
                sendMessage("Attack order added: " + src + " => " + target + " (" + units + " units)");
            } catch (NumberFormatException e) {
                sendMessage("Invalid number format for attack order.");
            }
        } else {
            sendMessage("Invalid attack order format.");
        }
    }

    /**
     * Collect initial unit placement for each player's territories.
     * Each player must allocate game.getInitialUnits() units among their territories,
     * with the last territory receiving the remaining units automatically.
     */
    public void collectInitialPlacement(Game game) {
        int remainingUnits = game.getInitialUnits();
        Player player = game.getPlayer(playerID);
        List<Territory> territories = player.getTerritories();

        for (int i = 0; i < territories.size(); i++) {
            Territory t = territories.get(i);
            // If not the last territory, ask player for input
            if (i < territories.size() - 1) {
                sendMessage("How many units to allocate to territory " + t.getName() + "? (Remaining units: " + remainingUnits + ")");
                try {
                    String input = in.readLine();
                    int units = Integer.parseInt(input.trim());
                    if (units < 0 || units > remainingUnits) {
                        sendMessage("Invalid input, please enter a number between 0 and " + remainingUnits + ".");
                        i--; // Repeat the current territory input
                        continue;
                    }
                    t.setUnits(units);
                    remainingUnits -= units;
                } catch (IOException | NumberFormatException e) {
                    sendMessage("Error reading input, please re-enter.");
                    i--; // Repeat the current territory input
                }
            } else {
                // Automatically allocate remaining units to the last territory
                t.setUnits(remainingUnits);
                sendMessage("Territory " + t.getName() + " automatically allocated the remaining " + remainingUnits + " units.");
                remainingUnits = 0;
            }
        }
        sendMessage("Initial unit placement completed!");
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            // Ignore exceptions on close.
        }
    }
}
