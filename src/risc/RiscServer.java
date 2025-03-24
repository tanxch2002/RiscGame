package risc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal RISC Server.
 * Listens for new client connections, starts the game when all players have joined.
 */
public class RiscServer {
    public static final int PORT = 12345;   // example port
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 2;

    private final List<ClientHandler> clientHandlers;
    private final Game game;

    public RiscServer() {
        this.clientHandlers = new ArrayList<>();
        // You may create a default map, or wait until players connect and choose a map config
        this.game = new Game();
        // For demonstration, we'll just create some fixed territories and adjacency.
        this.game.setupDefaultMap();
    }

    public static void main(String[] args) {
        RiscServer server = new RiscServer();
        server.runServer();
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);
            while (clientHandlers.size() < MAX_PLAYERS) {
                System.out.println("Waiting for clients...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());

                ClientHandler handler = new ClientHandler(socket, this, clientHandlers.size());
                clientHandlers.add(handler);
                handler.start();

                // If we have at least the minimum required players, we can start the game
                if (clientHandlers.size() == MIN_PLAYERS) {
                    // Optionally wait a few seconds for more players or just start
                    System.out.println("Minimum players reached. You can start the game now or wait for more...");
                }
                if (clientHandlers.size() == MAX_PLAYERS) {
                    System.out.println("Max players reached.");
                    break;
                }
            }
            // Start the game logic once we decide we have enough players
            startGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Once all players are connected, set up the game and run the main loop.
     */
    private void startGame() {
        // Initialize the game with the number of players
        game.initPlayers(clientHandlers.size());

        // Step 1: initial unit placement (all players do it simultaneously)
        broadcastMessage("All players: Place your initial units. You each have "
                + game.getInitialUnits() + " units total.\n");
        gamePhaseInitialPlacement();

        // Step 2: main game loop
        while (!game.hasWinner()) {
            broadcastMessage("\n=== New Turn ===\n");
            issueOrdersPhase();
            executeOrdersPhase();

            // end of turn: add 1 unit to each territory
            game.addOneUnitToEachTerritory();

            // check defeat/win conditions
            game.updatePlayerStatus();
            if (game.hasWinner()) {
                broadcastMessage("We have a winner: " + game.getWinner().getName() + "\n");
                break;
            }
        }
        broadcastMessage("Game Over.\n");
        // optionally: close all client connections, or allow a new game
        closeAllConnections();
    }

    /**
     * Ask each client to place initial units.
     * We assume the server receives how many units each territory gets,
     * and the server enforces correctness.
     */
    private void gamePhaseInitialPlacement() {
        // In a real game, you'd gather each player's distribution,
        // but for minimal code, we'll simply assign them evenly or forcibly
        // distribute them for demonstration:
        game.distributeInitialUnits();
        broadcastMessage("All initial units placed.\nCurrent Map:\n" + game.getMapState());
    }

    /**
     * Issue orders phase: each player sends in all their orders
     * (move, attack) until they type "D" (done).
     */
    private void issueOrdersPhase() {
        broadcastMessage("Enter your orders: (M)ove, (A)ttack, (D)one.\n");
        for (ClientHandler ch : clientHandlers) {
            // If the player's already lost, skip
            if (!game.getPlayer(ch.getPlayerID()).isAlive()) {
                continue;
            }
            // Request orders from that player
            ch.sendMessage("Your turn to enter orders. Type them line by line.\n");
            ch.collectOrders(game);
        }
    }

    /**
     * Execute the orders phase: moves first, then attacks.
     */
    private void executeOrdersPhase() {
        // Move orders first
        broadcastMessage("\nExecuting Move Orders...\n");
        game.executeAllMoveOrders();

        // Attack orders second
        broadcastMessage("\nExecuting Attack Orders...\n");
        game.executeAllAttackOrders();

        // Clear all orders
        game.clearAllOrders();

        // Show updated map
        broadcastMessage("Map after this turn:\n" + game.getMapState());
    }

    /**
     * Utility method to broadcast a message to all clients.
     */
    public void broadcastMessage(String msg) {
        for (ClientHandler ch : clientHandlers) {
            ch.sendMessage(msg);
        }
        // Also log to server console
        System.out.println("Broadcast: " + msg);
    }

    public void closeAllConnections() {
        for (ClientHandler ch : clientHandlers) {
            try {
                ch.sendMessage("Closing connection...\n");
                ch.closeConnection();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public Game getGame() {
        return this.game;
    }
}
