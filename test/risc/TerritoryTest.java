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
     * Test retrieving the number of units in a Territory (should be 0 initially).
     */
    @Test
    void getUnits() {
        Territory t = new Territory("A");
        assertEquals(0, t.getUnits());
    }

    /**
     * Test setting the number of units in a Territory.
     */
    @Test
    void setUnits() {
        Territory t = new Territory("A");
        t.setUnits(5);
        assertEquals(5, t.getUnits());
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