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
    final List<Order> allOrders;  // package-private for OrderExecutor access
    private final Random rand;
    private final OrderExecutor orderExecutor;

    private int initialUnitsPerPlayer = 10; // example value
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
     * Set up the map based on the desired number of players.
     */
    public void setUpMap(int desiredPlayers) {
        territories.clear();
        territories.addAll(MapBuilder.buildMap(desiredPlayers));
    }

    /**
     * Initialize players and assign territories.
     */
    public void initPlayers(int numPlayers) {
        // Create players
        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player(i, "Player" + (i + 1));
            players.add(p);
        }

        // Calculate base and extra territories per player
        int totalTerritories = territories.size();
        int baseCount = totalTerritories / numPlayers;
        int extra = totalTerritories % numPlayers;

        int index = 0;
        // Distribute territories to each player
        for (Player p : players) {
            int numForThisPlayer = baseCount + (extra > 0 ? 1 : 0);
            if (extra > 0) {
                extra--;
            }
            for (int j = 0; j < numForThisPlayer; j++) {
                Territory t = territories.get(index++);
                t.setOwner(p);
                t.setUnits(0); // initial unit count
                p.addTerritory(t);
            }
        }
    }

    public int getInitialUnits() {
        return initialUnitsPerPlayer;
    }

    /**
     * Evenly distribute initial units across each player's territories.
     */
    public void distributeInitialUnits() {
        for (Player p : players) {
            int territoriesCount = p.getTerritories().size();
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
     * Add a new Order to the queue.
     */
    public synchronized void addOrder(Order order) {
        allOrders.add(order);
    }

    /**
     * Execute all move orders.
     */
    public void executeAllMoveOrders() {
        orderExecutor.executeMoveOrders();
    }

    /**
     * Execute all attack orders.
     */
    public void executeAllAttackOrders() {
        orderExecutor.executeAttackOrders();
    }

    public void clearAllOrders() {
        allOrders.clear();
    }

    /**
     * After all orders are executed, add 1 unit per territory.
     */
    public void addOneUnitToEachTerritory() {
        for (Territory t : territories) {
            t.setUnits(t.getUnits() + 1);
        }
    }

    /**
     * Remove players with no territories and check for a winner.
     */
    public void updatePlayerStatus() {
        for (Player p : players) {
            if (p.getTerritories().isEmpty()) {
                p.setAlive(false);
            }
        }
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

    /**
     * Get the list of orders.
     */
    List<Order> getAllOrders() {
        return allOrders;
    }

    public Random getRandom() {
        return rand;
    }

    /**
     * Return a string representation of the map for display.
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
