package risc;

/**
 * Represents a move order in the game.
 * This class extends the Order class and specifies the details of a move,
 * including the player ID, source territory, destination territory, and number of units.
 */
public class MoveOrder extends Order {

    public MoveOrder(int playerID, String source, String dest, int numUnits) {
        super(playerID, source, dest, numUnits);
    }
}