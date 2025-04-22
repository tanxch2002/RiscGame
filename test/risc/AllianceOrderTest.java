package risc;

public class AllianceOrderTest {
    public static void main(String[] args) {
        testConstructorAndGetters();
    }

    private static void testConstructorAndGetters() {
        AllianceOrder order = new AllianceOrder(1, "Player2");

        assert order.getPlayerID() == 1 : "Player ID should be 1";
        assert "Player2".equals(order.getTargetPlayerName()) : "Target player name should be Player2";
        assert order.getSourceName() == null : "Source name should be null";
        assert order.getDestName() == null : "Destination name should be null";
        assert order.getNumUnits() == 0 : "Number of units should be 0";
    }
}