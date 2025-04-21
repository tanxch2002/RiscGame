package risc;

public class OrderTest {
    public static void main(String[] args) {
        testOrderBaseProperties();
    }

    private static void testOrderBaseProperties() {
        Order order = new MoveOrder(1, "A", "B", 0, 5);

        assert order.getPlayerID() == 1 : "Player ID should be 1";
        assert "A".equals(order.getSourceName()) : "Source name should be A";
        assert "B".equals(order.getDestName()) : "Destination name should be B";
        assert order.getNumUnits() == 5 : "Number of units should be 5";
    }
}