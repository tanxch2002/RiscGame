package risc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game {
    private final List<Territory> territories;
    private final List<Player> players;
    final List<Order> allOrders;
    private final Random rand;
    private final OrderExecutor orderExecutor;

    private int initialUnitsPerPlayer = 10;
    private boolean winnerExists = false;
    private Player winner = null;
    private final RiscServer server;

    public Game(RiscServer server) {
        this.server = server;
        this.territories = new ArrayList<>();
        this.players = new ArrayList<>();
        this.allOrders = Collections.synchronizedList(new ArrayList<>());
        this.rand = new Random();
        this.orderExecutor = new OrderExecutor(this);
    }

    public void setUpMap(int desiredPlayers) {
        territories.clear();
        territories.addAll(MapBuilder.buildMap(desiredPlayers));
    }

    public void initPlayers(int numPlayers) {
        players.clear();
        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player(i, "Player" + (i + 1));
            players.add(p);
        }
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

    public synchronized void addOrder(Order order) {
        allOrders.add(order);
    }

    public void executeAllMoveOrders() {
        orderExecutor.executeMoveOrders();
    }

    public void executeAllAttackOrders() {
        orderExecutor.executeAttackOrders();
    }

    public void executeAllAlliances() {
        if (players.size() >= 3) {
            orderExecutor.executeAllianceOrders();
        }
    }

    public void executeAllUpgrades() {
        orderExecutor.executeUpgradeOrders();
        orderExecutor.executeTechUpgradeOrders();
    }

    public void clearAllOrders() {
        allOrders.clear();
    }

    public void endTurn() {
        for (Player p : players) {
            if (p.isTechUpgrading()) {
                p.finishTechUpgrade();
            }
            if (!p.isAlive()) continue;
            int totalFood = 0;
            int totalTech = 0;
            for (Territory t : p.getTerritories()) {
                totalFood += t.getFoodProduction();
                totalTech += t.getTechProduction();
                t.addUnits(p.getId(), 0, 1);
            }
            p.addFood(totalFood);
            p.addTech(totalTech);
        }
    }

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

    public List<Player> getAllPlayers() {
        return players;
    }

    public List<Order> getAllOrders() {
        return allOrders;
    }

    public Random getRandom() {
        return rand;
    }

    public String getMapState() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== Current Map State =====\n");
        for (Territory t : territories) {
            String owner = (t.getOwner() == null ? "None" : t.getOwner().getName());
            sb.append(String.format("%s (%s)\n", t.getName(), owner));
            sb.append("  Size: ").append(t.getSize())
                    .append(", Neighbors: ").append(t.neighborsString()).append("\n");
            sb.append("  StationedUnits: ").append(t.stationedUnitsString()).append("\n\n");
        }
        sb.append("=============================\n");
        return sb.toString();
    }

    public void broadcast(String msg) {
        if (server != null) {
            server.broadcastMessage(msg);
        }
    }
}
