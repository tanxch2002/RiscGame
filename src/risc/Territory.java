package risc;

import java.util.*;

/**
 * Represents a territory on the game map, with an owner, neighbors, size,
 * and stationed units for multiple players at different levels.
 */
public class Territory {
    private final String name;
    private Player owner; // Territory owner
    private final List<Territory> neighbors;
    private int size;

    // Stationed units: playerID -> (unitLevel -> count)
    private final Map<Integer, Map<Integer, Integer>> stationedUnits;

    public Territory(String name) {
        this(name, 1);
    }

    public Territory(String name, int size) {
        this.name = name;
        this.size = size;
        this.neighbors = new ArrayList<>();
        this.stationedUnits = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Territory> getNeighbors() {
        return neighbors;
    }

    public void addNeighbor(Territory t) {
        if (!neighbors.contains(t)) {
            neighbors.add(t);
        }
    }

    /**
     * Returns neighbor names separated by spaces.
     */
    public String neighborsString() {
        StringBuilder sb = new StringBuilder();
        for (Territory t : neighbors) {
            sb.append(t.name).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Returns the map of unit counts for a specific player.
     */
    public Map<Integer,Integer> getStationedUnitsMap(int playerID) {
        return stationedUnits.getOrDefault(playerID, new HashMap<>());
    }

    /**
     * Adds units of a given level for a player.
     */
    public void addUnits(int playerID, int level, int count) {
        if (count <= 0) return;
        stationedUnits.putIfAbsent(playerID, new HashMap<>());
        Map<Integer,Integer> levelMap = stationedUnits.get(playerID);
        levelMap.put(level, levelMap.getOrDefault(level, 0) + count);
    }

    /**
     * Removes a specified number of units of a given level for a player.
     * @return true if removal succeeded, false otherwise.
     */
    public boolean removeUnits(int playerID, int level, int count) {
        Map<Integer,Integer> levelMap = stationedUnits.get(playerID);
        if (levelMap == null) {
            return false;
        }
        int cur = levelMap.getOrDefault(level, 0);
        if (cur < count) {
            return false;
        }
        int remain = cur - count;
        if (remain == 0) {
            levelMap.remove(level);
        } else {
            levelMap.put(level, remain);
        }
        if (levelMap.isEmpty()) {
            stationedUnits.remove(playerID);
        }
        return true;
    }

    /**
     * Removes all units of a player from this territory.
     * @return map of removed (level -> count) or null if none.
     */
    public Map<Integer,Integer> removeAllUnitsOfPlayer(int playerID) {
        return stationedUnits.remove(playerID);
    }

    /**
     * Returns the total number of units from all players in this territory.
     */
    public int getTotalUnits() {
        int sum = 0;
        for (Map<Integer, Integer> levelMap : stationedUnits.values()) {
            for (int c : levelMap.values()) {
                sum += c;
            }
        }
        return sum;
    }

    /**
     * Food production per turn, equal to territory size.
     */
    public int getFoodProduction() {
        return size;
    }

    /**
     * Tech production per turn, equal to territory size.
     */
    public int getTechProduction() {
        return size;
    }

    /**
     * Returns a string representation of all stationed units for debugging.
     * Example: "P0->{0=5,1=2}; P1->{0=3}".
     */
    public String stationedUnitsString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Map<Integer,Integer>> e : stationedUnits.entrySet()) {
            sb.append("P").append(e.getKey()).append("->").append(e.getValue()).append("; ");
        }
        if (sb.length() == 0) {
            sb.append("No units");
        }
        return sb.toString();
    }
}