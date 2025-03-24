package risc;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a territory on the map.
 */
public class Territory {
    private final String name;
    private Player owner;
    private int units;
    private final List<Territory> neighbors;

    public Territory(String name) {
        this.name = name;
        this.neighbors = new ArrayList<>();
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
    public int getUnits() {
        return units;
    }
    public void setUnits(int units) {
        this.units = units;
    }

    public List<Territory> getNeighbors() {
        return neighbors;
    }
    public void addNeighbor(Territory t) {
        if (!neighbors.contains(t)) {
            neighbors.add(t);
        }
    }

    public String neighborsString() {
        StringBuilder sb = new StringBuilder();
        for (Territory t : neighbors) {
            sb.append(t.name).append(" ");
        }
        return sb.toString().trim();
    }
}
