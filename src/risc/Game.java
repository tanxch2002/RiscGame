package risc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Game 对象持有：地图(territories)、玩家列表、订单列表、以及执行器(OrderExecutor)。
 * 同时负责在回合结束时产出资源、生成新单位、进行科技升级生效等。
 */
public class Game {
    private final List<Territory> territories;
    private final List<Player> players;
    final List<Order> allOrders;  // 订单列表 (可能多线程访问)
    private final Random rand;
    private final OrderExecutor orderExecutor;

    private int initialUnitsPerPlayer = 10; // 初始每位玩家可分配的单位数量
    private boolean winnerExists = false;
    private Player winner = null;

    public Game() {
        this.territories = new ArrayList<>();
        this.players = new ArrayList<>();
        this.allOrders = Collections.synchronizedList(new ArrayList<>());
        this.rand = new Random();
        this.orderExecutor = new OrderExecutor(this);
    }

    /**
     * 根据玩家数量构建地图
     */
    public void setUpMap(int desiredPlayers) {
        territories.clear();
        // 调用原先的MapBuilder，或者自定义:
        territories.addAll(MapBuilder.buildMap(desiredPlayers));
    }

    /**
     * 初始化玩家并分配领土
     */
    public void initPlayers(int numPlayers) {
        players.clear();
        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player(i, "Player" + (i + 1));
            players.add(p);
        }
        // 将领土分配给玩家 (示例性做法)
        int totalTerritories = territories.size();
        int baseCount = totalTerritories / numPlayers;
        int extra = totalTerritories % numPlayers;

        int index = 0;
        for (Player p : players) {
            int numForThisPlayer = baseCount + (extra > 0 ? 1 : 0);
            if (extra > 0) extra--;
            for (int j = 0; j < numForThisPlayer; j++) {
                Territory t = territories.get(index++);
                t.setOwner(p);
                p.addTerritory(t);
            }
        }
    }

    public int getInitialUnits() {
        return initialUnitsPerPlayer;
    }

    /**
     * 同步地添加一个订单
     */
    public synchronized void addOrder(Order order) {
        allOrders.add(order);
    }

    /**
     * 执行所有移动指令
     */
    public void executeAllMoveOrders() {
        orderExecutor.executeMoveOrders();
    }

    /**
     * 执行所有攻击指令
     */
    public void executeAllAttackOrders() {
        orderExecutor.executeAttackOrders();
    }

    /**
     * 执行单位升级与科技升级指令
     */
    public void executeAllUpgrades() {
        orderExecutor.executeUpgradeOrders();
        orderExecutor.executeTechUpgradeOrders();
    }

    /**
     * 回合结束时清空所有订单
     */
    public void clearAllOrders() {
        allOrders.clear();
    }

    /**
     * 每回合结束时：
     * 1) 若有玩家正在进行 maxTechLevel 升级，则在此时生效
     * 2) 领土为玩家产出资源
     * 3) 每块领土生成 1 个基础单位（level=0）
     */
    public void endTurn() {
        // 1) 生效最大科技等级升级
        for (Player p : players) {
            if (p.isTechUpgrading()) {
                p.finishTechUpgrade();
            }
        }
        // 2) & 3) 收集资源 + 领土上生成1支基础单位
        for (Player p : players) {
            if (!p.isAlive()) continue;
            int totalFood = 0;
            int totalTech = 0;
            for (Territory t : p.getTerritories()) {
                totalFood += t.getFoodProduction();
                totalTech += t.getTechProduction();
                // 增加 1 个等级0单位
                t.addUnits(0, 1);
            }
            p.addFood(totalFood);
            p.addTech(totalTech);
        }
    }

    /**
     * 更新玩家状态。若某玩家已无领土，则其被淘汰；若只剩一位玩家存活，则游戏结束
     */
    public void updatePlayerStatus() {
        for (Player p : players) {
            if (p.getTerritories().isEmpty()) {
                p.setAlive(false);
            }
        }
        // 统计存活玩家
        List<Player> alivePlayers = new ArrayList<>();
        for (Player p : players) {
            if (p.isAlive()) {
                alivePlayers.add(p);
            }
        }
        if (alivePlayers.size() == 1) {
            this.winnerExists = true;
            this.winner = alivePlayers.get(0);
        }
        // 或者检查是否有人拥有了所有领土
        for (Player p : alivePlayers) {
            if (p.getTerritories().size() == territories.size()) {
                this.winnerExists = true;
                this.winner = p;
                break;
            }
        }
    }

    public boolean hasWinner() {
        return winnerExists;
    }

    public Player getWinner() {
        return winner;
    }

    public Territory getTerritoryByName(String name) {
        for (Territory t : territories) {
            if (t.getName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    public Player getPlayer(int id) {
        return players.get(id);
    }

    public List<Order> getAllOrders() {
        return allOrders;
    }

    public Random getRandom() {
        return rand;
    }

    /**
     * 打印地图状态
     */
    public String getMapState() {
        StringBuilder sb = new StringBuilder();
        for (Territory t : territories) {
            sb.append(String.format("%s(%s): %d units, size=%d, neighbors: %s\n",
                    t.getName(),
                    (t.getOwner() == null ? "None" : t.getOwner().getName()),
                    t.getTotalUnits(),
                    t.getSize(),
                    t.neighborsString()));
        }
        return sb.toString();
    }
}
