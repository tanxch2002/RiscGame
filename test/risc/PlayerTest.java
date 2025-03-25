package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void getId() {
        Player p = new Player(7, "TestPlayer");
        assertEquals(7, p.getId());
    }

    @Test
    void getName() {
        Player p = new Player(7, "TestPlayer");
        assertEquals("TestPlayer", p.getName());
    }

    @Test
    void isAlive() {
        Player p = new Player(7, "TestPlayer");
        assertTrue(p.isAlive());
    }

    @Test
    void setAlive() {
        Player p = new Player(7, "TestPlayer");
        p.setAlive(false);
        assertFalse(p.isAlive());
    }

    @Test
    void addTerritory() {
        Player p = new Player(7, "TestPlayer");
        Territory t = new Territory("A");
        p.addTerritory(t);
        assertEquals(1, p.getTerritories().size());
        // 再次添加同一领地，不应该重复
        p.addTerritory(t);
        assertEquals(1, p.getTerritories().size());
    }

    @Test
    void removeTerritory() {
        Player p = new Player(7, "TestPlayer");
        Territory t = new Territory("A");
        p.addTerritory(t);
        p.removeTerritory(t);
        assertTrue(p.getTerritories().isEmpty());
    }

    @Test
    void getTerritories() {
        Player p = new Player(7, "TestPlayer");
        assertNotNull(p.getTerritories());
        assertTrue(p.getTerritories().isEmpty());
    }
}
