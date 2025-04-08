package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UpgradeUnitOrderTest {

    @Test
    void testConstructorAndGetters() {
        UpgradeUnitOrder order = new UpgradeUnitOrder(1, "Narnia", 0, 2, 5);

        assertEquals(1, order.getPlayerID());
        assertEquals("Narnia", order.getSourceName());
        assertEquals("Narnia", order.getDestName()); // Source and dest are the same
        assertEquals(5, order.getNumUnits());
        assertEquals(0, order.getCurrentLevel());
        assertEquals(2, order.getTargetLevel());
    }

    @Test
    void testInheritance() {
        UpgradeUnitOrder order = new UpgradeUnitOrder(2, "Midkemia", 1, 3, 2);
        assertTrue(order instanceof Order);
    }
}