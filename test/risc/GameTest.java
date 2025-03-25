package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void setUpMap() {
        Game g = new Game();
        g.setUpMap(3); // Set up 6 territories
        Territory tA = g.getTerritoryByName("A");
        assertNotNull(tA);
        assertNull(g.getTerritoryByName("Z")); // Non-existent territory
    }

    @Test
    void initPlayers() {
        Game g = new Game();
        g.setUpMap(3);
        g.initPlayers(3);
        assertEquals("Player1", g.getPlayer(0).getName());
        assertEquals("Player2", g.getPlayer(1).getName());
        assertEquals("Player3", g.getPlayer(2).getName());
    }

    @Test
    void getInitialUnits() {
        Game g = new Game();
        assertEquals(10, g.getInitialUnits());
    }

    @Test
    void distributeInitialUnits() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);
        g.distributeInitialUnits();
        assertTrue(true); // No exception means distribution is complete
    }

    @Test
    void addOrder() {
        Game g = new Game();
        Order o = new MoveOrder(0, "A", "B", 5);
        g.addOrder(o);
        assertTrue(g.getAllOrders().contains(o));
    }

    @Test
    void executeAllMoveOrders() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);

        Player p0 = g.getPlayer(0);
        Territory tA = p0.getTerritories().get(0);
        Territory tB = p0.getTerritories().get(1);
        tA.setUnits(10);

        g.addOrder(new MoveOrder(0, tA.getName(), tB.getName(), 5));
        g.executeAllMoveOrders();
        assertEquals(5, tA.getUnits());
        assertEquals(5, tB.getUnits());
    }

    @Test
    void executeAllAttackOrders() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);

        Player p0 = g.getPlayer(0);
        Player p1 = g.getPlayer(1);
        Territory tA = p0.getTerritories().get(0);
        Territory tB = p1.getTerritories().get(0);
        tA.setUnits(10);
        tB.setUnits(5);

        g.addOrder(new AttackOrder(0, tA.getName(), tB.getName(), 4));
        g.executeAllAttackOrders();
        assertTrue(tA.getUnits() >= 6); // Since 4 units were used for attack
    }

    @Test
    void clearAllOrders() {
        Game g = new Game();
        g.addOrder(new MoveOrder(0, "A", "B", 5));
        g.clearAllOrders();
        assertTrue(g.getAllOrders().isEmpty());
    }

    @Test
    void addOneUnitToEachTerritory() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);

        for (Territory t : g.getPlayer(0).getTerritories()) {
            t.setUnits(1);
        }
        for (Territory t : g.getPlayer(1).getTerritories()) {
            t.setUnits(2);
        }

        g.addOneUnitToEachTerritory();

        for (Territory t : g.getPlayer(0).getTerritories()) {
            assertEquals(2, t.getUnits());
        }
        for (Territory t : g.getPlayer(1).getTerritories()) {
            assertEquals(3, t.getUnits());
        }
    }

    @Test
    void hasWinner() {
        Game g = new Game();
        assertFalse(g.hasWinner());
    }

    @Test
    void getWinner() {
        Game g = new Game();
        assertNull(g.getWinner());
    }

    @Test
    void getTerritoryByName() {
        Game g = new Game();
        g.setUpMap(2);
        assertNotNull(g.getTerritoryByName("A"));
        assertNull(g.getTerritoryByName("Z"));
    }

    @Test
    void getPlayer() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);
        Player p = g.getPlayer(1);
        assertEquals("Player2", p.getName());
    }

    @Test
    void getAllOrders() {
        Game g = new Game();
        assertNotNull(g.getAllOrders());
    }

    @Test
    void getRandom() {
        Game g = new Game();
        assertNotNull(g.getRandom());
    }

    @Test
    void getMapState() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);
        String mapState = g.getMapState();
        assertTrue(mapState.contains("A(")); // Assert that the string contains "A("
    }
}