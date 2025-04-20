package risc;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

/**
 * Multiplayer game room server with optional DeepSeek AI player.
 */
public class RiscServer {
    private final String gameID;
    private final int desiredHumanPlayers;
    private final boolean includeAI;
    private final List<ClientHandler> clientHandlers;
    private final Game game;
    private AIController aiController;
    private static final String BOT_NAME = "DeepSeekBot";
    private boolean started = false;


    public RiscServer(int desiredHumanPlayers, String gameID, boolean includeAI) {
        this.desiredHumanPlayers = desiredHumanPlayers;
        this.gameID = gameID;
        this.includeAI = includeAI;
        this.clientHandlers = new ArrayList<>();
        this.game = new Game(this);
        int totalSlots = desiredHumanPlayers + (includeAI ? 1 : 0);
        game.setUpMap(totalSlots);
    }

    /* ================================================= */
    /*                  客户端接入                       */
    /* ================================================= */
    public synchronized void addNewClient(Socket socket, PlayerAccount account) {
        if (started) {
            System.out.println("Game already started, reject new player: " + account.getUsername());
            try { socket.close(); } catch (IOException ignored) {}
            return;
        }
        int newPlayerID = clientHandlers.size();
        ClientHandler ch = new ClientHandler(socket, this, newPlayerID, account);
        clientHandlers.add(ch);
        ch.start();
        synchronized (clientHandlers) { clientHandlers.notifyAll(); }
    }

    /* ================================================= */
    /*                   主循环启动                      */
    /* ================================================= */
    public void startServerLogic() {
        Thread t = new Thread(() -> {
            System.out.println("[RiscServer-" + gameID + "] waiting for "
                    + desiredHumanPlayers + " human players...");
            synchronized (clientHandlers) {
                while (clientHandlers.size() < desiredHumanPlayers) {
                    try { clientHandlers.wait(); } catch (InterruptedException e) {}
                }
            }
            started = true;
            System.out.println("[RiscServer-" + gameID + "] humans ready, adding AI bot...");
            startGame();
        });
        t.start();
    }

    /* ================================================= */
    /*                    游戏流程                       */
    /* ================================================= */
    private void startGame() {
        int totalPlayers = desiredHumanPlayers + (includeAI ? 1 : 0);
        game.initPlayers(totalPlayers);

        // Set human player names
        for (int i = 0; i < clientHandlers.size(); i++) {
            Player p = game.getPlayer(i);
            p.setName(clientHandlers.get(i).getAccount().getUsername());
        }

        // If AI is enabled, set up the bot
        if (includeAI) {
            int botID = totalPlayers - 1;
            Player prev = game.getPlayer(botID);
            AIPlayer bot = new AIPlayer(botID, BOT_NAME);
            for (Territory terr : prev.getTerritories()) {
                terr.setOwner(bot);
                bot.addTerritory(terr);
            }
            game.getAllPlayers().set(botID, bot);
            aiController = new AIController(game, bot);
            broadcastMessage("AI player [" + BOT_NAME + "] has joined the game!\n");
        }

        broadcastMessage("All players connected. " + (includeAI ? "Including AI. " : "")
                + "Each player has " + game.getInitialUnits() + " initial units.\n");
        broadcastMessage("每位玩家拥有 " + game.getInitialUnits() + " 个初始单位进行部署。\n");

        /* ---------- 初始布兵阶段 ---------- */
        gamePhaseInitialPlacement();

        /* ---------- 主回合循环 ---------- */
        while (!game.hasWinner()) {
            broadcastMessage("\n=== New Turn Begins ===\n");
            for (ClientHandler ch : clientHandlers) {
                Player p = game.getPlayer(ch.getPlayerID());
                if (p.isAlive()) {
                    ch.sendMessage("Your Stats - Level: " + p.getMaxTechLevel()
                            + ", Food: " + p.getFood()
                            + ", Tech: " + p.getTech());
                }
            }

            issueOrdersPhase();

            /* 执行顺序保持不变 */
            game.executeAllMoveOrders();
            game.executeAllAttackOrders();
            game.executeAllAlliances();
            game.executeAllUpgrades();

            /* ------------- 新增：广播 AI 资源 ------------- */
            Player aiPlayer = game.getAllPlayers()
                    .stream()
                    .filter(Player::isAI)
                    .findFirst()
                    .orElse(null);
            if (aiPlayer != null && aiPlayer.isAlive()) {
                broadcastMessage("DeepSeekBot 当前资源 -> Food: "
                        + aiPlayer.getFood() + ", Tech: " + aiPlayer.getTech());
            }
            /* ------------------------------------------- */

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

    /* ---------- 初始布兵 ---------- */
    private void gamePhaseInitialPlacement() {
        broadcastMessage("Initial placement phase starts...");
        List<Thread> threads = new ArrayList<>();

        /* 人类玩家线程 */
        for (ClientHandler ch : clientHandlers) {
            if (!game.getPlayer(ch.getPlayerID()).isAlive()) continue;
            Thread th = new Thread(() -> {
                ch.sendMessage("Please allocate initial units to your territories.");
                ch.collectInitialPlacement(game);
            });
            threads.add(th);
            th.start();
        }

        /* AI 玩家同步布兵（不占线程） */
        if (includeAI) {
            aiController.doInitialPlacement();
        }

        /* 等待人类完成 */
        for (Thread th : threads) {
            try { th.join(); } catch (InterruptedException ignored) {}
        }
        broadcastMessage("Initial placement completed.\nCurrent map state:\n" + game.getMapState());
    }

    /* ---------- 命令阶段 ---------- */
    private void issueOrdersPhase() {
        broadcastMessage("Enter command: (M)ove, (A)ttack, (U)pgrade, (T)ech, (D)one, (C)hat, (FA)lliance.\n");
        List<Thread> threads = new ArrayList<>();

        /* 人类玩家 */
        for (ClientHandler ch : clientHandlers) {
            if (!game.getPlayer(ch.getPlayerID()).isAlive()) continue;
            Thread th = new Thread(() -> {
                ch.sendMessage("It's your turn to issue orders...");
                ch.collectOrders(game);
            });
            threads.add(th);
            th.start();
        }

        /* 等人类全部完成，再让 AI 决策（便于 AI 知道对手动作） */
        for (Thread th : threads) {
            try { th.join(); } catch (InterruptedException ignored) {}
        }

        /* AI 出招 */
        if(includeAI && aiController != null) {
            aiController.generateTurnOrders();
        }

    }

    /* ---------- 移除阵亡人类玩家 ---------- */
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

    /* ---------- 广播 & 连接管理 ---------- */
    public void broadcastMessage(String msg) {
        for (ClientHandler ch : clientHandlers) {
            ch.sendMessage(msg);
        }
        System.out.println("[Broadcast-" + gameID + "]: " + msg);
    }

    public void closeAllConnections() {
        for (ClientHandler ch : clientHandlers) {
            try { ch.sendMessage("Connection closing..."); ch.closeConnection(); } catch (Exception ignored) {}
        }
    }

    public Game getGame() { return this.game; }
}
