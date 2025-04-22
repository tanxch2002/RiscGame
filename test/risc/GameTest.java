package risc;

import java.util.List;

public class GameTest {
    public static void main(String[] args) {
        testInitialization();
        testMapSetup();
        testAddOrder();
        testFindTerritory();
        testExecuteOrders();
        testEndTurn();
        testUpdatePlayerStatus();
        testBroadcast();
    }

    private static void testInitialization() {
        Game game = new Game(null);

        assert game.getInitialUnits() == 10 : "Initial units should be 10";
        assert !game.hasWinner() : "Game should not have a winner initially";
        assert game.getWinner() == null : "Winner should be null initially";
    }

    private static void testMapSetup() {
        Game game = new Game(null);
        game.setUpMap(3);
        game.initPlayers(3);

        List<Player> players = game.getAllPlayers();
        assert players.size() == 3 : "There should be 3 players";

        int totalTerritories = 0;
        for (Player p : players) {
            totalTerritories += p.getTerritories().size();
        }

        assert totalTerritories == 6 : "Total territories should be 6 for 3 players";
    }

    private static void testAddOrder() {
        Game game = new Game(null);
        MoveOrder move = new MoveOrder(0, "A", "B", 1, 5);
        game.addOrder(move);

        assert game.getAllOrders().size() == 1 : "Should have 1 order";
        assert game.getAllOrders().get(0) == move : "Order should be the move order";

        game.clearAllOrders();
        assert game.getAllOrders().isEmpty() : "Orders should be cleared";
    }

    private static void testFindTerritory() {
        Game game = new Game(null);
        game.setUpMap(2);

        Territory territory = game.getTerritoryByName("A");
        assert territory != null : "Should find territory A";
        assert "A".equals(territory.getName()) : "Territory name should be A";

        territory = game.getTerritoryByName("NonExistent");
        assert territory == null : "Non-existent territory should return null";
    }

    private static void testExecuteOrders() {
        Game game = new Game(null);

        try {
            game.executeAllMoveOrders();
            game.executeAllAttackOrders();
            game.executeAllAlliances();
            game.executeAllUpgrades();
        } catch (Exception e) {
            assert false : "Order execution methods should not throw exceptions";
        }
    }

    private static void testEndTurn() {
        Game game = new Game(null);
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        int initialFood = p0.getFood();
        int initialTech = p0.getTech();

        game.endTurn();

        assert p0.getFood() > initialFood : "Food should increase after end turn";
        assert p0.getTech() > initialTech : "Tech should increase after end turn";
    }

    private static void testUpdatePlayerStatus() {
        Game game = new Game(null);
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        Player p1 = game.getPlayer(1);

        for (Territory t : p1.getTerritories().toArray(new Territory[0])) {
            p1.removeTerritory(t);
            p0.addTerritory(t);
            t.setOwner(p0);
        }

        game.updatePlayerStatus();

        assert !p1.isAlive() : "Player 1 should be dead";
        assert game.hasWinner() : "Game should have a winner";
        assert game.getWinner() == p0 : "Player 0 should be the winner";
    }

    private static void testBroadcast() {
        Game game = new Game(null);

        try {
            game.broadcast("Test message");
        } catch (Exception e) {
            assert false : "Broadcast should not throw exceptions";
        }
    }
}