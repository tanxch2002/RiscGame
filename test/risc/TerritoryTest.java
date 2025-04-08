package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TerritoryTest {

    /**
     * Test retrieving the name of a Territory.
     */
    @Test
    void getName() {
        Territory t = new Territory("A");
        assertEquals("A", t.getName());
    }

    /**
     * Test retrieving the owner of a Territory (should be null initially).
     */
    @Test
    void getOwner() {
        Territory t = new Territory("A");
        assertNull(t.getOwner());
    }

    /**
     * Test setting the owner of a Territory.
     */
    @Test
    void setOwner() {
        Territory t = new Territory("A");
        Player p = new Player(0, "Tester");
        t.setOwner(p);
        assertEquals(p, t.getOwner());
    }

    /**
     * Test retrieving the units in a Territory (should be 0 initially for all levels).
     */
    @Test
    void testInitialUnits() {
        Territory t = new Territory("A");
        assertEquals(0, t.getTotalUnits());
        assertTrue(t.getUnitMap().isEmpty());
    }

    /**
     * Test adding and removing units.
     */
    @Test
    void testAddRemoveUnits() {
        Territory t = new Territory("A");
        t.addUnits(0, 5);
        assertEquals(5, t.getUnitMap().get(0));
        assertEquals(5, t.getTotalUnits());

        // Add more units of the same level
        t.addUnits(0, 3);
        assertEquals(8, t.getUnitMap().get(0));

        // Add units of a different level
        t.addUnits(1, 2);
        assertEquals(8, t.getUnitMap().get(0));
        assertEquals(2, t.getUnitMap().get(1));
        assertEquals(10, t.getTotalUnits());

        // Remove units
        assertTrue(t.removeUnits(0, 3));
        assertEquals(5, t.getUnitMap().get(0));
        assertEquals(7, t.getTotalUnits());

        // Try to remove more units than available
        assertFalse(t.removeUnits(1, 3));
        assertEquals(2, t.getUnitMap().get(1));
    }

    /**
     * Test retrieving the neighbors of a Territory (should be empty initially).
     */
    @Test
    void getNeighbors() {
        Territory t = new Territory("A");
        assertNotNull(t.getNeighbors());
        assertTrue(t.getNeighbors().isEmpty());
    }

    /**
     * Test adding a neighbor to a Territory.
     */
    @Test
    void addNeighbor() {
        Territory t = new Territory("A");
        Territory t2 = new Territory("B");
        t.addNeighbor(t2);
        assertEquals(1, t.getNeighbors().size());
        // Adding the same neighbor again should not duplicate
        t.addNeighbor(t2);
        assertEquals(1, t.getNeighbors().size());
    }

    /**
     * Test generating a string representation of a Territory's neighbors.
     */
    @Test
    void neighborsString() {
        Territory t = new Territory("A");
        Territory t2 = new Territory("B");
        Territory t3 = new Territory("C");
        t.addNeighbor(t2);
        t.addNeighbor(t3);

        String res = t.neighborsString();
        assertTrue(res.contains("B"));
        assertTrue(res.contains("C"));
    }
}