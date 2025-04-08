package risc;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Multiplayer game room server. Works in conjunction with the GlobalServer:
 * - GlobalServer accepts connections and handles login/registration as well as room selection.
 * - RiscServer is solely responsible for managing the clients within the game and the game logic.
 */
public class RiscServer {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 5;

    private final String gameID;
    private final int desiredPlayers;
    private final List<ClientHandler> clientHandlers;
    private final Game game;

    private boolean started = false;

    public RiscServer(int desiredPlayers, String gameID) {
        this.desiredPlayers = desiredPlayers;
        this.gameID = gameID;
        this.clientHandlers = new ArrayList<>();
        this.game = new Game();
        // Set up the game map based on the number of players
        this.game.setUpMap(desiredPlayers);
    }

    /**
     * Starts the main game flow in the background, e.g.:
     * Waiting for all players to join -> Initialize players -> Enter the turn loop -> End game
     */
    public void startServerLogic() {
        Thread t = new Thread(() -> {
            System.out.println("[RiscServer-" + gameID + "] is ready. Expecting " + desiredPlayers + " players.");
            synchronized (clientHandlers) {
                // Wait until a sufficient number of players have joined
                while (clientHandlers.size() < desiredPlayers) {
                    try {
                        clientHandlers.wait(); // Wait if the room is not full
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // All players have joined
            started = true;
            System.out.println("[RiscServer-" + gameID + "] All players joined! Starting game...");
            startGame();
        });
        t.start();
    }

    /**
     * Called by GlobalServer to add a new client socket and account to this game.
     */
    public synchronized void addNewClient(Socket socket, PlayerAccount account) {
        if (started) {
            // If the game has already started, whether to allow new players depends on the rules
            System.out.println("Game already started, reject new player: " + account.getUsername());
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
            return;
        }
        int newPlayerID = clientHandlers.size(); // Current number of joined players
        ClientHandler ch = new ClientHandler(socket, this, newPlayerID, account);
        clientHandlers.add(ch);
        ch.start();

        // Wake up threads waiting for players to join
        synchronized (clientHandlers) {
            clientHandlers.notifyAll();
        }
    }

    /**
     * Officially starts the game by initializing players and executing the turn loop.
     */
    public void startGame() {
        // 1) Initialize players
        game.initPlayers(clientHandlers.size());

        // Set each Player's name to the corresponding account's username
        for (int i = 0; i < clientHandlers.size(); i++) {
            Player p = game.getPlayer(i);
            p.setName(clientHandlers.get(i).getAccount().getUsername());
        }

        broadcastMessage("All players connected. Let the game begin.\n");
        broadcastMessage("You have " + game.getInitialUnits() + " units to place initially.\n");

        // 2) Initial placement phase
        gamePhaseInitialPlacement();

        // 3) Enter the main game loop
        while (!game.hasWinner()) {
            broadcastMessage("\n=== New Turn Begins ===\n");

            // New: Display each player's status (level, food, tech points) to them
            for (ClientHandler ch : clientHandlers) {
                Player p = game.getPlayer(ch.getPlayerID());
                if (p.isAlive()) {
                    ch.sendMessage("Your Stats - Level: " + p.getMaxTechLevel() +
                            ", Food: " + p.getFood() +
                            ", Tech: " + p.getTech());
                }
            }

            issueOrdersPhase();
            executeOrdersPhase();

            // 4) End of turn: handle resource production, new units generation, and tech upgrades
            game.endTurn();
            broadcastMessage("Map state after resources & new units:\n" + game.getMapState());

            // 5) Update player statuses and remove players with no territories
            game.updatePlayerStatus();
            removeDeadPlayers();

            if (game.hasWinner()) {
                broadcastMessage("Game over! Winner is: " + game.getWinner().getName() + "\n");
                break;
            }
        }

        broadcastMessage("Game over.\n");
        closeAllConnections();
    }

    /**
     * Demonstrative initial unit placement phase where all players can place units concurrently.
     */
    private void gamePhaseInitialPlacement() {
        broadcastMessage("Initial placement phase starts...");
        List<Thread> threads = new ArrayList<>();

        for (ClientHandler ch : clientHandlers) {
            if (!game.getPlayer(ch.getPlayerID()).isAlive()) continue;
            Thread t = new Thread(() -> {
                ch.sendMessage("Please allocate initial units to your territories.");
                ch.collectInitialPlacement(game);
            });
            threads.add(t);
            t.start();
        }

        // Wait for all players to complete their placement
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        broadcastMessage("Initial placement completed.\nCurrent map state:\n" + game.getMapState());
    }

    /**
     * Order phase: notifies each client to input their commands.
     */
    private void issueOrdersPhase() {
        broadcastMessage("Enter command: (M)ove, (A)ttack, (U)pgrade unit, (T)ech upgrade, (D)one.\n");
        List<Thread> threads = new ArrayList<>();

        for (ClientHandler ch : clientHandlers) {
            if (!game.getPlayer(ch.getPlayerID()).isAlive()) continue;
            Thread t = new Thread(() -> {
                ch.sendMessage("It's your turn to issue orders, please enter them sequentially.\n");
                ch.collectOrders(game);
            });
            threads.add(t);
            t.start();
        }

        // Wait for all players to finish inputting their orders
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Execute all commands input by the players (move, attack, upgrade, etc.).
     */
    private void executeOrdersPhase() {
        broadcastMessage("Executing move orders...");
        game.executeAllMoveOrders();
        broadcastMessage("Executing attack orders...");
        game.executeAllAttackOrders();
        broadcastMessage("Executing unit/tech upgrades...");
        game.executeAllUpgrades();  // To be implemented in Game or OrderExecutor

        // Clear all orders after execution within the turn
        game.clearAllOrders();

        broadcastMessage("Map state after order execution:\n" + game.getMapState());
    }

    /**
     * Removes players who no longer control any territories (eliminated).
     */
    private void removeDeadPlayers() {
        Iterator<ClientHandler> it = clientHandlers.iterator();
        while (it.hasNext()) {
            ClientHandler ch = it.next();
            if (!game.getPlayer(ch.getPlayerID()).isAlive()) {
                ch.sendMessage("You have been eliminated! Connection will close.");
                ch.closeConnection();
                it.remove();
            }
        }
    }

    /**
     * Broadcasts a message to all players.
     */
    public void broadcastMessage(String msg) {
        for (ClientHandler ch : clientHandlers) {
            ch.sendMessage(msg);
        }
        System.out.println("[Broadcast-" + gameID + "]: " + msg);
    }

    /**
     * Closes all connections to players.
     */
    public void closeAllConnections() {
        for (ClientHandler ch : clientHandlers) {
            try {
                ch.sendMessage("Connection closing...");
                ch.closeConnection();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    // Getter method for the Game instance
    public Game getGame() {
        return this.game;
    }
}
