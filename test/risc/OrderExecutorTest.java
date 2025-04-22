package risc;

public class OrderExecutorTest {
    public static void main(String[] args) {
        testMovement();
        testAttack();
        testAlliance();
        testUpgrades();
    }

    private static void testMovement() {
        Game game = new Game(null);
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        Territory t1 = p0.getTerritories().get(0);
        Territory t2 = p0.getTerritories().get(1);

        t1.addUnits(p0.getId(), 0, 10);

        MoveOrder move = new MoveOrder(p0.getId(), t1.getName(), t2.getName(), 0, 5);
        game.addOrder(move);

        game.executeAllMoveOrders();

        assert t1.getStationedUnitsMap(p0.getId()).getOrDefault(0, 0) == 5 :
                "Source should have 5 units left";
        assert t2.getStationedUnitsMap(p0.getId()).getOrDefault(0, 0) == 5 :
                "Destination should have 5 units";
    }

    private static void testAttack() {
        Game game = new Game(null);
        game.setUpMap(2);
        game.initPlayers(2);

        Player p0 = game.getPlayer(0);
        Player p1 = game.getPlayer(1);

        Territory t1 = p0.getTerritories().get(0);
        Territory t2 = p1.getTerritories().get(0);

        if (!t1.getNeighbors().contains(t2)) {
            t1.addNeighbor(t2);
            t2.addNeighbor(t1);
        }

        t1.addUnits(p0.getId(), 0, 10);

        AttackOrder attack = new AttackOrder(p0.getId(), t1.getName(), t2.getName(), 0, 5);
        game.addOrder(attack);

        game.executeAllAttackOrders();
    }

    private static void testAlliance() {
        Game game = new Game(null);
        game.setUpMap(3);
        game.initPlayers(3);

        Player p0 = game.getPlayer(0);
        Player p1 = game.getPlayer(1);
        Player p2 = game.getPlayer(2);

        p0.setName("Player0");
        p1.setName("Player1");
        p2.setName("Player2");

        AllianceOrder a01 = new AllianceOrder(0, "Player1");
        AllianceOrder a10 = new AllianceOrder(1, "Player0");
        game.addOrder(a01);
        game.addOrder(a10);

        game.executeAllAlliances();

        assert p0.isAlliedWith(1) : "Player 0 should be allied with 1";
        assert p1.isAlliedWith(0) : "Player 1 should be allied with 0";
    }

    private static void testUpgrades() {
        Game game = new Game(null);
        game.setUpMap(1);
        game.initPlayers(1);

        Player p0 = game.getPlayer(0);
        Territory t = p0.getTerritories().get(0);

        t.addUnits(p0.getId(), 0, 5);
        p0.addTech(100);

        UpgradeUnitOrder upgrade = new UpgradeUnitOrder(p0.getId(), t.getName(), 0, 1, 3);
        game.addOrder(upgrade);

        game.executeAllUpgrades();

        assert t.getStationedUnitsMap(p0.getId()).getOrDefault(0, 0) == 2 :
                "Should have 2 level-0 units left";
        assert t.getStationedUnitsMap(p0.getId()).getOrDefault(1, 0) == 3 :
                "Should have 3 level-1 units after upgrade";

        p0.addTech(100);
        TechUpgradeOrder techUpgrade = new TechUpgradeOrder(p0.getId());
        game.addOrder(techUpgrade);

        game.executeAllUpgrades();
        game.endTurn();

        assert p0.getMaxTechLevel() == 2 : "Max tech level should be upgraded to 2";
    }
}