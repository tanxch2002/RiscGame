package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveOrderTest {

    /**
     * Test the constructor and getter methods of the MoveOrder class.
     * This test ensures that the MoveOrder object correctly initializes its fields
     * and that the getter methods return the expected values.
     */
    @Test
    void testConstructorAndGetters() {
        MoveOrder mo = new MoveOrder(2, "X", "Y", 7);
        assertEquals(2, mo.getPlayerID(), "Player ID should match the value passed to the constructor");
        assertEquals("X", mo.getSourceName(), "Source name should match the value passed to the constructor");
        assertEquals("Y", mo.getDestName(), "Destination name should match the value passed to the constructor");
        assertEquals(7, mo.getNumUnits(), "Number of units should match the value passed to the constructor");
    }
}