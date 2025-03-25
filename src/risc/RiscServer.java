package risc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Iterator; // 引入 Iterator 用于安全移除

/**
 * Minimal RISC Server, allowing 2 to 5 players.
 * Listens for new client connections and starts the game when the specified number of players have joined.
 */
public class RiscServer {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 5;
    public static final int PORT = 12345;

    private final List<ClientHandler> clientHandlers;
    private final Game game;
    private final int desiredPlayers;

    /**
     * Constructor with desired number of players.
     */
    public RiscServer(int desiredPlayers) {
        this.clientHandlers = new ArrayList<>();
        this.game = new Game();
        this.game.setUpMap(desiredPlayers);
        this.desiredPlayers = desiredPlayers;
    }

    /**
     * Main method: prompts for number of players before starting the server.
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int numPlayers = 0;
        while (numPlayers < MIN_PLAYERS || numPlayers > MAX_PLAYERS) {
            System.out.println("Please enter the number of players for this game (" + MIN_PLAYERS + "-" + MAX_PLAYERS + "):");
            String line = sc.nextLine().trim();
            try {
                numPlayers = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                numPlayers = 0;
            }
        }
        sc.close();

        RiscServer server = new RiscServer(numPlayers);
        server.runServer();
    }

    /**
     * Main logic: wait for players to connect until desiredPlayers have joined, then start the game.
     */
    public void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for " + this.desiredPlayers + " players to connect...");

            while (clientHandlers.size() < desiredPlayers) {
                Socket socket = serverSocket.accept();
                System.out.println("Player connected: " + socket.getInetAddress());
                ClientHandler handler = new ClientHandler(socket, this, clientHandlers.size());
                clientHandlers.add(handler);
                handler.start();

                if (clientHandlers.size() == desiredPlayers) {
                    System.out.println("Desired player count (" + desiredPlayers + ") reached, starting game.");
                    break;
                }
            }
            startGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Once all players have connected, initialize the game and start the main loop.
     */
    private void startGame() {
        game.initPlayers(clientHandlers.size());
        broadcastMessage("All players begin initial unit placement. Each player has " + game.getInitialUnits() + " units to allocate.\n");
        gamePhaseInitialPlacement();

        while (!game.hasWinner()) {
            broadcastMessage("\n=== New Turn Begins ===\n");
            issueOrdersPhase();
            executeOrdersPhase();
            game.updatePlayerStatus();

            // 遍历所有客户端处理器，关闭已被淘汰玩家的连接，并从列表中移除
            Iterator<ClientHandler> iterator = clientHandlers.iterator();
            while (iterator.hasNext()) {
                ClientHandler ch = iterator.next();
                if (!game.getPlayer(ch.getPlayerID()).isAlive()) {
                    ch.sendMessage("You have been eliminated! Your connection will now be closed.");
                    ch.closeConnection();
                    iterator.remove();
                }
            }

            if (game.hasWinner()) {
                broadcastMessage("Game over! The winner is: " + game.getWinner().getName() + "\n");
                break;
            }
        }
        broadcastMessage("Game over.\n");
        closeAllConnections();
    }

    /**
     * Initial unit placement phase: each player manually allocates units to their territories.
     */
    private void gamePhaseInitialPlacement() {
        broadcastMessage("Initial unit placement phase: Each player has " + game.getInitialUnits() + " units, please allocate them to your territories accordingly.");
        List<Thread> threads = new ArrayList<>();

        for (ClientHandler ch : clientHandlers) {
            if (!game.getPlayer(ch.getPlayerID()).isAlive()) {
                continue;
            }
            Thread t = new Thread(() -> {
                ch.sendMessage("Please allocate initial units to each of your territories.");
                ch.collectInitialPlacement(game);
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        broadcastMessage("Initial unit placement completed.\nCurrent map state:\n" + game.getMapState());
    }

    /**
     * Order issuing phase: each player enters orders (M)ove, (A)ttack, (D)one.
     */
    private void issueOrdersPhase() {
        broadcastMessage("Enter command: (M)ove, (A)ttack, (D)one.\n");
        List<Thread> threads = new ArrayList<>();

        for (ClientHandler ch : clientHandlers) {
            if (!game.getPlayer(ch.getPlayerID()).isAlive()) {
                continue;
            }
            Thread t = new Thread(() -> {
                ch.sendMessage("It's your turn to issue orders, please enter them sequentially.\n");
                ch.collectOrders(game);
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Order execution phase: first execute move orders, then attack orders.
     */
    private void executeOrdersPhase() {
        broadcastMessage("\nExecuting move orders...\n");
        game.executeAllMoveOrders();
        broadcastMessage("\nExecuting attack orders...\n");
        game.executeAllAttackOrders();
        game.clearAllOrders();
        game.addOneUnitToEachTerritory();
        broadcastMessage("Map state after this turn:\n" + game.getMapState());
    }

    /**
     * Broadcast a message to all players.
     */
    public void broadcastMessage(String msg) {
        for (ClientHandler ch : clientHandlers) {
            ch.sendMessage(msg);
        }
        System.out.println("Broadcast: " + msg);
    }

    /**
     * Close connections with all players.
     */
    public void closeAllConnections() {
        for (ClientHandler ch : clientHandlers) {
            try {
                ch.sendMessage("Connection is closing...\n");
                ch.closeConnection();
            } catch (Exception e) {
                // Ignore exceptions on close.
            }
        }
    }

    /**
     * Expose the current game object.
     */
    public Game getGame() {
        return this.game;
    }
}
