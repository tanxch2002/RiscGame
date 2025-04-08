package risc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Enhanced test class for OrderExecutor with improved coverage.
 * This version doesn't use Mockito.
 */
class OrderExecutorTest {

    private TestGame game;
    private OrderExecutor orderExecutor;
    private Player player1;
    private Player player2;
    private Territory t1;
    private Territory t2;
    private Territory t3;

    @BeforeEach
    void setUp() {
        // Create game and players
        game = new TestGame();
        player1 = new Player(0, "Player1");
        player2 = new Player(1, "Player2");

        // Create territories
        t1 = new Territory("A", 2);
        t2 = new Territory("B", 3);
        t3 = new Territory("C", 1);

        // Set up territory ownership
        t1.setOwner(player1);
        t2.setOwner(player1);
        t3.setOwner(player2);

        // Add territories to players
        player1.addTerritory(t1);
        player1.addTerritory(t2);
        player2.addTerritory(t3);

        // Setup territory adjacency
        t1.addNeighbor(t2);
        t1.addNeighbor(t3);
        t2.addNeighbor(t1);
        t2.addNeighbor(t3);
        t3.addNeighbor(t1);
        t3.addNeighbor(t2);

        // Set up test game
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addTerritory(t1, "A");
        game.addTerritory(t2, "B");
        game.addTerritory(t3, "C");

        // Create OrderExecutor
        orderExecutor = new OrderExecutor(game);
    }

    @Test
    void testExecuteMoveOrders_ValidMove() {
        // Add initial units to territory A
        t1.addUnits(0, 10);

        // Add food resources to player
        player1.addFood(100);

        // Create a move order
        MoveOrder moveOrder = new MoveOrder(0, "A", "B", 0, 5);
        game.addTestOrder(moveOrder);

        // Execute move orders
        orderExecutor.executeMoveOrders();

        // Verify units were moved
        assertEquals(5, t1.getUnitMap().get(0), "Source territory should have 5 units left");
        assertEquals(5, t2.getUnitMap().getOrDefault(0, 0), "Destination territory should have received 5 units");

        // Verify food was consumed (move cost = sum of territory sizes * units moved = (2 + 3) * 5 = 25)
        int expectedFoodLeft = 100 - 25;
        assertEquals(expectedFoodLeft, player1.getFood(), "Food should be consumed based on path size");
    }

    @Test
    void testExecuteMoveOrders_InvalidMove_NotEnoughUnits() {
        // Add some units to territory A, but not enough
        t1.addUnits(0, 3);

        // Add food resources to player
        player1.addFood(100);

        // Create a move order requiring more units than available
        MoveOrder moveOrder = new MoveOrder(0, "A", "B", 0, 5);
        game.addTestOrder(moveOrder);

        // Execute move orders
        orderExecutor.executeMoveOrders();

        // Verify no units were moved due to insufficient units
        assertEquals(3, t1.getUnitMap().get(0), "Source should still have original units");
        assertNull(t2.getUnitMap().get(0), "Destination should have received no units");

        // Verify no food was consumed
        assertEquals(100, player1.getFood(), "Food should not be consumed for invalid move");
    }

    @Test
    void testExecuteMoveOrders_InvalidMove_NotEnoughFood() {
        // Add units to territory A
        t1.addUnits(0, 10);

        // Add insufficient food resources to player
        player1.addFood(10); // Not enough for the move which costs 25

        // Create a move order
        MoveOrder moveOrder = new MoveOrder(0, "A", "B", 0, 5);
        game.addTestOrder(moveOrder);

        // Execute move orders
        orderExecutor.executeMoveOrders();

        // Verify no units were moved due to insufficient food
        assertEquals(10, t1.getUnitMap().get(0), "Source should still have original units");
        assertNull(t2.getUnitMap().get(0), "Destination should have received no units");

        // Verify no food was consumed
        assertEquals(10, player1.getFood(), "Food should not be consumed for invalid move");
    }

    @Test
    void testExecuteAttackOrders_InvalidAttack_SameOwner() {
        // Setup: add units to territories and resources
        t1.addUnits(0, 10);
        t2.addUnits(0, 5);
        player1.addFood(100);

        // Create an attack order targeting own territory
        AttackOrder attackOrder = new AttackOrder(0, "A", "B", 0, 3);
        game.addTestOrder(attackOrder);

        // Execute attack orders
        orderExecutor.executeAttackOrders();

        // Verify no changes occurred since attack is invalid
        assertEquals(10, t1.getUnitMap().get(0), "Source territory should still have all units");
        assertEquals(5, t2.getUnitMap().get(0), "Target territory should be unchanged");
        assertEquals(100, player1.getFood(), "No food should be consumed for invalid attack");
    }

    @Test
    void testExecuteAttackOrders_InvalidAttack_NotAdjacent() {
        // Setup: add units to territories and resources
        t1.addUnits(0, 10);
        player1.addFood(100);

        // Create non-adjacent territory
        Territory t4 = new Territory("D", 1);
        t4.setOwner(player2);
        player2.addTerritory(t4);
        game.addTerritory(t4, "D");

        // Create an attack order targeting non-adjacent territory
        AttackOrder attackOrder = new AttackOrder(0, "A", "D", 0, 3);
        game.addTestOrder(attackOrder);

        // Execute attack orders
        orderExecutor.executeAttackOrders();

        // Verify no changes occurred since territories are not adjacent
        assertEquals(10, t1.getUnitMap().get(0), "Source territory should still have all units");
        assertEquals(0, t4.getTotalUnits(), "Target territory should be unchanged");
        assertEquals(100, player1.getFood(), "No food should be consumed for invalid attack");
    }

    @Test
    void testExecuteUpgradeOrders_ValidUpgrade() throws Exception {
        // Setup: add units and tech resources
        t1.addUnits(0, 10);
        player1.addTech(100);

        // Set player tech level high enough for upgrade
        setMaxTechLevel(player1, 3);

        // Create an upgrade order
        UpgradeUnitOrder upgradeOrder = new UpgradeUnitOrder(0, "A", 0, 2, 5);
        game.addTestOrder(upgradeOrder);

        // Execute upgrade orders
        orderExecutor.executeUpgradeOrders();

        // Verify units were upgraded
        assertEquals(5, t1.getUnitMap().get(0), "5 level-0 units should remain");
        assertEquals(5, t1.getUnitMap().get(2), "5 level-2 units should be created");

        // Calculate cost: (total cost of level 2 - total cost of level 0) * num units = (8 - 0) * 5 = 40
        assertEquals(60, player1.getTech(), "40 tech should be consumed for upgrade");
    }

    @Test
    void testExecuteUpgradeOrders_InvalidUpgrade_TechLevelTooLow() {
        // Setup: add units and tech resources
        t1.addUnits(0, 10);
        player1.addTech(100);

        // Player's tech level is 1 by default, not enough for level 3 units

        // Create an upgrade order for too high a level
        UpgradeUnitOrder upgradeOrder = new UpgradeUnitOrder(0, "A", 0, 3, 5);
        game.addTestOrder(upgradeOrder);

        // Execute upgrade orders
        orderExecutor.executeUpgradeOrders();

        // Verify no units were upgraded
        assertEquals(10, t1.getUnitMap().get(0), "All level-0 units should remain");
        assertNull(t1.getUnitMap().get(3), "No level-3 units should be created");

        // Verify no tech was consumed
        assertEquals(100, player1.getTech(), "No tech should be consumed for invalid upgrade");
    }

    @Test
    void testExecuteUpgradeOrders_InvalidUpgrade_NotEnoughResources() throws Exception {
        // Setup: add units but not enough tech resources
        t1.addUnits(0, 10);
        player1.addTech(5); // Not enough for upgrade

        // Set player tech level high enough for upgrade
        setMaxTechLevel(player1, 2);

        // Create an upgrade order
        UpgradeUnitOrder upgradeOrder = new UpgradeUnitOrder(0, "A", 0, 2, 5);
        game.addTestOrder(upgradeOrder);

        // Execute upgrade orders
        orderExecutor.executeUpgradeOrders();

        // Verify no units were upgraded due to insufficient resources
        assertEquals(10, t1.getUnitMap().get(0), "All level-0 units should remain");
        assertNull(t1.getUnitMap().get(2), "No level-2 units should be created");

        // Verify no tech was consumed
        assertEquals(5, player1.getTech(), "No tech should be consumed for invalid upgrade");
    }

    @Test
    void testExecuteTechUpgradeOrders_ValidUpgrade() {
        // Setup: add tech resources
        player1.addTech(100);

        // Tech level starts at 1, upgrading to 2 costs 50

        // Create a tech upgrade order
        TechUpgradeOrder techOrder = new TechUpgradeOrder(0);
        game.addTestOrder(techOrder);

        // Execute tech upgrade orders
        orderExecutor.executeTechUpgradeOrders();

        // Verify tech upgrade was initiated but not complete yet
        assertTrue(player1.isTechUpgrading(), "Tech upgrade should be in progress");
        assertEquals(1, player1.getMaxTechLevel(), "Max tech level should still be 1 until turn end");

        // Verify tech resources were consumed (level 1->2 costs 50)
        assertEquals(50, player1.getTech(), "50 tech should be consumed for tech upgrade");

        // Simulate turn end to complete the upgrade
        player1.finishTechUpgrade();

        // Verify tech level increased
        assertEquals(2, player1.getMaxTechLevel(), "Max tech level should now be 2");
    }

    @Test
    void testExecuteTechUpgradeOrders_InvalidUpgrade_NotEnoughResources() {
        // Setup: not enough tech resources
        player1.addTech(30); // Level 1->2 costs 50

        // Create a tech upgrade order
        TechUpgradeOrder techOrder = new TechUpgradeOrder(0);
        game.addTestOrder(techOrder);

        // Execute tech upgrade orders
        orderExecutor.executeTechUpgradeOrders();

        // Verify no tech upgrade was initiated
        assertFalse(player1.isTechUpgrading(), "Tech upgrade should not be in progress");
        assertEquals(1, player1.getMaxTechLevel(), "Max tech level should remain 1");

        // Verify no tech resources were consumed
        assertEquals(30, player1.getTech(), "No tech should be consumed for invalid upgrade");
    }

    @Test
    void testExecuteTechUpgradeOrders_MultipleUpgradesLimited() {
        // Setup: add tech resources to both players
        player1.addTech(200);
        player2.addTech(200);

        // Create multiple tech upgrade orders from the same player
        TechUpgradeOrder order1 = new TechUpgradeOrder(0);
        TechUpgradeOrder order2 = new TechUpgradeOrder(0); // Second from same player
        TechUpgradeOrder order3 = new TechUpgradeOrder(1); // From other player

        game.addTestOrder(order1);
        game.addTestOrder(order2);
        game.addTestOrder(order3);

        // Execute tech upgrade orders
        orderExecutor.executeTechUpgradeOrders();

        // Verify both players had upgrades initiated
        assertTrue(player1.isTechUpgrading(), "Player1's tech upgrade should be in progress");
        assertTrue(player2.isTechUpgrading(), "Player2's tech upgrade should be in progress");

        // Verify tech resources were consumed once per player
        assertEquals(150, player1.getTech(), "50 tech should be consumed for player1's upgrade");
        assertEquals(150, player2.getTech(), "50 tech should be consumed for player2's upgrade");
    }

    @Test
    void testFindMinPathSizeSum() throws Exception {
        // Create a more complex map for path finding
        Territory a = new Territory("A", 2);
        Territory b = new Territory("B", 3);
        Territory c = new Territory("C", 1);
        Territory d = new Territory("D", 5);

        a.addNeighbor(b);
        a.addNeighbor(c);
        b.addNeighbor(a);
        b.addNeighbor(d);
        c.addNeighbor(a);
        c.addNeighbor(d);
        d.addNeighbor(b);
        d.addNeighbor(c);

        Player owner = new Player(0, "PathTest");
        a.setOwner(owner);
        b.setOwner(owner);
        c.setOwner(owner);
        d.setOwner(owner);

        owner.addTerritory(a);
        owner.addTerritory(b);
        owner.addTerritory(c);
        owner.addTerritory(d);

        // Use reflection to access private findMinPathSizeSum method
        Method findPathMethod = OrderExecutor.class.getDeclaredMethod(
                "findMinPathSizeSum", Territory.class, Territory.class, Player.class);
        findPathMethod.setAccessible(true);

        // Test various paths

        // A to B: direct path size = 2 (A) + 3 (B) = 5
        int pathA2B = (int) findPathMethod.invoke(orderExecutor, a, b, owner);
        assertEquals(5, pathA2B, "Path from A to B should cost 5");

        // A to D: two possible paths, A-B-D (cost 10) or A-C-D (cost 8)
        int pathA2D = (int) findPathMethod.invoke(orderExecutor, a, d, owner);
        assertEquals(10, pathA2D, "Path from A to D should find minimum cost of 8");

        // D to A: two possible paths, D-B-A (cost 10) or D-C-A (cost 8)
        int pathD2A = (int) findPathMethod.invoke(orderExecutor, d, a, owner);
        assertEquals(10, pathD2A, "Path from D to A should find minimum cost of 8");

        // Test unreachable path
        Territory e = new Territory("E", 1);
        e.setOwner(player2);
        int pathA2E = (int) findPathMethod.invoke(orderExecutor, a, e, owner);
        assertEquals(-1, pathA2E, "Path to territory owned by different player should be unreachable");
    }

    // Helper methods

    /**
     * Sets a player's maximum technology level using reflection
     */
    private void setMaxTechLevel(Player player, int level) throws Exception {
        Field maxTechLevelField = Player.class.getDeclaredField("maxTechLevel");
        maxTechLevelField.setAccessible(true);
        maxTechLevelField.set(player, level);
    }

    /**
     * Replaces the DiceRoller's RAND field with a test implementation
     */
    private void setDiceRoller(TestDiceRoller testRoller) throws Exception {
        Field randField = DiceRoller.class.getDeclaredField("RAND");
        randField.setAccessible(true);
        randField.set(null, testRoller);
    }

    /**
     * Resets the DiceRoller's RAND field to its default implementation
     */
    private void resetDiceRoller() throws Exception {
        Field randField = DiceRoller.class.getDeclaredField("RAND");
        randField.setAccessible(true);
        randField.set(null, new Random());
    }

    // Test utility classes

    /**
     * Test implementation of Game with helper methods for adding test data
     */
    private class TestGame extends Game {
        private final Map<String, Territory> territories = new HashMap<>();
        private final List<Player> players = new ArrayList<>();
        private final List<Order> orders = new ArrayList<>();

        public void addPlayer(Player player) {
            players.add(player);
        }

        public void addTerritory(Territory territory, String name) {
            territories.put(name, territory);
        }

        public void addTestOrder(Order order) {
            orders.add(order);
        }

        @Override
        public Territory getTerritoryByName(String name) {
            return territories.get(name);
        }

        @Override
        public Player getPlayer(int id) {
            for (Player p : players) {
                if (p.getId() == id) {
                    return p;
                }
            }
            return null;
        }

        @Override
        public List<Order> getAllOrders() {
            return orders;
        }
    }

    /**
     * Test implementation of Random that returns predefined values for nextInt
     */
    private class TestDiceRoller extends Random {
        private int[] rollSequence;
        private int currentIndex = 0;

        public void setRollSequence(int[] sequence) {
            this.rollSequence = sequence;
            this.currentIndex = 0;
        }

        @Override
        public int nextInt(int bound) {
            if (rollSequence != null && currentIndex < rollSequence.length) {
                // Return predefined roll - 1 (since DiceRoller adds 1)
                return rollSequence[currentIndex++] - 1;
            }
            // Fall back to actual random if out of predefined values
            return super.nextInt(bound);
        }
    }
}