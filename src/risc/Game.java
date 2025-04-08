package risc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The Game object holds the map (territories), a list of players, the list of orders, and the OrderExecutor.
 * It is also responsible for producing resources, generating new units, and applying technology upgrades at the end of each turn.
 */
public class Game {
    private final List<Territory> territories;
    private final List<Player> players;
    final List<Order> allOrders;  // List of orders (may be accessed by multiple threads)
    private final Random rand;
    private final OrderExecutor orderExecutor;

    private int initialUnitsPerPlayer = 10; // The number of units that each player starts with
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
     * Sets up the map based on the desired number of players.
     */
    public void setUpMap(int desiredPlayers) {
        territories.clear();
        // Call the original MapBuilder or a custom one:
        territories.addAll(MapBuilder.buildMap(desiredPlayers));
    }

    /**
     * Initializes players and assigns territories to them.
     */
    public void initPlayers(int numPlayers) {
        players.clear();
        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player(i, "Player" + (i + 1));
            players.add(p);
        }
        // Distribute territories to players (this is an example approach)
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
     * Synchronously adds an order.
     */
    public synchronized void addOrder(Order order) {
        allOrders.add(order);
    }

    /**
     * Executes all move orders.
     */
    public void executeAllMoveOrders() {
        orderExecutor.executeMoveOrders();
    }

    /**
     * Executes all attack orders.
     */
    public void executeAllAttackOrders() {
        orderExecutor.executeAttackOrders();
    }

    /**
     * Executes both unit upgrade orders and technology upgrade orders.
     */
    public void executeAllUpgrades() {
        orderExecutor.executeUpgradeOrders();
        orderExecutor.executeTechUpgradeOrders();
    }

    /**
     * Clears all orders at the end of the turn.
     */
    public void clearAllOrders() {
        allOrders.clear();
    }

    /**
     * At the end of each turn:
     * 1) If any player is undergoing a maxTechLevel upgrade, it takes effect now.
     * 2) Territories generate resources for the player.
     * 3) Each territory produces one basic unit (level 0).
     */
    public void endTurn() {
        // 1) Apply the max technology level upgrades
        for (Player p : players) {
            if (p.isTechUpgrading()) {
                p.finishTechUpgrade();
            }
        }
        // 2) & 3) Collect resources and produce one basic unit on each territory
        for (Player p : players) {
            if (!p.isAlive()) continue;
            int totalFood = 0;
            int totalTech = 0;
            for (Territory t : p.getTerritories()) {
                totalFood += t.getFoodProduction();
                totalTech += t.getTechProduction();
                // Add one level 0 unit
                t.addUnits(0, 1);
            }
            p.addFood(totalFood);
            p.addTech(totalTech);
        }
    }

    /**
     * Updates the player statuses. If a player has no territories, they are eliminated;
     * if only one player remains alive, the game ends.
     */
    public void updatePlayerStatus() {
        for (Player p : players) {
            if (p.getTerritories().isEmpty()) {
                p.setAlive(false);
            }
        }
        // Count the alive players
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
        // Alternatively, check if a player owns all the territories
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
     * Prints the current state of the map.
     */
    public String getMapState() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== Current Map State =====\n");
        for (Territory t : territories) {
            String owner = (t.getOwner() == null ? "None" : t.getOwner().getName());

            // Collect and sort the number of units per level for orderly output
            List<Integer> sortedLevels = new ArrayList<>(t.getUnitMap().keySet());
            Collections.sort(sortedLevels);

            StringBuilder unitsDetail = new StringBuilder();
            if (sortedLevels.isEmpty()) {
                unitsDetail.append("No units");
            } else {
                for (Integer level : sortedLevels) {
                    int count = t.getUnitMap().get(level);
                    unitsDetail.append("Level ").append(level)
                            .append(": ").append(count).append(" units; ");
                }
                // Remove the extra semicolon and space at the end
                // For example, "Level 0: 5 units; Level 1: 2 units; " -> "Level 0: 5 units; Level 1: 2 units"
                if (unitsDetail.length() > 2) {
                    unitsDetail.setLength(unitsDetail.length() - 2);
                }
            }

            sb.append(String.format("%s (%s)\n", t.getName(), owner));
            sb.append("  Size: ").append(t.getSize())
                    .append(", Neighbors: ").append(t.neighborsString()).append("\n");
            sb.append("  Units: ").append(unitsDetail).append("\n\n");
        }
        sb.append("=============================\n");
        return sb.toString();
    }
}
