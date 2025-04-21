package risc;

public class DiceRollerTest {
    public static void main(String[] args) {
        testRollD20();
    }

    private static void testRollD20() {
        for (int i = 0; i < 100; i++) {
            int roll = DiceRoller.rollD20();
            assert roll >= 1 && roll <= 20 : "Roll should be between 1 and 20, got " + roll;
        }
    }
}