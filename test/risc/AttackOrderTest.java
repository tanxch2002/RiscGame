package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnhancedAttackOrderTest {

    @Test
    void testConstructorWithLevel() {
        AttackOrder ao = new AttackOrder(1, "source", "dest", 2, 10);
        assertEquals(1, ao.getPlayerID());
        assertEquals("source", ao.getSourceName());
        assertEquals("dest", ao.getDestName());
        assertEquals(10, ao.getNumUnits());
        assertEquals(2, ao.getLevel());
    }

    @Test
    void testCompareAttackOrders() {
        AttackOrder ao1 = new AttackOrder(1, "A", "B", 0, 5);
        AttackOrder ao2 = new AttackOrder(1, "A", "B", 2, 5);

        // Different level
        assertNotEquals(ao1.getLevel(), ao2.getLevel());

        // Same player, source, dest, and units
        assertEquals(ao1.getPlayerID(), ao2.getPlayerID());
        assertEquals(ao1.getSourceName(), ao2.getSourceName());
        assertEquals(ao1.getDestName(), ao2.getDestName());
        assertEquals(ao1.getNumUnits(), ao2.getNumUnits());
    }
}