package risc;

import java.util.*;

public class OrderExecutor {
    private final Game game;

    // Example data for unit upgrade costs and combat bonuses
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
    // 1) Move Orders
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

            // Calculate movement cost
            int pathCost = findMinPathSizeSum(src, dest, p);
            if (pathCost < 0) {
                continue;
            }
            int foodCost = pathCost * units;
            if (!p.spendFood(foodCost)) {
                continue;
            }
            // Remove units from source
            if (!src.removeUnits(p.getId(), level, units)) {
                continue;
            }
            // Add units to destination
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

        // Only allow moves within territories owned by the player or their allies
        if (!isOwnedOrAllied(src, p)) return false;
        if (!isOwnedOrAllied(dest, p)) return false;

        // Check if source has enough units
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
     * Uses BFS to find the minimum sum of territory sizes along a path through
     * territories owned by the player or their allies.
     * Returns the sum of sizes, or -1 if no valid path exists.
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
    // 2) Alliance Orders
    // ===================================
    public void executeAllianceOrders() {
        // Collect alliance requests: initiator -> list of target names
        Map<Integer, List<String>> allianceRequests = new HashMap<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof AllianceOrder) {
                AllianceOrder ao = (AllianceOrder) o;
                allianceRequests
                        .computeIfAbsent(ao.getPlayerID(), k -> new ArrayList<>())
                        .add(ao.getTargetPlayerName());
            }
        }

        // Map usernames to player IDs
        Map<String, Integer> nameToId = new HashMap<>();
        for (Player p : game.getAllPlayers()) {
            nameToId.put(p.getName(), p.getId());
        }

        // Match requests bidirectionally
        for (Map.Entry<Integer, List<String>> entry : allianceRequests.entrySet()) {
            int pA = entry.getKey();
            Player playerA = game.getPlayer(pA);
            if (!playerA.isAlive()) continue;

            for (String targetName : entry.getValue()) {
                Integer pB = nameToId.get(targetName);
                // Skip invalid or self-targeted requests
                if (pB == null || pB == pA) {
                    continue;
                }

                Player playerB = game.getPlayer(pB);
                if (!playerB.isAlive()) continue;

                // Check if B also requested alliance with A
                List<String> bRequests = allianceRequests.get(pB);
                if (bRequests != null && bRequests.contains(playerA.getName())) {
                    // Form alliance if not already allied
                    if (!playerA.isAlliedWith(pB)) {
                        playerA.addAlly(pB);
                        playerB.addAlly(pA);
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
    // 3) Attack Orders
    // ===================================
    public void executeAttackOrders() {
        List<AttackOrder> attacks = new ArrayList<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof AttackOrder) {
                attacks.add((AttackOrder) o);
            }
        }

        // First, handle alliance breaking and recall troops
        for (AttackOrder ao : attacks) {
            Player attacker = game.getPlayer(ao.getPlayerID());
            Territory dest = game.getTerritoryByName(ao.getDestName());
            if (dest == null) continue;

            Player defender = dest.getOwner();
            if (defender != null && attacker.isAlliedWith(defender.getId())) {
                // Break alliance and recall defender's troops
                breakAllianceAndRecall(attacker, defender);
            }
        }

        // Then process standard attacks
        for (AttackOrder ao : attacks) {
            if (!validateAttack(ao)) {
                continue;
            }
            Player attacker = game.getPlayer(ao.getPlayerID());
            Territory src = game.getTerritoryByName(ao.getSourceName());
            Territory dest = game.getTerritoryByName(ao.getDestName());
            int level = ao.getLevel();
            int units = ao.getNumUnits();

            // Spend food for attack
            int costFood = units;
            if (!attacker.spendFood(costFood)) {
                continue;
            }
            // Remove units from source
            if (!src.removeUnits(attacker.getId(), level, units)) {
                continue;
            }
            // Resolve combat
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
     * Simplified combat resolution: handles combat between the attacker and the current territory owner.
     * Additional third-party garrisons are not considered here but can be added if needed.
     */
    private void resolveCombat(Territory dest, Player attacker, int attLevel, int attCount) {
        Player defender = dest.getOwner();
        List<UnitInfo> defUnits = gatherUnits(dest, defender.getId());
        List<UnitInfo> attUnits = new ArrayList<>();
        attUnits.add(new UnitInfo(attLevel, UNIT_BONUS[attLevel], attCount));

        // Conduct D20-based combat with bonuses
        while (!attUnits.isEmpty() && !defUnits.isEmpty()) {
            UnitInfo aHigh = getHighestBonus(attUnits);
            UnitInfo dLow = getLowestBonus(defUnits);
            if (aHigh != null && dLow != null) {
                int atkRoll = DiceRoller.rollD20() + aHigh.bonus;
                int defRoll = DiceRoller.rollD20() + dLow.bonus;
                if (atkRoll > defRoll) {
                    dLow.count--;
                    if (dLow.count <= 0) defUnits.remove(dLow);
                } else {
                    aHigh.count--;
                    if (aHigh.count <= 0) attUnits.remove(aHigh);
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
                    if (dHigh.count <= 0) defUnits.remove(dHigh);
                } else {
                    aLow.count--;
                    if (aLow.count <= 0) attUnits.remove(aLow);
                }
            }
        }

        if (defUnits.isEmpty()) {
            // Attacker conquers the territory
            game.broadcast(attacker.getName() + " conquered " + dest.getName());
            dest.setOwner(attacker);
            dest.removeAllUnitsOfPlayer(defender.getId());
            for (UnitInfo info : attUnits) {
                if (info.count > 0) dest.addUnits(attacker.getId(), info.level, info.count);
            }
            defender.removeTerritory(dest);
            attacker.addTerritory(dest);
        } else {
            // Defender holds the territory
            game.broadcast(defender.getName() + " defends " + dest.getName() + " successfully");
            dest.removeAllUnitsOfPlayer(attacker.getId());
            dest.removeAllUnitsOfPlayer(defender.getId());
            for (UnitInfo info : defUnits) {
                if (info.count > 0) dest.addUnits(defender.getId(), info.level, info.count);
            }
        }
    }

    // Gather all stationed units of a specific player in a territory
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
            if (best == null || u.bonus > best.bonus) best = u;
        }
        return best;
    }
    private UnitInfo getLowestBonus(List<UnitInfo> list) {
        UnitInfo worst = null;
        for (UnitInfo u : list) {
            if (worst == null || u.bonus < worst.bonus) worst = u;
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
    // 4) Upgrade Orders
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

                // Remove old-level units and add upgraded units
                if (!t.removeUnits(p.getId(), uo.getCurrentLevel(), needUnits)) continue;
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
    // Break Alliance & Recall Troops
    // ===================================
    private void breakAllianceAndRecall(Player attacker, Player defender) {
        attacker.removeAlly(defender.getId());
        defender.removeAlly(attacker.getId());
        game.broadcast("Alliance broken due to attack! ("
                + attacker.getName() + " -> " + defender.getName() + ")");
        // Recall defender's units stationed on attacker's territories
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

    /**
     * Picks a random territory from the given list
     */
    private Territory pickOneTerritory(List<Territory> lands) {
        if (lands.isEmpty()) return null;
        Random rd = game.getRandom();
        return lands.get(rd.nextInt(lands.size()));
    }
}