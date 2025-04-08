package risc;

import java.util.*;

public class OrderExecutor {
    private final Game game;

    // Example: unit level -> (total cost, combat bonus)
    private static final int[] UNIT_TOTAL_COST = {0, 3, 8, 19, 25, 35, 50};
    private static final int[] UNIT_BONUS =      {0, 1, 3,  5,  8, 11, 15};

    // Upgrade cost table (1->2=50, 2->3=75, ...)
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
                // Skip if validation fails
                continue;
            }
            Player p = game.getPlayer(m.getPlayerID());
            Territory src = game.getTerritoryByName(m.getSourceName());
            Territory dest = game.getTerritoryByName(m.getDestName());
            int units = m.getNumUnits();
            int level = m.getLevel();

            // Calculate the food cost for moving, e.g. = sumOfPathSizes * units
            int pathCost = findMinPathSizeSum(src, dest, p);
            if (pathCost < 0) {
                // Unreachable, skip
                continue;
            }
            int foodCost = pathCost * units;
            if (!p.spendFood(foodCost)) {
                // Not enough food, skip
                continue;
            }

            // Deduct units of the corresponding level from the source territory
            if (!src.removeUnits(level, units)) {
                // Insufficient units, skip
                continue;
            }
            // Add units of the corresponding level to the destination territory
            dest.addUnits(level, units);
        }
    }

    private boolean validateMove(MoveOrder m) {
        Player p = game.getPlayer(m.getPlayerID());
        Territory src = game.getTerritoryByName(m.getSourceName());
        Territory dest = game.getTerritoryByName(m.getDestName());
        if (src == null || dest == null) return false;
        if (src.getOwner() != p || dest.getOwner() != p) return false;

        // Check if the source territory has enough units of level m.getLevel()
        int have = src.getUnitMap().getOrDefault(m.getLevel(), 0);
        return have >= m.getNumUnits();
    }

    /**
     * Uses BFS to find the minimal sum of path sizes (only traversing territories owned by player p),
     * returns the sum of the sizes of all territories along the path.
     */
    private int findMinPathSizeSum(Territory start, Territory end, Player p) {
        // Store BFS queue: (territory, costSoFar)
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
        return -1; // Unreachable
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
        // Simplified: only process AttackOrder
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
            int level = ao.getLevel(); // Use the level specified in the attack order

            // Attack cost = units * 1 food
            int foodCost = units;
            if (!attacker.spendFood(foodCost)) {
                continue;
            }
            // Remove units of the corresponding level from the source territory
            if (!src.removeUnits(level, units)) {
                continue;
            }
            // Call the new resolveCombat method with the corresponding level and unit count
            resolveCombat(dest, attacker, level, units);
        }
    }

    private boolean validateAttack(AttackOrder ao) {
        Player p = game.getPlayer(ao.getPlayerID());
        Territory src = game.getTerritoryByName(ao.getSourceName());
        Territory dest = game.getTerritoryByName(ao.getDestName());
        if (src == null || dest == null) return false;
        if (src.getOwner() != p) return false;

        if (dest.getOwner() == p) return false; // Cannot attack your own territory
        if (!src.getNeighbors().contains(dest)) return false; // Must be adjacent
        int have = src.getUnitMap().getOrDefault(0, 0);
        if (dest.getOwner() == p) return false; // Cannot attack your own territory
        if (!src.getNeighbors().contains(dest)) return false; // Must be adjacent

        // Make sure we have enough units of the specified level
        int have = src.getUnitMap().getOrDefault(ao.getLevel(), 0);
        return have >= ao.getNumUnits();
    }

    // New combat mechanism
    private void resolveCombat(Territory dest, Player attacker, int attackingLevel, int attackingCount) {
        Player defender = dest.getOwner();
        // Gather all defending units
        List<UnitInfo> defUnits = gatherUnitsFromTerritory(dest);
        // Gather attacking units
        List<UnitInfo> attUnits = new ArrayList<>();
        attUnits.add(new UnitInfo(attackingLevel, UNIT_BONUS[attackingLevel], attackingCount));

        // Combat rounds: alternate between two pairing methods
        while (!attUnits.isEmpty() && !defUnits.isEmpty()) {
            // First pairing: attacker's highest bonus unit vs. defender's lowest bonus unit
            UnitInfo attackerHighest = getHighestBonus(attUnits);
            UnitInfo defenderLowest = getLowestBonus(defUnits);
            if (attackerHighest != null && defenderLowest != null) {
                int attackRoll = DiceRoller.rollD20() + attackerHighest.bonus;
                int defenseRoll = DiceRoller.rollD20() + defenderLowest.bonus;
                if (attackRoll > defenseRoll) {
                    // Defender loses one unit
                    defenderLowest.count--;
                    if (defenderLowest.count <= 0) {
                        defUnits.remove(defenderLowest);
                    }
                } else {
                    // Tie or defender wins, attacker loses one unit
                    attackerHighest.count--;
                    if (attackerHighest.count <= 0) {
                        attUnits.remove(attackerHighest);
                    }
                }
            }
            // Check if one side has lost all units
            if (attUnits.isEmpty() || defUnits.isEmpty()) break;

            // Second pairing: attacker's lowest bonus unit vs. defender's highest bonus unit
            UnitInfo attackerLowest = getLowestBonus(attUnits);
            UnitInfo defenderHighest = getHighestBonus(defUnits);
            if (attackerLowest != null && defenderHighest != null) {
                int attackRoll = DiceRoller.rollD20() + attackerLowest.bonus;
                int defenseRoll = DiceRoller.rollD20() + defenderHighest.bonus;
                if (attackRoll > defenseRoll) {
                    // Defender loses one unit
                    defenderHighest.count--;
                    if (defenderHighest.count <= 0) {
                        defUnits.remove(defenderHighest);
                    }
                } else {
                    // Attacker loses one unit
                    attackerLowest.count--;
                    if (attackerLowest.count <= 0) {
                        attUnits.remove(attackerLowest);
                    }
                }
            }
        }

        // Combat ends: determine victory based on remaining units
        if (defUnits.isEmpty()) {
            // Attacker wins and occupies the territory
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
            // Defender wins
            dest.getUnitMap().clear();
            for (UnitInfo info : defUnits) {
                if (info.count > 0) {
                    dest.addUnits(info.level, info.count);
                }
            }
        }
    }

    // Helper method: find the unit with the highest combat bonus in the list
    private UnitInfo getHighestBonus(List<UnitInfo> list) {
        UnitInfo highest = null;
        for (UnitInfo info : list) {
            if (highest == null || info.bonus > highest.bonus) {
                highest = info;
            }
        }
        return highest;
    }

    // Helper method: find the unit with the lowest combat bonus in the list
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
        // Optionally, sort the list
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

    //=== New: Unit Upgrade Order
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

                // Upgrade cost = (UNIT_TOTAL_COST[target] - UNIT_TOTAL_COST[current]) * needUnits
                int cost = (UNIT_TOTAL_COST[uo.getTargetLevel()] - UNIT_TOTAL_COST[uo.getCurrentLevel()]) * needUnits;
                if (cost < 0) continue;
                if (!p.spendTech(cost)) continue;

                // Execute the upgrade
                t.removeUnits(uo.getCurrentLevel(), needUnits);
                t.addUnits(uo.getTargetLevel(), needUnits);
            }
        }
    }

    //=== New: Tech Upgrade Order
    public void executeTechUpgradeOrders() {
        // Only one upgrade per turn; additional restrictions can be added
        Set<Player> upgradedThisTurn = new HashSet<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof TechUpgradeOrder) {
                TechUpgradeOrder to = (TechUpgradeOrder) o;
                Player p = game.getPlayer(to.getPlayerID());
                if (upgradedThisTurn.contains(p)) {
                    // Already upgraded, skip
                    continue;
                }
                int currLevel = p.getMaxTechLevel();
                if (currLevel >= 6) continue; // Maximum is 6?
                int nextLevel = currLevel + 1;
                int cost = TECH_UPGRADE_COST.getOrDefault(currLevel, 99999);
                if (p.spendTech(cost)) {
                    // Mark the upgrade; effective at turn end
                    p.startTechUpgrade(nextLevel);
                    upgradedThisTurn.add(p);
                }
            }
        }
    }
}