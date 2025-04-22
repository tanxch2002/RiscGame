package risc;

import java.util.*;

/**
 * Represents a game player, managing their state, resources, territories, and alliances.
 */
public class Player {
    private final int id;
    private String name;
    private boolean alive;
    private final List<Territory> territories;

    private int food;
    private int tech;

    private int maxTechLevel = 1;
    private boolean isTechUpgrading = false;
    private int nextTechLevel = 1;

    // Alliance relationships
    private final Set<Integer> allies;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.alive = true;
        this.territories = new ArrayList<>();
        this.food = 100;
        this.tech = 0;
        this.allies = new HashSet<>();
    }

    /* ---------- Basic Information ---------- */
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String newName) { this.name = newName; }

    /* ---------- AI Identification: default is human ---------- */
    public boolean isAI() { return false; }

    /* ---------- Survival State & Territories ---------- */
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    public List<Territory> getTerritories() { return territories; }
    public void addTerritory(Territory t) { if (!territories.contains(t)) territories.add(t); }
    public void removeTerritory(Territory t) { territories.remove(t); }

    /* ---------- Resources ---------- */
    public int getFood() { return food; }
    public int getTech() { return tech; }
    public void addFood(int delta) { this.food += delta; }
    public void addTech(int delta) { this.tech += delta; }
    public boolean spendFood(int amt) { if (food < amt) return false; food -= amt; return true; }
    public boolean spendTech(int amt) { if (tech < amt) return false; tech -= amt; return true; }

    /* ---------- Technology ---------- */
    public int getMaxTechLevel() { return maxTechLevel; }
    public boolean isTechUpgrading() { return isTechUpgrading; }
    public void startTechUpgrade(int nextLevel) { isTechUpgrading = true; nextTechLevel = nextLevel; }
    public void finishTechUpgrade() {
        isTechUpgrading = false;
        maxTechLevel = nextTechLevel;
    }

    /* ---------- Alliances ---------- */
    public void addAlly(int otherID) { allies.add(otherID); }
    public void removeAlly(int otherID) { allies.remove(otherID); }
    public boolean isAlliedWith(int otherID) { return allies.contains(otherID); }
    public Set<Integer> getAllies() { return allies; }
}
