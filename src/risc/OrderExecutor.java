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
                continue;
            }
            Player p = game.getPlayer(m.getPlayerID());
            Territory src = game.getTerritoryByName(m.getSourceName());
            Territory dest = game.getTerritoryByName(m.getDestName());
            int units = m.getNumUnits();

            // 计算最短路径(只走自己的领土)，cost = sum(size)*units
            int pathCost = findMinPathSizeSum(src, dest, p);
            if (pathCost < 0) continue; // 不可达
            int foodCost = pathCost * units;
            if (!p.spendFood(foodCost)) {
                // 不够则跳过
                continue;
            }

            // 真正执行移动
            // 这里先假设全部是level=0的移动，你可以扩展为玩家指定移动哪些等级单位
            // 例子写死：移动numUnits全是等级0
            if (!src.removeUnits(0, units)) {
                continue;
            }
            dest.addUnits(0, units);
        }
    }

    private boolean validateMove(MoveOrder m) {
        Player p = game.getPlayer(m.getPlayerID());
        Territory src = game.getTerritoryByName(m.getSourceName());
        Territory dest = game.getTerritoryByName(m.getDestName());
        if (src == null || dest == null) return false;
        if (src.getOwner() != p || dest.getOwner() != p) return false;
        // src中有没有足够单位(这里也写死等级0)
        int have = src.getUnitMap().getOrDefault(0, 0);
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
                attacks.add((AttackOrder)o);
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
            // 攻击花费 = units * 1 食物
            int foodCost = units;
            if (!attacker.spendFood(foodCost)) {
                continue;
            }
            // 从src删除(假设全是等级0...)
            if (!src.removeUnits(0, units)) {
                continue;
            }
            // 进行战斗结算
            resolveCombat(dest, attacker, 0, units);
        }
    }

    private boolean validateAttack(AttackOrder ao) {
        Player p = game.getPlayer(ao.getPlayerID());
        Territory src = game.getTerritoryByName(ao.getSourceName());
        Territory dest = game.getTerritoryByName(ao.getDestName());
        if (src == null || dest == null) return false;
        if (src.getOwner() != p) return false;
        if (dest.getOwner() == p) return false; // 不能打自己
        if (!src.getNeighbors().contains(dest)) return false; // 必须相邻
        int have = src.getUnitMap().getOrDefault(0, 0);
        return have >= ao.getNumUnits();
    }

    // 新的战斗机制（示例：只考虑攻击方的units都是同一等级）
    private void resolveCombat(Territory dest, Player attacker, int attackingLevel, int attackingCount) {
        Player defender = dest.getOwner();
        // 收集防守方所有单位
        List<UnitInfo> defUnits = gatherUnitsFromTerritory(dest);
        // 收集进攻方单位
        List<UnitInfo> attUnits = new ArrayList<>();
        attUnits.add(new UnitInfo(attackingLevel, UNIT_BONUS[attackingLevel], attackingCount));

        // 先按攻击方bonus降序，再按防方bonus升序对战，再换过来 (简化实现略)
        // 这里仅示例一个简单的“随机配对”流程
        while (!attUnits.isEmpty() && !defUnits.isEmpty()) {
            UnitInfo a = attUnits.get(0);
            UnitInfo d = defUnits.get(0);

            int attackRoll = DiceRoller.rollD20() + a.bonus;
            int defenseRoll = DiceRoller.rollD20() + d.bonus;
            if (attackRoll > defenseRoll) {
                // 防守死1
                d.count--;
                if (d.count <= 0) {
                    defUnits.remove(0);
                }
            } else {
                // 平局/防守大时 攻击死1
                a.count--;
                if (a.count <= 0) {
                    attUnits.remove(0);
                }
            }
        }
        // 战斗结束
        if (defUnits.isEmpty()) {
            // 攻击方占领
            dest.setOwner(attacker);
            // 清空防守单位
            dest.getUnitMap().clear();
            // 剩余进攻方单位放入领土
            for (UnitInfo info : attUnits) {
                if (info.count > 0) {
                    dest.addUnits(info.level, info.count);
                }
            }
            defender.removeTerritory(dest);
            attacker.addTerritory(dest);
        } else {
            // 防守成功
            // 更新territory单位为剩余defUnits
            dest.getUnitMap().clear();
            for (UnitInfo info : defUnits) {
                if (info.count > 0) {
                    dest.addUnits(info.level, info.count);
                }
            }
        }
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
