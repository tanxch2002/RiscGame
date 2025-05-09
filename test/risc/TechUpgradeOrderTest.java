package risc;

public class TechUpgradeOrderTest {
    public static void main(String[] args) {
        testConstructorAndGetters();
    }

    private static void testConstructorAndGetters() {
        TechUpgradeOrder order = new TechUpgradeOrder(1);

        assert order.getPlayerID() == 1 : "Player ID should be 1";
        assert order.getSourceName() == null : "Source name should be null";
        assert order.getDestName() == null : "Destination name should be null";
        assert order.getNumUnits() == 0 : "Number of units should be 0";
    }
}