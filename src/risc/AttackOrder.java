package risc;

/**
 * Represents an attack order in the game.
 * This class extends the Order class and specifies the details of an attack,
 * including the player ID, source territory, destination territory, and number of units.
 */
public class AttackOrder extends Order {

    public AttackOrder(int playerID, String source, String dest, int numUnits) {
        super(playerID, source, dest, numUnits);
    }
}