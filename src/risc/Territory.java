package risc;

import java.util.*;

public class Territory {
    // The territory's name.
    private final String name;
    // The owner of the territory.
    private Player owner;

    // Replaces the original integer unit count with a map from unit level to unit count.
    private Map<Integer, Integer> unitMap;
    // List of neighboring territories.
    private final List<Territory> neighbors;

    // New attribute: the size of the territory.
    private int size;

    /**
     * Constructs a territory with the specified name and a default size of 1.
     *
     * @param name the name of the territory
     */
    public Territory(String name) {
        this(name, 1);
    }

    /**
     * Constructs a territory with the specified name and size.
     *
     * @param name the name of the territory
     * @param size the size of the territory
     */
    public Territory(String name, int size) {
        this.name = name;
        this.size = size;
        this.unitMap = new HashMap<>();
        this.neighbors = new ArrayList<>();
    }

    /**
     * Returns the territory's name.
     *
     * @return the name of the territory
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the owner of the territory.
     *
     * @return the player who owns the territory
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the territory.
     *
     * @param owner the new owner of the territory
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * Returns the size of the territory.
     *
     * @return the size of the territory
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the size of the territory.
     *
     * @param size the new size of the territory
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Returns the map of units, where the key represents unit level and the value represents the unit count.
     *
     * @return the unit map
     */
    public Map<Integer, Integer> getUnitMap() {
        return unitMap;
    }

    /**
     * Helper method: Adds a specified number of units at a given level to the territory.
     *
     * @param level the level of the units to add
     * @param count the number of units to add
     */
    public void addUnits(int level, int count) {
        unitMap.put(level, unitMap.getOrDefault(level, 0) + count);
    }

    /**
     * Helper method: Removes a specified number of units at a given level.
     * Returns false if there aren't enough units to remove.
     *
     * @param level the level of the units to remove
     * @param count the number of units to remove
     * @return true if units are successfully removed; false otherwise
     */
    public boolean removeUnits(int level, int count) {
        int cur = unitMap.getOrDefault(level, 0);
        if (cur < count) return false;

        int newCount = cur - count;
        if (newCount == 0) {
            unitMap.remove(level);
        } else {
            unitMap.put(level, newCount);
        }
        return true;
    }

    /**
     * Returns the total number of units present in the territory.
     *
     * @return the sum of all unit counts in the territory
     */
    public int getTotalUnits() {
        int sum = 0;
        for (int c : unitMap.values()) {
            sum += c;
        }
        return sum;
    }

    /**
     * Returns the food production of the territory.
     * Both food and tech production are equal to the territory's size, though this formula can be customized.
     *
     * @return the amount of food produced
     */
    public int getFoodProduction() {
        return size;
    }

    /**
     * Returns the technology production of the territory.
     * Both food and tech production are equal to the territory's size, though this formula can be customized.
     *
     * @return the amount of technology produced
     */
    public int getTechProduction() {
        return size;
    }

    /**
     * Returns the list of neighboring territories.
     *
     * @return a list of neighbors
     */
    public List<Territory> getNeighbors() {
        return neighbors;
    }

    /**
     * Adds a neighbor to the territory if it is not already present.
     *
     * @param t the neighboring territory to add
     */
    public void addNeighbor(Territory t) {
        if (!neighbors.contains(t)) {
            neighbors.add(t);
        }
    }

    /**
     * Returns a string representation of the names of all neighboring territories.
     *
     * @return a space-separated string of neighbor territory names
     */
    public String neighborsString() {
        StringBuilder sb = new StringBuilder();
        for (Territory t : neighbors) {
            sb.append(t.name).append(" ");
        }
        return sb.toString().trim();
    }
}