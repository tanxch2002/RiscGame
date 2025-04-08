package risc;

import java.util.*;

public class OrderExecutor {
    private final Game game;

    // 示例：单位等级 -> (总费用, 战斗加值)
    private static final int[] UNIT_TOTAL_COST = {0, 3, 8, 19, 25, 35, 50};
    private static final int[] UNIT_BONUS =      {0, 1, 3,  5,  8, 11, 15};

    // 升级费用表 (1->2=50, 2->3=75, ...)
    private static final Map<Integer, Integer> TECH_UPGRADE_COST = new HashMap<>();
    static {
        TECH_UPGRADE_COST.put(1, 50);
        TECH_UPGRADE_COST.put(2, 75);
        TECH_UPGRADE_COST.put(3, 125);
        TECH_UPGRADE_COST.put(4, 200);
        TECH_UPGRADE_COST.put(5, 300);
    }

    public OrderExecutor(Game game) {
        this.game = game;
    }

    public void executeMoveOrders() {
        List<MoveOrder> moves = new ArrayList<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof MoveOrder) {
                moves.add((MoveOrder) o);
            }
        }
        for (MoveOrder m : moves) {
            if (!validateMove(m)) {
                // 验证不通过就跳过
                continue;
            }
            Player p = game.getPlayer(m.getPlayerID());
            Territory src = game.getTerritoryByName(m.getSourceName());
            Territory dest = game.getTerritoryByName(m.getDestName());
            int units = m.getNumUnits();
            int level = m.getLevel();

            // 计算移动的食物费用，示例= sumOfPathSizes * units
            int pathCost = findMinPathSizeSum(src, dest, p);
            if (pathCost < 0) {
                // 不可达，跳过
                continue;
            }
            int foodCost = pathCost * units;
            if (!p.spendFood(foodCost)) {
                // 食物不够，跳过
                continue;
            }

            // 扣除源领土对应等级单位
            if (!src.removeUnits(level, units)) {
                // 不足则跳过
                continue;
            }
            // 在目标领土增加对应等级单位
            dest.addUnits(level, units);
        }
    }


    private boolean validateMove(MoveOrder m) {
        Player p = game.getPlayer(m.getPlayerID());
        Territory src = game.getTerritoryByName(m.getSourceName());
        Territory dest = game.getTerritoryByName(m.getDestName());
        if (src == null || dest == null) return false;
        if (src.getOwner() != p || dest.getOwner() != p) return false;

        // 检查源领土是否有足够 m.getLevel() 的单位
        int have = src.getUnitMap().getOrDefault(m.getLevel(), 0);
        return have >= m.getNumUnits();
    }


    /**
     * BFS求最小路径之和(只经过p拥有的领土)，返回所有领土的size之和。
     */
    private int findMinPathSizeSum(Territory start, Territory end, Player p) {
        // 存储 BFS队列: (territory, costSoFar)
        Queue<PathNode> queue = new LinkedList<>();
        queue.offer(new PathNode(start, start.getSize()));
        Set<Territory> visited = new HashSet<>();
        visited.add(start);

        while (!queue.isEmpty()) {
            PathNode cur = queue.poll();
            if (cur.terr == end) {
                return cur.costSum;
            }
            for (Territory nbr : cur.terr.getNeighbors()) {
                if (!visited.contains(nbr) && nbr.getOwner() == p) {
                    visited.add(nbr);
                    // costSoFar + nbr.size
                    queue.offer(new PathNode(nbr, cur.costSum + nbr.getSize()));
                }
            }
        }
        return -1; // 不可达
    }
    private static class PathNode {
        Territory terr;
        int costSum;
        public PathNode(Territory t, int cost) {
            this.terr = t;
            this.costSum = cost;
        }
    }

    public void executeAttackOrders() {
        // 简化：只处理AttackOrder
        List<AttackOrder> attacks = new ArrayList<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof AttackOrder) {
                attacks.add((AttackOrder) o);
            }
        }
        for (AttackOrder ao : attacks) {
            if (!validateAttack(ao)) {
                continue;
            }
            Player attacker = game.getPlayer(ao.getPlayerID());
            Territory src = game.getTerritoryByName(ao.getSourceName());
            Territory dest = game.getTerritoryByName(ao.getDestName());
            int units = ao.getNumUnits();
            int level = ao.getLevel(); // 使用攻击指令中的等级

            // 攻击花费 = units * 1 食物
            int foodCost = units;
            if (!attacker.spendFood(foodCost)) {
                continue;
            }
            // 从源领土删除对应等级的单位
            if (!src.removeUnits(level, units)) {
                continue;
            }
            // 调用新的resolveCombat方法，传入对应等级和单位数量
            resolveCombat(dest, attacker, level, units);
        }
    }


    private boolean validateAttack(AttackOrder ao) {
        Player p = game.getPlayer(ao.getPlayerID());
        Territory src = game.getTerritoryByName(ao.getSourceName());
        Territory dest = game.getTerritoryByName(ao.getDestName());
        if (src == null || dest == null) return false;
        if (src.getOwner() != p) return false;
<<<<<<< Updated upstream
        if (dest.getOwner() == p) return false; // 不能打自己
        if (!src.getNeighbors().contains(dest)) return false; // 必须相邻
        int have = src.getUnitMap().getOrDefault(0, 0);
=======
        if (dest.getOwner() == p) return false; // Cannot attack your own territory
        if (!src.getNeighbors().contains(dest)) return false; // Must be adjacent

        // Make sure we have enough units of the specified level
        int have = src.getUnitMap().getOrDefault(ao.getLevel(), 0);
>>>>>>> Stashed changes
        return have >= ao.getNumUnits();
    }

    // 新的战斗机制
    private void resolveCombat(Territory dest, Player attacker, int attackingLevel, int attackingCount) {
        Player defender = dest.getOwner();
        // 收集防守方所有单位
        List<UnitInfo> defUnits = gatherUnitsFromTerritory(dest);
        // 收集进攻方单位
        List<UnitInfo> attUnits = new ArrayList<>();
        attUnits.add(new UnitInfo(attackingLevel, UNIT_BONUS[attackingLevel], attackingCount));

        // 战斗回合：交替进行两种配对方式
        while (!attUnits.isEmpty() && !defUnits.isEmpty()) {
            // 第一对：进攻方最高加值单位 vs 防守方最低加值单位
            UnitInfo attackerHighest = getHighestBonus(attUnits);
            UnitInfo defenderLowest = getLowestBonus(defUnits);
            if (attackerHighest != null && defenderLowest != null) {
                int attackRoll = DiceRoller.rollD20() + attackerHighest.bonus;
                int defenseRoll = DiceRoller.rollD20() + defenderLowest.bonus;
                if (attackRoll > defenseRoll) {
                    // 防守方损失1个单位
                    defenderLowest.count--;
                    if (defenderLowest.count <= 0) {
                        defUnits.remove(defenderLowest);
                    }
                } else {
                    // 平局或防守胜，进攻方损失1个单位
                    attackerHighest.count--;
                    if (attackerHighest.count <= 0) {
                        attUnits.remove(attackerHighest);
                    }
                }
            }
            // 检查是否有一方已经全部损失
            if (attUnits.isEmpty() || defUnits.isEmpty()) break;

            // 第二对：进攻方最低加值单位 vs 防守方最高加值单位
            UnitInfo attackerLowest = getLowestBonus(attUnits);
            UnitInfo defenderHighest = getHighestBonus(defUnits);
            if (attackerLowest != null && defenderHighest != null) {
                int attackRoll = DiceRoller.rollD20() + attackerLowest.bonus;
                int defenseRoll = DiceRoller.rollD20() + defenderHighest.bonus;
                if (attackRoll > defenseRoll) {
                    defenderHighest.count--;
                    if (defenderHighest.count <= 0) {
                        defUnits.remove(defenderHighest);
                    }
                } else {
                    attackerLowest.count--;
                    if (attackerLowest.count <= 0) {
                        attUnits.remove(attackerLowest);
                    }
                }
            }
        }

        // 战斗结束：根据剩余单位判断胜负
        if (defUnits.isEmpty()) {
            // 攻击方胜利，领土占领
            dest.setOwner(attacker);
            dest.getUnitMap().clear();
            for (UnitInfo info : attUnits) {
                if (info.count > 0) {
                    dest.addUnits(info.level, info.count);
                }
            }
            defender.removeTerritory(dest);
            attacker.addTerritory(dest);
        } else {
            // 防守方胜利
            dest.getUnitMap().clear();
            for (UnitInfo info : defUnits) {
                if (info.count > 0) {
                    dest.addUnits(info.level, info.count);
                }
            }
        }
    }

    // 辅助方法：查找列表中战斗加值最高的单位
    private UnitInfo getHighestBonus(List<UnitInfo> list) {
        UnitInfo highest = null;
        for (UnitInfo info : list) {
            if (highest == null || info.bonus > highest.bonus) {
                highest = info;
            }
        }
        return highest;
    }

    // 辅助方法：查找列表中战斗加值最低的单位
    private UnitInfo getLowestBonus(List<UnitInfo> list) {
        UnitInfo lowest = null;
        for (UnitInfo info : list) {
            if (lowest == null || info.bonus < lowest.bonus) {
                lowest = info;
            }
        }
        return lowest;
    }


    private List<UnitInfo> gatherUnitsFromTerritory(Territory t) {
        List<UnitInfo> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : t.getUnitMap().entrySet()) {
            int lvl = e.getKey();
            int cnt = e.getValue();
            list.add(new UnitInfo(lvl, UNIT_BONUS[lvl], cnt));
        }
        // 也可排序
        return list;
    }

    private static class UnitInfo {
        int level;
        int bonus;
        int count;
        public UnitInfo(int level, int bonus, int count) {
            this.level = level;
            this.bonus = bonus;
            this.count = count;
        }
    }

    //=== 新增：单位升级指令
    public void executeUpgradeOrders() {
        for (Order o : game.getAllOrders()) {
            if (o instanceof UpgradeUnitOrder) {
                UpgradeUnitOrder uo = (UpgradeUnitOrder) o;
                Player p = game.getPlayer(uo.getPlayerID());
                Territory t = game.getTerritoryByName(uo.getSourceName());
                if (t.getOwner() != p) continue;
                if (uo.getTargetLevel() > p.getMaxTechLevel()) continue;

                int needUnits = uo.getNumUnits();
                int have = t.getUnitMap().getOrDefault(uo.getCurrentLevel(), 0);
                if (have < needUnits) continue;

                // 升级费用 = (UNIT_TOTAL_COST[target] - UNIT_TOTAL_COST[current]) * needUnits
                int cost = (UNIT_TOTAL_COST[uo.getTargetLevel()] - UNIT_TOTAL_COST[uo.getCurrentLevel()]) * needUnits;
                if (cost < 0) continue;
                if (!p.spendTech(cost)) continue;

                // 执行升级
                t.removeUnits(uo.getCurrentLevel(), needUnits);
                t.addUnits(uo.getTargetLevel(), needUnits);
            }
        }
    }

    //=== 新增：最大科技升级指令
    public void executeTechUpgradeOrders() {
        // 同一回合只能一次升级，也可加限制
        Set<Player> upgradedThisTurn = new HashSet<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof TechUpgradeOrder) {
                TechUpgradeOrder to = (TechUpgradeOrder)o;
                Player p = game.getPlayer(to.getPlayerID());
                if (upgradedThisTurn.contains(p)) {
                    // 已经升级过，跳过
                    continue;
                }
                int currLevel = p.getMaxTechLevel();
                if (currLevel >= 6) continue; // 最高6？
                int nextLevel = currLevel + 1;
                int cost = TECH_UPGRADE_COST.getOrDefault(currLevel, 99999);
                if (p.spendTech(cost)) {
                    // 升级标记，回合结束后生效
                    p.startTechUpgrade(nextLevel);
                    upgradedThisTurn.add(p);
                }
            }
        }
    }
}