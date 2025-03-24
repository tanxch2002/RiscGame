package risc;

import java.util.Random;

/**
 * Simple utility class for dice rolling
 */
public class DiceRoller {
    private static final Random RAND = new Random();

    public static int rollD20() {
        // returns integer [1..20]
        return RAND.nextInt(20) + 1;
    }
}
