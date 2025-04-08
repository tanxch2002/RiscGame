package risc;

/**
 * Represents an attack order in the game.
 * This class extends the Order class and specifies the details of an attack,
 * including the player ID, source territory, destination territory, number of units,
 * and the level of attacking units.
 */
public class AttackOrder extends Order {
    private final int level;

    /**
     * Constructs an AttackOrder.
     *
     * @param playerID the ID of the attacking player
     * @param source   the source territory
     * @param dest     the destination territory
     * @param level    the level of the attacking units
     * @param numUnits the number of units at the specified level
     */
    public AttackOrder(int playerID, String source, String dest, int level, int numUnits) {
        super(playerID, source, dest, numUnits);
        this.level = level;
    }

    /**
     * Returns the level of the attacking units.
     *
     * @return the level of the attacking units
     */
    public int getLevel() {
        return level;
    }
}
