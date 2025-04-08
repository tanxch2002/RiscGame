package risc;

import java.util.ArrayList;
import java.util.List;

public class Player {
    // Unique player identifier (immutable)
    private final int id;
    // Player name, which can be changed with setName (for updating username in RiscServer)
    private String name;
    // Indicates whether the player is still active in the game
    private boolean alive;
    // A list of territories that belong to the player
    private final List<Territory> territories;

    // Resource fields representing the player's food and technology points
    private int food;
    private int tech;

    // Fields related to technology upgrade:
    // maxTechLevel: the current maximum technology level of the player.
    private int maxTechLevel = 1;
    // isTechUpgrading: indicates if a technology upgrade is in progress.
    private boolean isTechUpgrading = false;
    // nextTechLevel: the technology level the player will achieve after upgrade completion.
    private int nextTechLevel = 1;

    /**
     * Constructs a new Player with the specified id and name.
     *
     * @param id   unique identifier for the player
     * @param name initial name of the player
     */
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.alive = true;
        this.territories = new ArrayList<>();
        this.food = 0;
        this.tech = 0;
    }

    /**
     * Returns the unique identifier of the player.
     *
     * @return player's id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the current name of the player.
     *
     * @return player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new name for the player.
     *
     * @param newName the new name to assign to the player
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Checks if the player is still active in the game.
     *
     * @return true if player is alive; false otherwise
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Sets the alive status of the player.
     *
     * @param alive the new status for the player (true if alive, false if not)
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Retrieves the list of territories controlled by the player.
     *
     * @return list of the player's territories
     */
    public List<Territory> getTerritories() {
        return territories;
    }

    /**
     * Adds a territory to the player's list of controlled territories.
     * Ensures no duplicate territories are added.
     *
     * @param t the territory to add
     */
    public void addTerritory(Territory t) {
        if (!territories.contains(t)) {
            territories.add(t);
        }
    }

    /**
     * Removes a territory from the player's list of controlled territories.
     *
     * @param t the territory to remove
     */
    public void removeTerritory(Territory t) {
        territories.remove(t);
    }

    // ==== Resource Management Methods ====

    /**
     * Retrieves the current amount of food resource.
     *
     * @return available food points
     */
    public int getFood() {
        return food;
    }

    /**
     * Retrieves the current amount of technology resource.
     *
     * @return available technology points
     */
    public int getTech() {
        return tech;
    }

    /**
     * Increases the food resource by a specified amount.
     *
     * @param delta amount of food to add
     */
    public void addFood(int delta) {
        this.food += delta;
    }

    /**
     * Increases the technology resource by a specified amount.
     *
     * @param delta amount of technology points to add
     */
    public void addTech(int delta) {
        this.tech += delta;
    }

    /**
     * Attempts to deduct a specified amount of food from the player's resources.
     *
     * @param amt the amount of food to spend
     * @return true if the operation is successful; false if not enough food is available
     */
    public boolean spendFood(int amt) {
        if (food < amt) return false;
        food -= amt;
        return true;
    }

    /**
     * Attempts to deduct a specified amount of technology points from the player's resources.
     *
     * @param amt the amount of technology points to spend
     * @return true if the operation is successful; false if not enough tech is available
     */
    public boolean spendTech(int amt) {
        if (tech < amt) return false;
        tech -= amt;
        return true;
    }

    // ==== Technology Upgrade Methods ====

    /**
     * Returns the maximum technology level the player has reached.
     *
     * @return current maximum technology level
     */
    public int getMaxTechLevel() {
        return maxTechLevel;
    }

    /**
     * Checks whether a technology upgrade is currently underway.
     *
     * @return true if an upgrade is in progress; false otherwise
     */
    public boolean isTechUpgrading() {
        return isTechUpgrading;
    }

    /**
     * Initiates a technology upgrade to a specified next level.
     *
     * @param nextLevel the technology level to be achieved after upgrade
     */
    public void startTechUpgrade(int nextLevel) {
        this.isTechUpgrading = true;
        this.nextTechLevel = nextLevel;
    }

    /**
     * Completes the technology upgrade, updating the maximum technology level.
     */
    public void finishTechUpgrade() {
        this.isTechUpgrading = false;
        this.maxTechLevel = nextTechLevel;
    }
}
