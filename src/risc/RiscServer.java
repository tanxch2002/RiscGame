package risc;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

/**
 * Multiplayer game room server with optional DeepSeek AI player.
 */
public class RiscServer {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 5;

    private final String gameID;
    private final int   desiredHumanPlayers; // 人类玩家数量
    private final List<ClientHandler> clientHandlers;
    private final Game  game;

    /* --- 新增: AI --- */
    private AIController aiController;   // 只有 1 个 Bot，如需多个可用列表
    private static final String BOT_NAME = "DeepSeekBot";

    private boolean started = false;

    public RiscServer(int desiredHumanPlayers, String gameID) {
        this.desiredHumanPlayers   = desiredHumanPlayers;
        this.gameID = gameID;
        this.clientHandlers = new ArrayList<>();
        this.game = new Game(this);
        this.game.setUpMap(desiredHumanPlayers + 1); // 预留 1 个 AI
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
        /* ---------- 创建玩家（人类 + AI） ---------- */
        int totalPlayers = clientHandlers.size() + 1;              // +1 AI
        game.initPlayers(totalPlayers);

        /* 设置人类玩家名字 */
        for (int i = 0; i < clientHandlers.size(); i++) {
            Player p = game.getPlayer(i);
            p.setName(clientHandlers.get(i).getAccount().getUsername());
        }

        /* ---------- 创建 AIPlayer 并接管最后一个玩家的领地 ---------- */
        int botID = totalPlayers - 1;
        Player prev = game.getPlayer(botID);          // 旧的占位玩家
        AIPlayer bot = new AIPlayer(botID, BOT_NAME);

        /* 把领地转给 bot */
        for (Territory terr : prev.getTerritories()) {
            terr.setOwner(bot);        // 更新地块归属
            bot.addTerritory(terr);    // 加入 bot 的领地清单
        }
        /* 用 bot 替换 players 列表中的最后一位 */
        game.getAllPlayers().set(botID, bot);

        /* 为 AI 创建控制器 */
        aiController = new AIController(game, bot);


        broadcastMessage("All players connected. AI 玩家 [" + BOT_NAME + "] 加入战局！\n");
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
        aiController.doInitialPlacement();

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
        aiController.generateTurnOrders();
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
