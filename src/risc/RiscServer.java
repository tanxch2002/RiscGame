package risc;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 多人游戏房间服务器。与 GlobalServer 配合:
 * - GlobalServer 接受连接并完成登录/注册以及选房
 * - RiscServer 只负责管理本游戏内的客户端和游戏逻辑
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
        // 根据玩家数量搭建地图
        this.game.setUpMap(desiredPlayers);
    }

    /**
     * 在后台启动该游戏的主要流程，例如：
     * 等待所有玩家到齐 -> 初始化玩家 -> 进入回合循环 -> 结束游戏
     */
    public void startServerLogic() {
        Thread t = new Thread(() -> {
            System.out.println("[RiscServer-" + gameID + "] is ready. Expecting " + desiredPlayers + " players.");
            synchronized (clientHandlers) {
                // 等待直到有足够数量的玩家加入
                while (clientHandlers.size() < desiredPlayers) {
                    try {
                        clientHandlers.wait(); // 没满就一直等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 所有玩家到齐
            started = true;
            System.out.println("[RiscServer-" + gameID + "] All players joined! Starting game...");
            startGame();
        });
        t.start();
    }

    /**
     * 由 GlobalServer 调用，将一个新客户端 socket + 账号 加入本游戏。
     */
    public synchronized void addNewClient(Socket socket, PlayerAccount account) {
        if (started) {
            // 如果游戏已经开始，是否允许新玩家加入，视规则而定
            System.out.println("Game already started, reject new player: " + account.getUsername());
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
            return;
        }
        int newPlayerID = clientHandlers.size(); // 当前已加入玩家数
        ClientHandler ch = new ClientHandler(socket, this, newPlayerID, account);
        clientHandlers.add(ch);
        ch.start();

        // 唤醒等待玩家加入的线程
        synchronized (clientHandlers) {
            clientHandlers.notifyAll();
        }
    }

    /**
     * 正式开始游戏，初始化玩家，执行回合流程
     */
    // … 省略前面的代码

    public void startGame() {
        // 1) 初始化玩家
        game.initPlayers(clientHandlers.size());

        // 将每个 Player 的名字设为其账号用户名
        for (int i = 0; i < clientHandlers.size(); i++) {
            Player p = game.getPlayer(i);
            p.setName(clientHandlers.get(i).getAccount().getUsername());
        }

        broadcastMessage("All players connected. Let the game begin.\n");
        broadcastMessage("You have " + game.getInitialUnits() + " units to place initially.\n");

        // 2) 初始安置
        gamePhaseInitialPlacement();

        // 3) 进入游戏主循环
        while (!game.hasWinner()) {
            broadcastMessage("\n=== New Turn Begins ===\n");

            // 新增：向每个客户端显示玩家状态（等级、食物、科技点数）
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

            // 4) 回合结束，处理资源产出、新单位生成、科技升级生效等
            game.endTurn();
            broadcastMessage("Map state after resources & new units:\n" + game.getMapState());

            // 5) 更新玩家状态、去除已无领土玩家
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
     * 演示性的初始单位安置流程，可并行地让所有玩家进行安置
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

        // 等待全部玩家安置完毕
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
     * 指令阶段，通知每个客户端输入指令
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

        // 等待全部玩家输入完毕
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行所有玩家输入的指令 (移动 / 攻击 / 升级等)
     */
    private void executeOrdersPhase() {
        broadcastMessage("Executing move orders...");
        game.executeAllMoveOrders();
        broadcastMessage("Executing attack orders...");
        game.executeAllAttackOrders();
        broadcastMessage("Executing unit/tech upgrades...");
        game.executeAllUpgrades();  // 需在 Game 或 OrderExecutor 中实现

        // 回合内全部指令执行完毕后清空
        game.clearAllOrders();

        broadcastMessage("Map state after order execution:\n" + game.getMapState());
    }

    /**
     * 删除没有领土的玩家 (被淘汰)
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
     * 向所有玩家广播消息
     */
    public void broadcastMessage(String msg) {
        for (ClientHandler ch : clientHandlers) {
            ch.sendMessage(msg);
        }
        System.out.println("[Broadcast-" + gameID + "]: " + msg);
    }

    /**
     * 关闭所有玩家连接
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

    // 可暴露 Getter
    public Game getGame() {
        return this.game;
    }
}
