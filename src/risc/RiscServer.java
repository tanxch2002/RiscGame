package risc;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Multiplayer game room server.
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
        this.game = new Game(this);
        this.game.setUpMap(desiredPlayers);
    }

    public void startServerLogic() {
        Thread t = new Thread(() -> {
            System.out.println("[RiscServer-" + gameID + "] is ready. Expecting " + desiredPlayers + " players.");
            synchronized (clientHandlers) {
                while (clientHandlers.size() < desiredPlayers) {
                    try {
                        clientHandlers.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            started = true;
            System.out.println("[RiscServer-" + gameID + "] All players joined! Starting game...");
            startGame();
        });
        t.start();
    }

    public synchronized void addNewClient(Socket socket, PlayerAccount account) {
        if (started) {
            System.out.println("Game already started, reject new player: " + account.getUsername());
            try {
                socket.close();
            } catch (IOException e) {
            }
            return;
        }
        int newPlayerID = clientHandlers.size();
        ClientHandler ch = new ClientHandler(socket, this, newPlayerID, account);
        clientHandlers.add(ch);
        ch.start();
        synchronized (clientHandlers) {
            clientHandlers.notifyAll();
        }
    }

    public void startGame() {
        // 初始化玩家
        game.initPlayers(clientHandlers.size());

        // 设置玩家名字
        for (int i = 0; i < clientHandlers.size(); i++) {
            Player p = game.getPlayer(i);
            p.setName(clientHandlers.get(i).getAccount().getUsername());
        }

        broadcastMessage("All players connected. Let the game begin.\n");
        broadcastMessage("You have " + game.getInitialUnits() + " units to place initially.\n");

        gamePhaseInitialPlacement();

        while (!game.hasWinner()) {
            broadcastMessage("\n=== New Turn Begins ===\n");

            for (ClientHandler ch : clientHandlers) {
                Player p = game.getPlayer(ch.getPlayerID());
                if (p.isAlive()) {
                    ch.sendMessage("Your Stats - Level: " + p.getMaxTechLevel() +
                            ", Food: " + p.getFood() +
                            ", Tech: " + p.getTech());
                }
            }

            issueOrdersPhase();
            // 依次执行
            game.executeAllMoveOrders();
            game.executeAllAttackOrders();
            game.executeAllAlliances();  // NEW: 结盟处理
            game.executeAllUpgrades();
            game.clearAllOrders();

            game.endTurn();
            broadcastMessage("Map state after endTurn:\n" + game.getMapState());

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

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        broadcastMessage("Initial placement completed.\nCurrent map state:\n" + game.getMapState());
    }

    private void issueOrdersPhase() {
        broadcastMessage("Enter command: (M)ove, (A)ttack, (U)pgrade, (T)ech, (D)one, (C)hat, (FA)lliance.\n");
        List<Thread> threads = new ArrayList<>();

        for (ClientHandler ch : clientHandlers) {
            if (!game.getPlayer(ch.getPlayerID()).isAlive()) continue;
            Thread t = new Thread(() -> {
                ch.sendMessage("It's your turn to issue orders...");
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

    public void broadcastMessage(String msg) {
        for (ClientHandler ch : clientHandlers) {
            ch.sendMessage(msg);
        }
        System.out.println("[Broadcast-" + gameID + "]: " + msg);
    }

    public void closeAllConnections() {
        for (ClientHandler ch : clientHandlers) {
            try {
                ch.sendMessage("Connection closing...");
                ch.closeConnection();
            } catch (Exception e) {
            }
        }
    }

    public Game getGame() {
        return this.game;
    }
}
