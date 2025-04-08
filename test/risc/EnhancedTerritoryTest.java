package risc;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EnhancedTerritoryTest {

    @Test
    void testSizeAttributes() {
        Territory t1 = new Territory("DefaultSize");
        assertEquals(1, t1.getSize()); // Default size

        Territory t2 = new Territory("CustomSize", 3);
        assertEquals(3, t2.getSize());

        t2.setSize(5);
        assertEquals(5, t2.getSize());
    }

    @Test
    void testResourceProduction() {
        Territory t = new Territory("ResourceTest", 4);

        // By default, production equals size
        assertEquals(4, t.getFoodProduction());
        assertEquals(4, t.getTechProduction());

        t.setSize(7);
        assertEquals(7, t.getFoodProduction());
        assertEquals(7, t.getTechProduction());
    }

    @Test
    void testUnitManagement() {
        Territory t = new Territory("UnitTest");
        Map<Integer, Integer> unitMap = t.getUnitMap();
        assertTrue(unitMap.isEmpty());

        // Add units of different levels
        t.addUnits(0, 10);
        t.addUnits(1, 5);
        t.addUnits(3, 2);

        assertEquals(10, unitMap.get(0));
        assertEquals(5, unitMap.get(1));
        assertEquals(2, unitMap.get(3));
        assertEquals(17, t.getTotalUnits());

        // Remove some units
        assertTrue(t.removeUnits(0, 4));
        assertEquals(6, unitMap.get(0));

        // Remove all units of a level
        assertTrue(t.removeUnits(3, 2));
        assertFalse(unitMap.containsKey(3));

        // Try to remove more units than available
        assertFalse(t.removeUnits(1, 10));
        assertEquals(5, unitMap.get(1));
    }
}