package risc;

import java.util.*;

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
        this.allOrders = new ArrayList<>();
        this.rand = new Random();
    }

    /**
     * Creates a simple default map for demonstration:
     *   - 3 territories total, each adjacent to each other.
     */
    public void setupDefaultMap() {
        // 清空原有 territories，避免和旧地图冲突
        this.territories.clear();

        // 示例：创建 7 个领地，名称可自定义
        Territory t1 = new Territory("A");
        Territory t2 = new Territory("B");
        Territory t3 = new Territory("C");
        Territory t4 = new Territory("D");
        Territory t5 = new Territory("E");
        Territory t6 = new Territory("F");
        Territory t7 = new Territory("G");

        // 设置相邻关系（示例拓扑，可根据需要调整）
        // A 与 B、C 邻接
        t1.addNeighbor(t2);
        t1.addNeighbor(t3);

        // B 与 A、D、G 邻接
        t2.addNeighbor(t1);
        t2.addNeighbor(t4);
        t2.addNeighbor(t7);

        // C 与 A、D、E 邻接
        t3.addNeighbor(t1);
        t3.addNeighbor(t4);
        t3.addNeighbor(t5);

        // D 与 B、C、F 邻接
        t4.addNeighbor(t2);
        t4.addNeighbor(t3);
        t4.addNeighbor(t6);

        // E 与 C、F、G 邻接
        t5.addNeighbor(t3);
        t5.addNeighbor(t6);
        t5.addNeighbor(t7);

        // F 与 D、E、G 邻接
        t6.addNeighbor(t4);
        t6.addNeighbor(t5);
        t6.addNeighbor(t7);

        // G 与 B、E、F 邻接
        t7.addNeighbor(t2);
        t7.addNeighbor(t5);
        t7.addNeighbor(t6);

        // 加入到 Game 的 territories 列表
        territories.add(t1);
        territories.add(t2);
        territories.add(t3);
        territories.add(t4);
        territories.add(t5);
        territories.add(t6);
        territories.add(t7);
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
     */
    public void addOrder(Order order) {
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
        // Group by (target territory) => list of attackers
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

        // For each territory, handle the attacks.
        // If multiple players attack the same territory, we do them sequentially in random order.
        for (String targetName : attacksByTarget.keySet()) {
            Territory target = getTerritoryByName(targetName);
            List<AttackOrder> attackers = attacksByTarget.get(targetName);
            Collections.shuffle(attackers, rand); // randomize attack sequence

            for (AttackOrder ao : attackers) {
                // retrieve fresh references in case territory changed owners
                Territory src = getTerritoryByName(ao.getSourceName());
                target = getTerritoryByName(targetName);
                Player attacker = getPlayer(ao.getPlayerID());

                if (src.getUnits() < ao.getNumUnits()) {
                    // not enough units to actually attack
                    continue;
                }
                // remove attacker units from source
                src.setUnits(src.getUnits() - ao.getNumUnits());

                // if the defender has 0 units, attacker wins immediately
                if (target.getUnits() == 0 || !target.getOwner().isAlive()) {
                    target.setOwner(attacker);
                    target.setUnits(ao.getNumUnits());
                    attacker.addTerritory(target);
                    continue;
                }

                // Resolve combat
                int attackerUnits = ao.getNumUnits();
                int defenderUnits = target.getUnits();
                Player defender = target.getOwner();

                // attackerUnits vs. defenderUnits
                while (attackerUnits > 0 && defenderUnits > 0) {
                    int attackRoll = DiceRoller.rollD20();
                    int defenseRoll = DiceRoller.rollD20();
                    if (attackRoll > defenseRoll) {
                        // defender loses 1
                        defenderUnits--;
                    } else {
                        // attacker loses 1 (tie or less)
                        attackerUnits--;
                    }
                }
                // If attacker has leftover units
                if (attackerUnits > 0) {
                    // attacker takes ownership
                    target.setOwner(attacker);
                    target.setUnits(attackerUnits);
                    defender.removeTerritory(target);
                    attacker.addTerritory(target);
                } else {
                    // defender remains
                    target.setUnits(defenderUnits);
                }
            }
        }
    }

    private boolean validateAttack(AttackOrder ao) {
        Territory src = getTerritoryByName(ao.getSourceName());
        Territory dest = getTerritoryByName(ao.getDestName());
        Player p = getPlayer(ao.getPlayerID());
        if (src == null || dest == null) return false;
        if (!src.getOwner().equals(p)) return false;
        if (src.getUnits() < ao.getNumUnits()) return false;
        // must be adjacent to attack
        if (!src.getNeighbors().contains(dest)) return false;
        // cannot attack your own territory
        if (dest.getOwner().equals(p)) return false;
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
