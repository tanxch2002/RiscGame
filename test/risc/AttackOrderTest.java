package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttackOrderTest {

    @Test
    void testConstructorAndGetters() {
        AttackOrder ao = new AttackOrder(1, "source", "dest", 10);
        assertEquals(1, ao.getPlayerID());
        assertEquals("source", ao.getSourceName());
        assertEquals("dest", ao.getDestName());
        assertEquals(10, ao.getNumUnits());
    }
}
