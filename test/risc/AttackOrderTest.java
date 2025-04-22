package risc;

public class AttackOrderTest {
    public static void main(String[] args) {
        testConstructorAndGetters();
    }

    private static void testConstructorAndGetters() {
        AttackOrder order = new AttackOrder(1, "A", "B", 2, 5);

        assert order.getPlayerID() == 1 : "Player ID should be 1";
        assert "A".equals(order.getSourceName()) : "Source name should be A";
        assert "B".equals(order.getDestName()) : "Destination name should be B";
        assert order.getLevel() == 2 : "Level should be 2";
        assert order.getNumUnits() == 5 : "Number of units should be 5";
    }
}