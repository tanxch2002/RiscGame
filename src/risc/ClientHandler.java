package risc;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Each ClientHandler runs on its own thread to communicate with exactly one client.
 */
public class    ClientHandler extends Thread {
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

    /**
     * 收集玩家在初始阶段对各自领土的兵力分配。
     * 每位玩家必须将 game.getInitialUnits() 的单位分配到所有领土上，
     * 除最后一块自动接收剩余单位外，其余领土由玩家输入分配数量。
     */
    public void collectInitialPlacement(Game game) {
        int remainingUnits = game.getInitialUnits();
        Player player = game.getPlayer(playerID);
        List<Territory> territories = player.getTerritories();

        for (int i = 0; i < territories.size(); i++) {
            Territory t = territories.get(i);
            // 如果不是最后一个领土，则让玩家输入分配数量
            if (i < territories.size() - 1) {
                sendMessage("你在领土 " + t.getName() + " 上分配的单位数量？（剩余单位：" + remainingUnits + "）");
                try {
                    String input = in.readLine();
                    int units = Integer.parseInt(input.trim());
                    if (units < 0 || units > remainingUnits) {
                        sendMessage("输入错误，请输入一个 0 到 " + remainingUnits + " 的数字。");
                        i--; // 重复当前领土的输入
                        continue;
                    }
                    t.setUnits(units);
                    remainingUnits -= units;
                } catch (IOException | NumberFormatException e) {
                    sendMessage("读取输入时出错，请重新输入。");
                    i--; // 重复当前领土的输入
                }
            } else {
                // 最后一个领土自动分配剩余的单位
                t.setUnits(remainingUnits);
                sendMessage("领土 " + t.getName() + " 自动分配剩余的 " + remainingUnits + " 单位。");
                remainingUnits = 0;
            }
        }
        sendMessage("你在初始阶段的兵力分配完成！");
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
