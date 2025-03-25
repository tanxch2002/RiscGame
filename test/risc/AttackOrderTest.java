package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttackOrderTest {

    /**
     * Test the constructor and getter methods of the AttackOrder class.
     * This test ensures that the AttackOrder object correctly initializes its fields
     * and that the getter methods return the expected values.
     */
    @Test
    void testConstructorAndGetters() {
        AttackOrder ao = new AttackOrder(1, "source", "dest", 10);
        assertEquals(1, ao.getPlayerID(), "Player ID should match the value passed to the constructor");
        assertEquals("source", ao.getSourceName(), "Source name should match the value passed to the constructor");
        assertEquals("dest", ao.getDestName(), "Destination name should match the value passed to the constructor");
        assertEquals(10, ao.getNumUnits(), "Number of units should match the value passed to the constructor");
    }
}