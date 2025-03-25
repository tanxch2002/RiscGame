package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TerritoryTest {

    @Test
    void getName() {
        Territory t = new Territory("A");
        assertEquals("A", t.getName());
    }

    @Test
    void getOwner() {
        Territory t = new Territory("A");
        assertNull(t.getOwner());
    }

    @Test
    void setOwner() {
        Territory t = new Territory("A");
        Player p = new Player(0, "Tester");
        t.setOwner(p);
        assertEquals(p, t.getOwner());
    }

    @Test
    void getUnits() {
        Territory t = new Territory("A");
        assertEquals(0, t.getUnits());
    }

    @Test
    void setUnits() {
        Territory t = new Territory("A");
        t.setUnits(5);
        assertEquals(5, t.getUnits());
    }

    @Test
    void getNeighbors() {
        Territory t = new Territory("A");
        assertNotNull(t.getNeighbors());
        assertTrue(t.getNeighbors().isEmpty());
    }

    @Test
    void addNeighbor() {
        Territory t = new Territory("A");
        Territory t2 = new Territory("B");
        t.addNeighbor(t2);
        assertEquals(1, t.getNeighbors().size());
        // 再次添加同样邻居，不应重复
        t.addNeighbor(t2);
        assertEquals(1, t.getNeighbors().size());
    }

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
