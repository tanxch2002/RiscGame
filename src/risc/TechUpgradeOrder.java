package risc;

/**
 * Issues an order for a maximum technology level upgrade.
 * For example, upgrading from techLevel=1 to 2 (costing 50 tech points), which takes effect in the next turn.
 */
public class TechUpgradeOrder extends Order {

    public TechUpgradeOrder(int playerID) {
        // For this order, sourceName, destName, and numUnits are not required, so pass null or 0.
        super(playerID, null, null, 0);
    }
}
