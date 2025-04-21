package risc;

public class UpgradeUnitOrderTest {
    public static void main(String[] args) {
        testConstructorAndGetters();
    }

    private static void testConstructorAndGetters() {
        UpgradeUnitOrder order = new UpgradeUnitOrder(1, "A", 0, 2, 5);

        assert order.getPlayerID() == 1 : "Player ID should be 1";
        assert "A".equals(order.getSourceName()) : "Source name should be A";
        assert "A".equals(order.getDestName()) : "Destination name should also be A (same territory)";
        assert order.getCurrentLevel() == 0 : "Current level should be 0";
        assert order.getTargetLevel() == 2 : "Target level should be 2";
        assert order.getNumUnits() == 5 : "Number of units should be 5";
    }
}