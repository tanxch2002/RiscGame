package risc;

/**
 * Represents a move order in the game.
 * Now it also has a level field to specify which level units are moving.
 */
public class MoveOrder extends Order {
    private final int level;

    /**
     * @param playerID ID of the player initiating the move
     * @param source   The source territory
     * @param dest     The destination territory
     * @param level    The level of the units to be moved
     * @param numUnits The number of units of the specified level to move
     */
    public MoveOrder(int playerID, String source, String dest, int level, int numUnits) {
        super(playerID, source, dest, numUnits);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
