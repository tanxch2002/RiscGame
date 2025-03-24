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
            // You could wait for further messages from client here if needed.
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
                    // parse move: e.g. "M sourceTerr destinationTerr numUnits"
                    // For minimal code, assume user typed correct format
                    out.println("Enter format: sourceTerritory destinationTerritory numUnits");
                    String argsLine = in.readLine();
                    if (argsLine == null) break;
                    String[] parts = argsLine.split("\\s+");
                    if (parts.length == 3) {
                        String src = parts[0];
                        String dest = parts[1];
                        int units = Integer.parseInt(parts[2]);
                        game.addOrder(new MoveOrder(playerID, src, dest, units));
                        out.println("Move order added: " + src + " -> " + dest + " (" + units + " units)");
                    }
                }
                else if (line.startsWith("A")) {
                    // parse attack: "A sourceTerr targetTerr numUnits"
                    out.println("Enter format: sourceTerritory targetTerritory numUnits");
                    String argsLine = in.readLine();
                    if (argsLine == null) break;
                    String[] parts = argsLine.split("\\s+");
                    if (parts.length == 3) {
                        String src = parts[0];
                        String target = parts[1];
                        int units = Integer.parseInt(parts[2]);
                        game.addOrder(new AttackOrder(playerID, src, target, units));
                        out.println("Attack order added: " + src + " => " + target + " (" + units + " units)");
                    }
                }
                else if (line.startsWith("D")) {
                    out.println("All orders done for this turn.");
                    break;
                }
                else {
                    out.println("Invalid command, please try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
