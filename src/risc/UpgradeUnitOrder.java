package risc;

/**
 * Represents an order to upgrade units.
 * For example, upgrade a specified number of units from currentLevel to targetLevel
 * in the territory identified by territoryName.
 */
public class UpgradeUnitOrder extends Order {
    private final int currentLevel;
    private final int targetLevel;

    /**
     * Constructs an order to upgrade units.
     *
     * @param playerID      the ID of the player issuing the order
     * @param territoryName the territory in which the upgrade operation takes place
     * @param currentLevel  the current level of the units
     * @param targetLevel   the target level for the units after upgrade
     * @param numUnits      the number of units to upgrade
     */
    public UpgradeUnitOrder(int playerID,
                            String territoryName,
                            int currentLevel,
                            int targetLevel,
                            int numUnits) {
        super(playerID, territoryName, territoryName, numUnits);
        this.currentLevel = currentLevel;
        this.targetLevel = targetLevel;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
