package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnhancedMoveOrderTest {

    @Test
    void testConstructorWithLevel() {
        MoveOrder mo = new MoveOrder(2, "X", "Y", 3, 7);
        assertEquals(2, mo.getPlayerID());
        assertEquals("X", mo.getSourceName());
        assertEquals("Y", mo.getDestName());
        assertEquals(7, mo.getNumUnits());
        assertEquals(3, mo.getLevel());
    }

    @Test
    void testCompareMoveOrders() {
        MoveOrder mo1 = new MoveOrder(1, "A", "B", 0, 5);
        MoveOrder mo2 = new MoveOrder(1, "A", "B", 2, 5);

        // Different level
        assertNotEquals(mo1.getLevel(), mo2.getLevel());

        // Same player, source, dest, and units
        assertEquals(mo1.getPlayerID(), mo2.getPlayerID());
        assertEquals(mo1.getSourceName(), mo2.getSourceName());
        assertEquals(mo1.getDestName(), mo2.getDestName());
        assertEquals(mo1.getNumUnits(), mo2.getNumUnits());
    }
}