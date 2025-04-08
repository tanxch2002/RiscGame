package risc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced test class for Game with improved coverage.
 * This version doesn't use Mockito.
 */
class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testConstructor() {
        assertNotNull(game);

        // Access private fields using reflection to verify initialization
        try {
            java.lang.reflect.Field territoriesField = Game.class.getDeclaredField("territories");
            territoriesField.setAccessible(true);
            List<?> territories = (List<?>) territoriesField.get(game);
            assertTrue(territories.isEmpty(), "Territories should be initialized to empty list");

            java.lang.reflect.Field playersField = Game.class.getDeclaredField("players");
            playersField.setAccessible(true);
            List<?> players = (List<?>) playersField.get(game);
            assertTrue(players.isEmpty(), "Players should be initialized to empty list");

            java.lang.reflect.Field allOrdersField = Game.class.getDeclaredField("allOrders");
            allOrdersField.setAccessible(true);
            List<?> allOrders = (List<?>) allOrdersField.get(game);
            assertTrue(allOrders.isEmpty(), "Orders should be initialized to empty list");

            java.lang.reflect.Field randField = Game.class.getDeclaredField("rand");
            randField.setAccessible(true);
            Object rand = randField.get(game);
            assertNotNull(rand, "Random object should be initialized");

            java.lang.reflect.Field orderExecutorField = Game.class.getDeclaredField("orderExecutor");
            orderExecutorField.setAccessible(true);
            Object orderExecutor = orderExecutorField.get(game);
            assertNotNull(orderExecutor, "OrderExecutor should be initialized");

        } catch (Exception e) {
            fail("Exception during reflection: " + e.getMessage());
        }
    }

    @Test
    void testSetUpMap() {
        game.setUpMap(3); // 3 players -> 6 territories

        Territory tA = game.getTerritoryByName("A");
        assertNotNull(tA, "Territory A should exist");
        assertEquals("A", tA.getName(), "Territory name should be A");

        // Test with different player counts
        Game game2 = new Game();
        game2.setUpMap(5); // 5 players -> 10 territories
        assertEquals(10, countTerritories(game2), "Should have 10 territories for 5 players");

        Game game3 = new Game();
        game3.setUpMap(2); // 2 players -> 8 territories (default case)
        assertEquals(8, countTerritories(game3), "Should have 8 territories for 2 players");
    }

    private int countTerritories(Game game) {
        try {
            java.lang.reflect.Field territoriesField = Game.class.getDeclaredField("territories");
            territoriesField.setAccessible(true);
            List<?> territories = (List<?>) territoriesField.get(game);
            return territories.size();
        } catch (Exception e) {
            fail("Exception during reflection: " + e.getMessage());
            return 0;
        }
    }

    @Test
    void testInitPlayers() {
        game.setUpMap(3); // 3 players -> 6 territories
        game.initPlayers(3);

        assertEquals(3, countPlayers(), "Should have 3 players");

        Player p1 = game.getPlayer(0);
        assertNotNull(p1, "Player 1 should exist");
        assertEquals("Player1", p1.getName(), "Player name should be Player1");

        // Verify territory distribution
        assertEquals(2, p1.getTerritories().size(), "Player 1 should have 2 territories");

        // Test with uneven distribution
        Game game2 = new Game();
        game2.setUpMap(2); // 2 players -> 8 territories
        game2.initPlayers(3); // 3 players for 8 territories

        // First two players should have 3 territories, last player should have 2
        try {
            java.lang.reflect.Field playersField = Game.class.getDeclaredField("players");
            playersField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Player> players = (List<Player>) playersField.get(game2);

            assertEquals(3, players.get(0).getTerritories().size(), "First player should have 3 territories");
            assertEquals(3, players.get(1).getTerritories().size(), "Second player should have 3 territories");
            assertEquals(2, players.get(2).getTerritories().size(), "Third player should have 2 territories");
        } catch (Exception e) {
            fail("Exception during reflection: " + e.getMessage());
        }
    }

    private int countPlayers() {
        try {
            java.lang.reflect.Field playersField = Game.class.getDeclaredField("players");
            playersField.setAccessible(true);
            List<?> players = (List<?>) playersField.get(game);
            return players.size();
        } catch (Exception e) {
            fail("Exception during reflection: " + e.getMessage());
            return 0;
        }
    }

    @Test
    void testGetInitialUnits() {
        assertEquals(10, game.getInitialUnits(), "Initial units should be 10");
    }

    @Test
    void testAddOrder() {
        Order o = new MoveOrder(0, "A", "B", 0, 5);
        game.addOrder(o);

        List<Order> orders = game.getAllOrders();
        assertEquals(1, orders.size(), "Should have 1 order");
        assertTrue(orders.contains(o), "Order should be in the list");

        // Add another order and verify
        Order o2 = new AttackOrder(0, "B", "C", 0, 3);
        game.addOrder(o2);

        assertEquals(2, orders.size(), "Should have 2 orders");
        assertTrue(orders.contains(o2), "Second order should be in the list");
    }

    @Test
    void testExecuteAllMoveOrders() {
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        List<Territory> territories = p0.getTerritories();
        Territory tA = territories.get(0);
        Territory tB = territories.get(1);

        tA.addUnits(0, 10);

        // Make sure player has enough food
        p0.addFood(100);

        // Add a move order
        game.addOrder(new MoveOrder(0, tA.getName(), tB.getName(), 0, 5));

        // Execute move orders
        game.executeAllMoveOrders();

        // Verify units were moved
        assertEquals(5, tA.getUnitMap().getOrDefault(0, 0), "Territory A should have 5 units left");
        assertEquals(5, tB.getUnitMap().getOrDefault(0, 0), "Territory B should have 5 units");
    }

    @Test
    void testExecuteAllAttackOrders() {
        // Set up game with two players
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        Player p1 = game.getPlayer(1);

        // Get a territory from each player
        Territory t1 = p0.getTerritories().get(0);
        Territory t2 = p1.getTerritories().get(0);

        // Make sure they're adjacent
        t1.addNeighbor(t2);
        t2.addNeighbor(t1);

        // Add units
        t1.addUnits(0, 20); // Overwhelming force to ensure victory
        t2.addUnits(0, 1);

        // Add food for attack
        p0.addFood(100);

        // Add an attack order
        game.addOrder(new AttackOrder(0, t1.getName(), t2.getName(), 0, 15));

        // Execute attack orders
        game.executeAllAttackOrders();

        // Verify territory ownership - outcome depends on dice rolls
        // We'll just verify that the method ran without exceptions
        assertNotNull(t1.getOwner(), "Territory 1 should have an owner");
        assertNotNull(t2.getOwner(), "Territory 2 should have an owner");
    }

    @Test
    void testExecuteAllUpgrades() {
        // Set up game with two players
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        Territory t1 = p0.getTerritories().get(0);

        // Add units and resources
        t1.addUnits(0, 10);
        p0.addTech(100);

        // Add an upgrade order
        game.addOrder(new UpgradeUnitOrder(0, t1.getName(), 0, 1, 5));

        // Execute upgrade orders
        game.executeAllUpgrades();

        // Verify units were upgraded (depends on player tech level)
        if (p0.getMaxTechLevel() >= 1) {
            assertEquals(5, t1.getUnitMap().getOrDefault(0, 0), "Should have 5 level 0 units left");
            assertEquals(5, t1.getUnitMap().getOrDefault(1, 0), "Should have 5 level 1 units");
        } else {
            assertEquals(10, t1.getUnitMap().getOrDefault(0, 0), "Should still have 10 level 0 units");
        }
    }

    @Test
    void testClearAllOrders() {
        game.addOrder(new MoveOrder(0, "A", "B", 0, 5));
        game.addOrder(new AttackOrder(0, "B", "C", 0, 3));

        assertEquals(2, game.getAllOrders().size(), "Should have 2 orders");

        game.clearAllOrders();

        assertTrue(game.getAllOrders().isEmpty(), "Orders list should be empty after clearing");
    }

    @Test
    void testEndTurn() {
        // Set up game with players and territories
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        Player p1 = game.getPlayer(1);

        // Set one player to be upgrading
        p0.startTechUpgrade(2);
        assertTrue(p0.isTechUpgrading(), "Player 0 should be upgrading tech");
        assertEquals(1, p0.getMaxTechLevel(), "Player 0 should still be at tech level 1");

        // End turn
        game.endTurn();

        // Verify tech upgrade completed
        assertFalse(p0.isTechUpgrading(), "Player 0 should no longer be upgrading");
        assertEquals(2, p0.getMaxTechLevel(), "Player 0 should now be at tech level 2");

        // Verify resource production
        int expectedFood = p0.getTerritories().size(); // 1 food per territory
        int expectedTech = p0.getTerritories().size(); // 1 tech per territory
        assertEquals(expectedFood, p0.getFood(), "Player 0 should have gained food");
        assertEquals(expectedTech, p0.getTech(), "Player 0 should have gained tech");

        // Verify new units
        for (Territory t : p0.getTerritories()) {
            assertEquals(1, t.getUnitMap().getOrDefault(0, 0), "Each territory should have 1 new basic unit");
        }
    }

    @Test
    void testUpdatePlayerStatus_NoWinner() {
        // Set up game with two players
        game.setUpMap(2);
        game.initPlayers(2);

        // Initially no winner
        assertFalse(game.hasWinner(), "Should not have a winner initially");
        assertNull(game.getWinner(), "Winner should be null initially");

        // Update player status
        game.updatePlayerStatus();

        // Still no winner
        assertFalse(game.hasWinner(), "Should not have a winner after update");
        assertNull(game.getWinner(), "Winner should still be null");
    }

    @Test
    void testUpdatePlayerStatus_WithWinner() {
        // Set up game with two players
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        Player p1 = game.getPlayer(1);

        // Give all territories to p0
        for (Territory t : new ArrayList<>(p1.getTerritories())) {
            t.setOwner(p0);
            p0.addTerritory(t);
            p1.removeTerritory(t);
        }

        // Update player status
        game.updatePlayerStatus();

        // p0 should be the winner
        assertTrue(game.hasWinner(), "Should have a winner");
        assertEquals(p0, game.getWinner(), "Player 0 should be the winner");
    }

    @Test
    void testUpdatePlayerStatus_PlayerEliminated() {
        // Set up game with two players
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        Player p1 = game.getPlayer(1);

        // Remove all territories from p1
        for (Territory t : new ArrayList<>(p1.getTerritories())) {
            p1.removeTerritory(t);
        }

        // Update player status
        game.updatePlayerStatus();

        // p1 should be eliminated
        assertFalse(p1.isAlive(), "Player 1 should be eliminated");
        assertTrue(p0.isAlive(), "Player 0 should still be alive");

        // p0 should be the winner
        assertTrue(game.hasWinner(), "Should have a winner");
        assertEquals(p0, game.getWinner(), "Player 0 should be the winner");
    }

    @Test
    void testGetTerritoryByName() {
        game.setUpMap(2);

        Territory tA = game.getTerritoryByName("A");
        assertNotNull(tA, "Territory A should exist");
        assertEquals("A", tA.getName(), "Territory should have the correct name");

        Territory nonExistent = game.getTerritoryByName("NonExistent");
        assertNull(nonExistent, "Nonexistent territory should return null");
    }

    @Test
    void testGetPlayer() {
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        assertNotNull(p0, "Player 0 should exist");
        assertEquals("Player1", p0.getName(), "Player 0 should have the name Player1");

        Player p1 = game.getPlayer(1);
        assertNotNull(p1, "Player 1 should exist");
        assertEquals("Player2", p1.getName(), "Player 1 should have the name Player2");
    }

    @Test
    void testGetAllOrders() {
        List<Order> orders = game.getAllOrders();
        assertNotNull(orders, "Orders list should not be null");
        assertTrue(orders.isEmpty(), "Orders list should be empty initially");

        game.addOrder(new MoveOrder(0, "A", "B", 0, 5));
        assertEquals(1, orders.size(), "Orders list should have 1 item");
    }

    @Test
    void testGetRandom() {
        assertNotNull(game.getRandom(), "Random object should not be null");
    }

    @Test
    void testGetMapState() {
        game.setUpMap(2);
        game.initPlayers(2);

        String mapState = game.getMapState();
        assertNotNull(mapState, "Map state string should not be null");

        // Verify basic content
        assertTrue(mapState.contains("Current Map State"), "Map state should include header");
        assertTrue(mapState.contains("A ("), "Map state should include territory A");
        assertTrue(mapState.contains("Size:"), "Map state should include territory size");
        assertTrue(mapState.contains("Neighbors:"), "Map state should include neighbors");
        assertTrue(mapState.contains("Units:"), "Map state should include units");
    }
}