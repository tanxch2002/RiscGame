package risc;

public class AIPlayerTest {
    public static void main(String[] args) {
        testConstructorAndIsAI();
    }

    private static void testConstructorAndIsAI() {
        AIPlayer player = new AIPlayer(1, "TestAI");

        assert player.getId() == 1 : "ID should be 1";
        assert "TestAI".equals(player.getName()) : "Name should be TestAI";
        assert player.isAI() : "isAI should return true";
    }
}