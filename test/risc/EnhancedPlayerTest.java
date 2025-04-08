package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnhancedPlayerTest {

    @Test
    void testResourceManagement() {
        Player p = new Player(1, "TestPlayer");
        assertEquals(0, p.getFood());
        assertEquals(0, p.getTech());

        p.addFood(50);
        p.addTech(75);
        assertEquals(50, p.getFood());
        assertEquals(75, p.getTech());

        assertTrue(p.spendFood(30));
        assertEquals(20, p.getFood());

        assertTrue(p.spendTech(50));
        assertEquals(25, p.getTech());

        assertFalse(p.spendFood(30)); // Not enough food
        assertEquals(20, p.getFood()); // Food unchanged

        assertFalse(p.spendTech(30)); // Not enough tech
        assertEquals(25, p.getTech()); // Tech unchanged
    }

    @Test
    void testTechUpgradeSystem() {
        Player p = new Player(1, "TestPlayer");

        // Initial tech level
        assertEquals(1, p.getMaxTechLevel());
        assertFalse(p.isTechUpgrading());

        // Start tech upgrade
        p.startTechUpgrade(2);
        assertTrue(p.isTechUpgrading());
        assertEquals(1, p.getMaxTechLevel()); // Not changed yet

        // Complete tech upgrade
        p.finishTechUpgrade();
        assertFalse(p.isTechUpgrading());
        assertEquals(2, p.getMaxTechLevel()); // Now upgraded

        // Another tech upgrade
        p.startTechUpgrade(3);
        assertTrue(p.isTechUpgrading());
        p.finishTechUpgrade();
        assertEquals(3, p.getMaxTechLevel());
    }
}