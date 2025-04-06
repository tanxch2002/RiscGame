package risc;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * ClientHandler 负责与单个客户端通信。
 * 在 run() 中完成基础欢迎消息，然后可以在 collectOrders()、collectInitialPlacement() 等方法与用户交互。
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private final RiscServer server;
    private final int playerID;
    private final PlayerAccount account; // 新增，表示登录账户

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
            // 后续可继续在此处处理与客户端的握手、指令等
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向客户端发送一条文本消息
     */
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    /**
     * 收集本玩家的本回合指令(Move/Attack/Upgrade/TechUpgrade/Done)
     */
    public void collectOrders(Game game) {
        try {
            while (true) {
                out.println("Enter an order (M/A/U/T/D): ");
                String line = in.readLine();
                if (line == null) {
                    break; // 客户端断开
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

                } else if (line.startsWith("U")) {
                    out.println("Upgrade format: territory currentLevel targetLevel numUnits");
                    String argsLine = in.readLine();
                    if (argsLine == null) break;
                    processUpgradeOrder(argsLine, game);

                } else if (line.startsWith("T")) {
                    // 发起最大科技等级升级
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
     * 处理单位升级指令：UpgradeUnitOrder
     */
    private void processUpgradeOrder(String argsLine, Game game) {
        // territoryName, currentLevel, targetLevel, numUnits
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
     * 处理最大科技等级升级：TechUpgradeOrder
     */
    private void processTechUpgradeOrder(Game game) {
        game.addOrder(new TechUpgradeOrder(playerID));
        sendMessage("Tech upgrade order added.");
    }

    /**
     * 收集玩家的初始单位安置，将 initialUnits 分配到玩家领土上
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
                        i--; // 重试
                        continue;
                    }
                    // 这里示例：把全部分配为等级0单位
                    t.addUnits(0, units);
                    remainingUnits -= units;
                } catch (IOException | NumberFormatException e) {
                    sendMessage("Error reading input, please re-enter.");
                    i--;
                }
            } else {
                // 最后一个领土自动分配剩余单位
                t.addUnits(0, remainingUnits);
                sendMessage("Territory " + t.getName() + " automatically allocated the remaining " + remainingUnits + " units.");
                remainingUnits = 0;
            }
        }
        sendMessage("Initial unit placement completed!");
    }

    /**
     * 关闭与客户端的连接
     */
    public void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            socket.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
