package risc;

/**
 * Represents a generic Order.
 * Subclasses: MoveOrder, AttackOrder, etc.
 */
public abstract class Order {
    protected int playerID;
    protected String sourceName;
    protected String destName;
    protected int numUnits;

    public Order(int playerID, String sourceName, String destName, int numUnits) {
        this.playerID = playerID;
        this.sourceName = sourceName;
        this.destName = destName;
        this.numUnits = numUnits;
    }

    public int getPlayerID() {
        return playerID;
    }
    public String getSourceName() {
        return sourceName;
    }
    public String getDestName() {
        return destName;
    }
    public int getNumUnits() {
        return numUnits;
    }
}
