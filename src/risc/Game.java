package risc;

import java.util.*;
import java.util.Collections;

/**
 * The Game class holds the map, territories, players, and orders.
 * It enforces rules for moves, attacks, and turn resolution.
 */
public class Game {
    private final List<Territory> territories;
    private final List<Player> players;
    private final List<Order> allOrders;
    private final Random rand;

    private int initialUnitsPerPlayer = 10; // example
    private boolean winnerExists = false;
    private Player winner = null;

    public Game() {
        this.territories = new ArrayList<>();
        this.players = new ArrayList<>();
        // 使用线程安全的集合
        this.allOrders = Collections.synchronizedList(new ArrayList<>());
        this.rand = new Random();
    }

    /**
     * 构造六块土地的地图
     */
    public void setupSixMap() {
        territories.clear();
        // 创建 6 个领地
        Territory t1 = new Territory("A");
        Territory t2 = new Territory("B");
        Territory t3 = new Territory("C");
        Territory t4 = new Territory("D");
        Territory t5 = new Territory("E");
        Territory t6 = new Territory("F");

        // 设置邻接关系（示例拓扑）
        // A 与 B、C 邻接
        t1.addNeighbor(t2);
        t1.addNeighbor(t3);

        // B 与 A、D 邻接
        t2.addNeighbor(t1);
        t2.addNeighbor(t4);

        // C 与 A、D、E 邻接
        t3.addNeighbor(t1);
        t3.addNeighbor(t4);
        t3.addNeighbor(t5);

        // D 与 B、C、F 邻接
        t4.addNeighbor(t2);
        t4.addNeighbor(t3);
        t4.addNeighbor(t6);

        // E 与 C、F 邻接
        t5.addNeighbor(t3);
        t5.addNeighbor(t6);

        // F 与 D、E 邻接
        t6.addNeighbor(t4);
        t6.addNeighbor(t5);

        territories.add(t1);
        territories.add(t2);
        territories.add(t3);
        territories.add(t4);
        territories.add(t5);
        territories.add(t6);
    }

    /**
     * 构造八块土地的地图
     */
    public void setupEightMap() {
        territories.clear();
        // 创建 8 个领地
        Territory t1 = new Territory("A");
        Territory t2 = new Territory("B");
        Territory t3 = new Territory("C");
        Territory t4 = new Territory("D");
        Territory t5 = new Territory("E");
        Territory t6 = new Territory("F");
        Territory t7 = new Territory("G");
        Territory t8 = new Territory("H");

        // 设置邻接关系（示例拓扑）
        // A: B, C
        t1.addNeighbor(t2);
        t1.addNeighbor(t3);

        // B: A, D, E
        t2.addNeighbor(t1);
        t2.addNeighbor(t4);
        t2.addNeighbor(t5);

        // C: A, F, G
        t3.addNeighbor(t1);
        t3.addNeighbor(t6);
        t3.addNeighbor(t7);

        // D: B, H
        t4.addNeighbor(t2);
        t4.addNeighbor(t8);

        // E: B, F, H
        t5.addNeighbor(t2);
        t5.addNeighbor(t6);
        t5.addNeighbor(t8);

        // F: C, E, G
        t6.addNeighbor(t3);
        t6.addNeighbor(t5);
        t6.addNeighbor(t7);

        // G: C, F, H
        t7.addNeighbor(t3);
        t7.addNeighbor(t6);
        t7.addNeighbor(t8);

        // H: D, E, G
        t8.addNeighbor(t4);
        t8.addNeighbor(t5);
        t8.addNeighbor(t7);

        territories.add(t1);
        territories.add(t2);
        territories.add(t3);
        territories.add(t4);
        territories.add(t5);
        territories.add(t6);
        territories.add(t7);
        territories.add(t8);
    }

    /**
     * 构造十块土地的地图
     */
    public void setupTenMap() {
        territories.clear();
        // 创建 10 个领地
        Territory t1 = new Territory("A");
        Territory t2 = new Territory("B");
        Territory t3 = new Territory("C");
        Territory t4 = new Territory("D");
        Territory t5 = new Territory("E");
        Territory t6 = new Territory("F");
        Territory t7 = new Territory("G");
        Territory t8 = new Territory("H");
        Territory t9 = new Territory("I");
        Territory t10 = new Territory("J");

        // 设置邻接关系（示例拓扑）
        // A: B, C
        t1.addNeighbor(t2);
        t1.addNeighbor(t3);

        // B: A, D, E
        t2.addNeighbor(t1);
        t2.addNeighbor(t4);
        t2.addNeighbor(t5);

        // C: A, F, G
        t3.addNeighbor(t1);
        t3.addNeighbor(t6);
        t3.addNeighbor(t7);

        // D: B, H
        t4.addNeighbor(t2);
        t4.addNeighbor(t8);

        // E: B, F, I
        t5.addNeighbor(t2);
        t5.addNeighbor(t6);
        t5.addNeighbor(t9);

        // F: C, E, G, J
        t6.addNeighbor(t3);
        t6.addNeighbor(t5);
        t6.addNeighbor(t7);
        t6.addNeighbor(t10);

        // G: C, F, H
        t7.addNeighbor(t3);
        t7.addNeighbor(t6);
        t7.addNeighbor(t8);

        // H: D, G, I
        t8.addNeighbor(t4);
        t8.addNeighbor(t7);
        t8.addNeighbor(t9);

        // I: E, H, J
        t9.addNeighbor(t5);
        t9.addNeighbor(t8);
        t9.addNeighbor(t10);

        // J: F, I
        t10.addNeighbor(t6);
        t10.addNeighbor(t9);

        territories.add(t1);
        territories.add(t2);
        territories.add(t3);
        territories.add(t4);
        territories.add(t5);
        territories.add(t6);
        territories.add(t7);
        territories.add(t8);
        territories.add(t9);
        territories.add(t10);
    }

    public void setUpMap(int desiredPlayers){
        if(desiredPlayers == 3){
            setupSixMap();
        } else if (desiredPlayers == 5) {
            setupTenMap();
        }else{
            setupEightMap();
        }
    }

    /**
     * Initialize players, assign territories in some manner.
     */
    public void initPlayers(int numPlayers) {
        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player(i, "Player" + (i + 1));
            players.add(p);
        }
        // For minimal code: just divide the territories evenly in a round-robin
        for (int i = 0; i < territories.size(); i++) {
            Territory t = territories.get(i);
            Player p = players.get(i % numPlayers);
            t.setOwner(p);
            t.setUnits(0); // start with 0, will place in initial phase
            p.addTerritory(t);
        }
    }

    public int getInitialUnits() {
        return initialUnitsPerPlayer;
    }

    /**
     * Minimal approach: distribute initial units evenly across each player's territory
     */
    public void distributeInitialUnits() {
        for (Player p : players) {
            int territoriesCount = p.getTerritories().size();
            // simply put all units evenly
            int unitsLeft = initialUnitsPerPlayer;
            for (Territory t : p.getTerritories()) {
                int toPlace = unitsLeft / territoriesCount;
                t.setUnits(t.getUnits() + toPlace);
                unitsLeft -= toPlace;
                territoriesCount--;
            }
        }
    }

    /**
     * Add a newly created Order to the queue.
     * 使用 synchronized 关键字保证线程安全
     */
    public synchronized void addOrder(Order order) {
        // The server might do checks to ensure no duplication or nonsense, omitted for brevity
        allOrders.add(order);
    }

    /**
     * Execute all move orders first.
     */
    public void executeAllMoveOrders() {
        // Filter out MoveOrders
        List<MoveOrder> moves = new ArrayList<>();
        for (Order o : allOrders) {
            if (o instanceof MoveOrder) {
                moves.add((MoveOrder)o);
            }
        }
        // Validate and execute each move
        for (MoveOrder m : moves) {
            if (!validateMove(m)) {
                continue; // skip invalid moves
            }
            // remove units from source
            Territory src = getTerritoryByName(m.getSourceName());
            src.setUnits(src.getUnits() - m.getNumUnits());
            // add to destination
            Territory dest = getTerritoryByName(m.getDestName());
            dest.setUnits(dest.getUnits() + m.getNumUnits());
        }
    }

    private boolean validateMove(MoveOrder move) {
        Territory src = getTerritoryByName(move.getSourceName());
        Territory dest = getTerritoryByName(move.getDestName());
        Player p = getPlayer(move.getPlayerID());
        if (src == null || dest == null) return false;
        if (!src.getOwner().equals(p) || !dest.getOwner().equals(p)) return false;
        if (src.getUnits() < move.getNumUnits()) return false;
        // check connectivity
        return canReach(src, dest, p);
    }

    /**
     * BFS or DFS to see if there's a path from src to dest where all territories belong to p.
     */
    private boolean canReach(Territory src, Territory dest, Player p) {
        Set<Territory> visited = new HashSet<>();
        Queue<Territory> queue = new LinkedList<>();
        queue.add(src);
        visited.add(src);
        while (!queue.isEmpty()) {
            Territory current = queue.poll();
            if (current.equals(dest)) {
                return true;
            }
            for (Territory nbr : current.getNeighbors()) {
                if (!visited.contains(nbr) && nbr.getOwner().equals(p)) {
                    visited.add(nbr);
                    queue.add(nbr);
                }
            }
        }
        return false;
    }

    /**
     * Execute all attack orders second.
     */
    public void executeAllAttackOrders() {
        // Step 1: 预处理互相攻击的订单
        // 收集所有合法的攻击订单
        List<AttackOrder> mutualOrders = new ArrayList<>();
        List<AttackOrder> allAttackOrders = new ArrayList<>();
        for (Order o : allOrders) {
            if (o instanceof AttackOrder) {
                AttackOrder ao = (AttackOrder) o;
                if (validateAttack(ao)) {
                    allAttackOrders.add(ao);
                }
            }
        }
        // 用于记录已特殊处理的订单，避免重复处理
        List<AttackOrder> processedOrders = new ArrayList<>();
        for (AttackOrder ao : allAttackOrders) {
            if (processedOrders.contains(ao))
                continue;
            // 在所有订单中查找与当前订单相反的订单
            AttackOrder reverse = null;
            for (AttackOrder other : allAttackOrders) {
                if (other == ao)
                    continue;
                if (processedOrders.contains(other))
                    continue;
                if (other.getSourceName().equalsIgnoreCase(ao.getDestName())
                        && other.getDestName().equalsIgnoreCase(ao.getSourceName())) {
                    reverse = other;
                    break;
                }
            }
            if (reverse != null) {
                Territory src = getTerritoryByName(ao.getSourceName());
                Territory dest = getTerritoryByName(ao.getDestName());
                if (src != null && dest != null) {
                    // 判断是否为满兵出击：即攻击订单中派出的单位数与当前领地的驻军数相等
                    if (src.getUnits() == ao.getNumUnits() && dest.getUnits() == reverse.getNumUnits()) {
                        // 满足互相攻击的条件，特殊处理：
                        // 对于来自 X->Y 的订单，将领地 Y 的所有权转给攻击方，并将部队数设为订单中的数量
                        // 同理，对于来自 Y->X 的订单，将领地 X 的所有权转给对应的攻击方
                        Player attackerForDest = getPlayer(ao.getPlayerID());
                        Player attackerForSrc = getPlayer(reverse.getPlayerID());

                        dest.setOwner(attackerForDest);
                        dest.setUnits(ao.getNumUnits());
                        attackerForDest.addTerritory(dest);

                        src.setOwner(attackerForSrc);
                        src.setUnits(reverse.getNumUnits());
                        attackerForSrc.addTerritory(src);

                        // 标记这对订单已处理
                        processedOrders.add(ao);
                        processedOrders.add(reverse);
                        mutualOrders.add(ao);
                        mutualOrders.add(reverse);
                    }
                }
            }
        }
        // 将特殊处理的订单从总订单中移除，以免重复处理
        allOrders.removeAll(mutualOrders);

        // Step 2: 对剩余订单按原有逻辑处理
        // 按目标领地分组
        Map<String, List<AttackOrder>> attacksByTarget = new HashMap<>();
        for (Order o : allOrders) {
            if (o instanceof AttackOrder) {
                AttackOrder ao = (AttackOrder) o;
                if (!validateAttack(ao)) {
                    continue;
                }
                attacksByTarget.putIfAbsent(ao.getDestName(), new ArrayList<>());
                attacksByTarget.get(ao.getDestName()).add(ao);
            }
        }

        // 对每个目标领地的攻击订单随机排序后依次处理
        for (String targetName : attacksByTarget.keySet()) {
            Territory target = getTerritoryByName(targetName);
            List<AttackOrder> attackers = attacksByTarget.get(targetName);
            Collections.shuffle(attackers, rand); // 随机顺序

            for (AttackOrder ao : attackers) {
                // 重新获取最新的领地状态
                Territory src = getTerritoryByName(ao.getSourceName());
                target = getTerritoryByName(targetName);
                Player attacker = getPlayer(ao.getPlayerID());

                if (src.getUnits() < ao.getNumUnits()) {
                    // 攻击方单位不足则跳过
                    continue;
                }
                // 从来源领地扣除进攻部队
                src.setUnits(src.getUnits() - ao.getNumUnits());

                // 如果目标领地防守部队为 0 或防守方不再存活，则直接占领
                if (target.getUnits() == 0 || !target.getOwner().isAlive()) {
                    target.setOwner(attacker);
                    target.setUnits(ao.getNumUnits());
                    attacker.addTerritory(target);
                    continue;
                }

                // 战斗解决过程：双方单位轮流损失，直到一方耗尽
                int attackerUnits = ao.getNumUnits();
                int defenderUnits = target.getUnits();
                Player defender = target.getOwner();

                while (attackerUnits > 0 && defenderUnits > 0) {
                    int attackRoll = DiceRoller.rollD20();
                    int defenseRoll = DiceRoller.rollD20();
                    if (attackRoll > defenseRoll) {
                        defenderUnits--;
                    } else {
                        attackerUnits--;
                    }
                }
                // 战斗结束后的结果更新
                if (attackerUnits > 0) {
                    target.setOwner(attacker);
                    target.setUnits(attackerUnits);
                    defender.removeTerritory(target);
                    attacker.addTerritory(target);
                } else {
                    target.setUnits(defenderUnits);
                }
            }
        }
    }

    /**
     * 验证攻击订单是否合法：
     * 1. 检查来源与目标领地是否存在
     * 2. 检查来源领地是否由发起攻击的玩家控制，且有足够部队
     * 3. 检查来源与目标是否邻接
     * 4. 确保不能攻击自己领地
     */
    private boolean validateAttack(AttackOrder ao) {
        Territory src = getTerritoryByName(ao.getSourceName());
        Territory dest = getTerritoryByName(ao.getDestName());
        Player p = getPlayer(ao.getPlayerID());
        if (src == null || dest == null)
            return false;
        if (!src.getOwner().equals(p))
            return false;
        if (src.getUnits() < ao.getNumUnits())
            return false;
        if (!src.getNeighbors().contains(dest))
            return false;
        if (dest.getOwner().equals(p))
            return false;
        return true;
    }


    /**
     * After all orders are done, add 1 unit per territory.
     */
    public void addOneUnitToEachTerritory() {
        for (Territory t : territories) {
            t.setUnits(t.getUnits() + 1);
        }
    }

    /**
     * Remove players that have no territories, mark them as defeated.
     */
    public void updatePlayerStatus() {
        for (Player p : players) {
            if (p.getTerritories().isEmpty()) {
                p.setAlive(false);
            }
        }
        // check if we have a winner
        // if exactly 1 alive player or 1 player owns all territories
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
        // or if a single player owns all territories
        for (Player p : players) {
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

    /**
     * Utility to get a territory by name.
     */
    private Territory getTerritoryByName(String name) {
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

    public void clearAllOrders() {
        allOrders.clear();
    }

    /**
     * Return a string representation of the map for display in clients.
     */
    public String getMapState() {
        StringBuilder sb = new StringBuilder();
        for (Territory t : territories) {
            sb.append(String.format("%s(%s): %d units, neighbors: %s\n",
                    t.getName(), t.getOwner().getName(),
                    t.getUnits(), t.neighborsString()));
        }
        return sb.toString();
    }
}
