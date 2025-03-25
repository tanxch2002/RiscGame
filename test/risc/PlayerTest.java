package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    /**
     * Test retrieving the player's ID.
     */
    @Test
    void getId() {
        Player p = new Player(7, "TestPlayer");
        assertEquals(7, p.getId());
    }

    /**
     * Test retrieving the player's name.
     */
    @Test
    void getName() {
        Player p = new Player(7, "TestPlayer");
        assertEquals("TestPlayer", p.getName());
    }

    /**
     * Test checking if the player is alive.
     */
    @Test
    void isAlive() {
        Player p = new Player(7, "TestPlayer");
        assertTrue(p.isAlive());
    }

    /**
     * Test setting the player's alive status.
     */
    @Test
    void setAlive() {
        Player p = new Player(7, "TestPlayer");
        p.setAlive(false);
        assertFalse(p.isAlive());
    }

    /**
     * Test adding a territory to the player.
     */
    @Test
    void addTerritory() {
        Player p = new Player(7, "TestPlayer");
        Territory t = new Territory("A");
        p.addTerritory(t);
        assertEquals(1, p.getTerritories().size());
        // Adding the same territory again should not duplicate
        p.addTerritory(t);
        assertEquals(1, p.getTerritories().size());
    }

    /**
     * Test removing a territory from the player.
     */
    @Test
    void removeTerritory() {
        Player p = new Player(7, "TestPlayer");
        Territory t = new Territory("A");
        p.addTerritory(t);
        p.removeTerritory(t);
        assertTrue(p.getTerritories().isEmpty());
    }

    /**
     * Test retrieving the player's territories.
     */
    @Test
    void getTerritories() {
        Player p = new Player(7, "TestPlayer");
        assertNotNull(p.getTerritories());
        assertTrue(p.getTerritories().isEmpty());
    }
}