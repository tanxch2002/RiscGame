package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TechUpgradeOrderTest {

    @Test
    void testConstructor() {
        TechUpgradeOrder order = new TechUpgradeOrder(3);

        assertEquals(3, order.getPlayerID());
        assertNull(order.getSourceName());
        assertNull(order.getDestName());
        assertEquals(0, order.getNumUnits());
    }

    @Test
    void testInheritance() {
        TechUpgradeOrder order = new TechUpgradeOrder(2);
        assertTrue(order instanceof Order);
    }
}