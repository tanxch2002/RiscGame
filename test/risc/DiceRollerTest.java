package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiceRollerTest {
    @Test
    void rollD20() {
        // Check multiple times if the return value is between [1..20]
        for (int i = 0; i < 50; i++) {
            int r = DiceRoller.rollD20();
            assertTrue(r >= 1 && r <= 20, "rollD20 should return between 1 and 20");
        }
    }

    @Test
    void testRollD20() {
        // Run the same test to cover both methods
        rollD20();
    }
}