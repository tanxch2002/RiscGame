package risc;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single player in the game.
 */
public class Player {
    private final int id;
    private final String name;
    private boolean alive;
    private final List<Territory> territories;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.alive = true;
        this.territories = new ArrayList<>();
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public boolean isAlive() {
        return alive;
    }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    public void addTerritory(Territory t) {
        if (!territories.contains(t)) {
            territories.add(t);
        }
    }
    public void removeTerritory(Territory t) {
        territories.remove(t);
    }
    public List<Territory> getTerritories() {
        return territories;
    }
}
