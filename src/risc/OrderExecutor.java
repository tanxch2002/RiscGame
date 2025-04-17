package risc;

import java.util.*;

public class OrderExecutor {
    private final Game game;

    // 示例：升级/战斗数据
    private static final int[] UNIT_TOTAL_COST = {0, 3, 8, 19, 25, 35, 50};
    private static final int[] UNIT_BONUS      = {0, 1, 3,  5,  8, 11, 15};

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

    // ===================================
    // 1) Move
    // ===================================
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
            int level = m.getLevel();
            int units = m.getNumUnits();

            // 计算移动消耗
            int pathCost = findMinPathSizeSum(src, dest, p);
            if (pathCost < 0) {
                continue;
            }
            int foodCost = pathCost * units;
            if (!p.spendFood(foodCost)) {
                continue;
            }
            // 在src移除部队
            if (!src.removeUnits(p.getId(), level, units)) {
                continue;
            }
            // 在dest添加部队
            dest.addUnits(p.getId(), level, units);
            game.broadcast(p.getName() + " moves " + units + " L" + level + " from "
                    + src.getName() + " to " + dest.getName());
        }
    }

    private boolean validateMove(MoveOrder m) {
        Player p = game.getPlayer(m.getPlayerID());
        Territory src = game.getTerritoryByName(m.getSourceName());
        Territory dest = game.getTerritoryByName(m.getDestName());
        if (src == null || dest == null) return false;

        // 只允许在自己或盟友领地进行移动
        // src必须是自己或盟友控制, dest亦必须是自己或盟友控制
        if (!isOwnedOrAllied(src, p)) return false;
        if (!isOwnedOrAllied(dest, p)) return false;

        // 是否src里有足够单位
        Map<Integer,Integer> srcMap = src.getStationedUnitsMap(p.getId());
        int have = srcMap.getOrDefault(m.getLevel(), 0);
        return have >= m.getNumUnits();
    }

    private boolean isOwnedOrAllied(Territory t, Player p) {
        if (t.getOwner() == p) return true;
        if (t.getOwner() != null && p.isAlliedWith(t.getOwner().getId())) {
            return true;
        }
        return false;
    }

    /**
     * BFS寻找自己或盟友领地的最短路径，返回size之和
     */
    private int findMinPathSizeSum(Territory start, Territory end, Player p) {
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
                if (!visited.contains(nbr)) {
                    if (isOwnedOrAllied(nbr, p)) {
                        visited.add(nbr);
                        queue.offer(new PathNode(nbr, cur.costSum + nbr.getSize()));
                    }
                }
            }
        }
        return -1;
    }

    private static class PathNode {
        Territory terr;
        int costSum;
        public PathNode(Territory t, int cost) {
            this.terr = t;
            this.costSum = cost;
        }
    }

    // ===================================
    // 2) Alliance
    // ===================================
    public void executeAllianceOrders() {
        // 收集发起方 -> 结盟请求列表
        Map<Integer, List<String>> allianceRequests = new HashMap<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof AllianceOrder) {
                AllianceOrder ao = (AllianceOrder) o;
                allianceRequests
                        .computeIfAbsent(ao.getPlayerID(), k -> new ArrayList<>())
                        .add(ao.getTargetPlayerName());
            }
        }

        // username -> playerID
        Map<String, Integer> nameToId = new HashMap<>();
        for (Player p : game.getAllPlayers()) {
            nameToId.put(p.getName(), p.getId());
        }

        // 双向匹配
        for (Map.Entry<Integer, List<String>> entry : allianceRequests.entrySet()) {
            int pA = entry.getKey();
            Player playerA = game.getPlayer(pA);
            if (!playerA.isAlive()) continue;

            for (String targetName : entry.getValue()) {
                Integer pB = nameToId.get(targetName);
                // === 新增检查：pB为null 或 pB == pA => 跳过，不能与自己结盟 ===
                if (pB == null || pB == pA) {
                    // 可选：给玩家A一个提示信息，如:
                    // game.broadcast(playerA.getName() + " tried to form alliance with invalid target: " + targetName);
                    continue;
                }

                Player playerB = game.getPlayer(pB);
                if (!playerB.isAlive()) continue;

                // 看 B 是否也向 A 发起了请求
                List<String> bRequests = allianceRequests.get(pB);
                if (bRequests != null && bRequests.contains(playerA.getName())) {
                    // 成立结盟
                    if (!playerA.isAlliedWith(pB)) {
                        playerA.addAlly(pB);
                        playerB.addAlly(pA);
                        // 打印并广播
                        System.out.println("Alliance formed between "
                                + playerA.getName() + " and " + playerB.getName());
                        game.broadcast("Alliance formed between "
                                + playerA.getName() + " and " + playerB.getName() + "!");
                    }
                }
            }
        }
    }


    // ===================================
    // 3) Attack
    // ===================================
    public void executeAttackOrders() {
        List<AttackOrder> attacks = new ArrayList<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof AttackOrder) {
                attacks.add((AttackOrder) o);
            }
        }

        // 先检测并破盟
        for (AttackOrder ao : attacks) {
            Player attacker = game.getPlayer(ao.getPlayerID());
            Territory dest = game.getTerritoryByName(ao.getDestName());
            if (dest == null) continue;

            Player defender = dest.getOwner();
            if (defender != null && attacker.isAlliedWith(defender.getId())) {
                // 破盟 + 撤回
                breakAllianceAndRecall(attacker, defender);
            }
        }

        // 再做普通攻击
        for (AttackOrder ao : attacks) {
            if (!validateAttack(ao)) {
                continue;
            }
            Player attacker = game.getPlayer(ao.getPlayerID());
            Territory src = game.getTerritoryByName(ao.getSourceName());
            Territory dest = game.getTerritoryByName(ao.getDestName());
            int level = ao.getLevel();
            int units = ao.getNumUnits();

            // 花费粮食
            int costFood = units;
            if (!attacker.spendFood(costFood)) {
                continue;
            }
            // 从src移除
            if (!src.removeUnits(attacker.getId(), level, units)) {
                continue;
            }
            // 进行战斗
            resolveCombat(dest, attacker, level, units);
        }
    }

    private boolean validateAttack(AttackOrder ao) {
        Player p = game.getPlayer(ao.getPlayerID());
        Territory src = game.getTerritoryByName(ao.getSourceName());
        Territory dest = game.getTerritoryByName(ao.getDestName());
        if (src == null || dest == null) return false;
        if (src.getOwner() != p) return false;
        if (dest.getOwner() == p) return false;
        if (!src.getNeighbors().contains(dest)) return false;

        Map<Integer,Integer> srcMap = src.getStationedUnitsMap(p.getId());
        int have = srcMap.getOrDefault(ao.getLevel(), 0);
        return have >= ao.getNumUnits();
    }

    /**
     * 简化版 resolveCombat:
     * 仅考虑 defender.owner 的单位 vs attacker
     * 若 defender 领地上还驻有第三方部队，根据需求可再处理
     */
    private void resolveCombat(Territory dest, Player attacker, int attLevel, int attCount) {
        Player defender = dest.getOwner();
        // defenderUnits => 仅 defender.id
        List<UnitInfo> defUnits = gatherUnits(dest, defender.getId());
        // attackerUnits
        List<UnitInfo> attUnits = new ArrayList<>();
        attUnits.add(new UnitInfo(attLevel, UNIT_BONUS[attLevel], attCount));

        // 投骰子
        while (!attUnits.isEmpty() && !defUnits.isEmpty()) {
            UnitInfo aHigh = getHighestBonus(attUnits);
            UnitInfo dLow = getLowestBonus(defUnits);
            if (aHigh != null && dLow != null) {
                int atkRoll = DiceRoller.rollD20() + aHigh.bonus;
                int defRoll = DiceRoller.rollD20() + dLow.bonus;
                if (atkRoll > defRoll) {
                    dLow.count--;
                    if (dLow.count <= 0) {
                        defUnits.remove(dLow);
                    }
                } else {
                    aHigh.count--;
                    if (aHigh.count <= 0) {
                        attUnits.remove(aHigh);
                    }
                }
            }
            if (attUnits.isEmpty() || defUnits.isEmpty()) break;

            UnitInfo aLow = getLowestBonus(attUnits);
            UnitInfo dHigh = getHighestBonus(defUnits);
            if (aLow != null && dHigh != null) {
                int atkRoll = DiceRoller.rollD20() + aLow.bonus;
                int defRoll = DiceRoller.rollD20() + dHigh.bonus;
                if (atkRoll > defRoll) {
                    dHigh.count--;
                    if (dHigh.count <= 0) {
                        defUnits.remove(dHigh);
                    }
                } else {
                    aLow.count--;
                    if (aLow.count <= 0) {
                        attUnits.remove(aLow);
                    }
                }
            }
        }

        if (defUnits.isEmpty()) {
            // attacker wins
            game.broadcast(attacker.getName() + " conquered " + dest.getName());
            dest.setOwner(attacker);
            // 清空 defender 的驻军
            dest.removeAllUnitsOfPlayer(defender.getId());
            // attacker驻军
            for (UnitInfo info : attUnits) {
                if (info.count > 0) {
                    dest.addUnits(attacker.getId(), info.level, info.count);
                }
            }
            defender.removeTerritory(dest);
            attacker.addTerritory(dest);
        } else {
            // defender wins
            game.broadcast(defender.getName() + " defends " + dest.getName() + " successfully");
            // 清空 attacker 的驻军(这块地的)
            dest.removeAllUnitsOfPlayer(attacker.getId());
            // 把防守剩余单位重新放回
            dest.removeAllUnitsOfPlayer(defender.getId()); // 先清空
            for (UnitInfo info : defUnits) {
                if (info.count > 0) {
                    dest.addUnits(defender.getId(), info.level, info.count);
                }
            }
        }
    }

    // 收集Territory中指定playerID的所有单位
    private List<UnitInfo> gatherUnits(Territory terr, int playerID) {
        List<UnitInfo> list = new ArrayList<>();
        Map<Integer,Integer> map = terr.getStationedUnitsMap(playerID);
        for (Map.Entry<Integer,Integer> e : map.entrySet()) {
            int lvl = e.getKey();
            int cnt = e.getValue();
            list.add(new UnitInfo(lvl, UNIT_BONUS[lvl], cnt));
        }
        return list;
    }

    private UnitInfo getHighestBonus(List<UnitInfo> list) {
        UnitInfo best = null;
        for (UnitInfo u : list) {
            if (best == null || u.bonus > best.bonus) {
                best = u;
            }
        }
        return best;
    }
    private UnitInfo getLowestBonus(List<UnitInfo> list) {
        UnitInfo worst = null;
        for (UnitInfo u : list) {
            if (worst == null || u.bonus < worst.bonus) {
                worst = u;
            }
        }
        return worst;
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

    // ===================================
    // 4) Upgrade
    // ===================================
    public void executeUpgradeOrders() {
        for (Order o : game.getAllOrders()) {
            if (o instanceof UpgradeUnitOrder) {
                UpgradeUnitOrder uo = (UpgradeUnitOrder) o;
                Player p = game.getPlayer(uo.getPlayerID());
                Territory t = game.getTerritoryByName(uo.getSourceName());
                if (t.getOwner() != p) continue;
                if (uo.getTargetLevel() > p.getMaxTechLevel()) continue;

                int needUnits = uo.getNumUnits();
                Map<Integer,Integer> map = t.getStationedUnitsMap(p.getId());
                int have = map.getOrDefault(uo.getCurrentLevel(), 0);
                if (have < needUnits) continue;

                int cost = (UNIT_TOTAL_COST[uo.getTargetLevel()] - UNIT_TOTAL_COST[uo.getCurrentLevel()]) * needUnits;
                if (cost < 0) continue;
                if (!p.spendTech(cost)) continue;

                // remove old
                if (!t.removeUnits(p.getId(), uo.getCurrentLevel(), needUnits)) {
                    continue;
                }
                // add new
                t.addUnits(p.getId(), uo.getTargetLevel(), needUnits);
            }
        }
    }

    public void executeTechUpgradeOrders() {
        Set<Player> upgradedThisTurn = new HashSet<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof TechUpgradeOrder) {
                TechUpgradeOrder to = (TechUpgradeOrder) o;
                Player p = game.getPlayer(to.getPlayerID());
                if (upgradedThisTurn.contains(p)) continue;
                int curr = p.getMaxTechLevel();
                if (curr >= 6) continue;
                int next = curr + 1;
                int cost = TECH_UPGRADE_COST.getOrDefault(curr, 99999);
                if (p.spendTech(cost)) {
                    p.startTechUpgrade(next);
                    upgradedThisTurn.add(p);
                }
            }
        }
    }

    // ===================================
    // 破盟 & 撤军
    // ===================================
    private void breakAllianceAndRecall(Player attacker, Player defender) {
        attacker.removeAlly(defender.getId());
        defender.removeAlly(attacker.getId());
        game.broadcast("Alliance broken due to attack! ("
                + attacker.getName() + " -> " + defender.getName() + ")");
        // 撤回 defender 驻扎在 attacker 领地上的部队
        recallAlliedUnits(defender, attacker);
    }

    private void recallAlliedUnits(Player ally, Player betrayer) {
        List<Territory> betrayerLands = betrayer.getTerritories();
        List<Territory> allyLands = ally.getTerritories();
        if (allyLands.isEmpty()) {
            game.broadcast("No territory to recall for " + ally.getName() + ", skipping...");
            return;
        }

        for (Territory t : betrayerLands) {
            Map<Integer,Integer> removedMap = t.removeAllUnitsOfPlayer(ally.getId());
            if (removedMap != null && !removedMap.isEmpty()) {
                Territory target = pickOneTerritory(allyLands);
                for (Map.Entry<Integer,Integer> e : removedMap.entrySet()) {
                    int lvl = e.getKey();
                    int cnt = e.getValue();
                    target.addUnits(ally.getId(), lvl, cnt);
                }
                game.broadcast("Recalled " + ally.getName() + "'s troops from "
                        + betrayer.getName() + "'s land [" + t.getName() + "] to [" + target.getName() + "]");
            }
        }
    }

    private Territory pickOneTerritory(List<Territory> lands) {
        if (lands.isEmpty()) return null;
        Random rd = game.getRandom();
        return lands.get(rd.nextInt(lands.size()));
    }
}
