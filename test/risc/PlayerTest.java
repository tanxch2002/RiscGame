package risc;

public class PlayerTest {
    public static void main(String[] args) {
        testBasicProperties();
        testResourceManagement();
        testTechUpgrade();
        testTerritoriesManagement();
        testAlliances();
    }

    private static void testBasicProperties() {
        Player player = new Player(1, "Player1");

        assert player.getId() == 1 : "ID should be 1";
        assert "Player1".equals(player.getName()) : "Name should be Player1";
        assert player.isAlive() : "Player should be alive by default";

        player.setName("UpdatedName");
        assert "UpdatedName".equals(player.getName()) : "Name should be updated";

        player.setAlive(false);
        assert !player.isAlive() : "Player should not be alive after setAlive(false)";

        assert !player.isAI() : "Regular player should not be AI";
    }

    private static void testResourceManagement() {
        Player player = new Player(1, "Player1");

        assert player.getFood() == 100 : "Initial food should be 100";
        assert player.getTech() == 0 : "Initial tech should be 0";

        player.addFood(50);
        player.addTech(30);

        assert player.getFood() == 150 : "Food should be 150 after adding 50";
        assert player.getTech() == 30 : "Tech should be 30 after adding 30";

        assert player.spendFood(50) : "Should be able to spend 50 food";
        assert player.getFood() == 100 : "Food should be 100 after spending 50";

        assert !player.spendFood(200) : "Should not be able to spend 200 food";
        assert player.getFood() == 100 : "Food should still be 100 after failed spend";

        assert player.spendTech(20) : "Should be able to spend 20 tech";
        assert player.getTech() == 10 : "Tech should be 10 after spending 20";

        assert !player.spendTech(20) : "Should not be able to spend 20 tech";
        assert player.getTech() == 10 : "Tech should still be 10 after failed spend";
    }

    private static void testTechUpgrade() {
        Player player = new Player(1, "Player1");

        assert player.getMaxTechLevel() == 1 : "Initial max tech level should be 1";
        assert !player.isTechUpgrading() : "Should not be upgrading tech initially";

        player.startTechUpgrade(2);

        assert player.isTechUpgrading() : "Should be upgrading tech";
        assert player.getMaxTechLevel() == 1 : "Max tech level should still be 1 during upgrade";

        player.finishTechUpgrade();

        assert !player.isTechUpgrading() : "Should not be upgrading tech after finishing";
        assert player.getMaxTechLevel() == 2 : "Max tech level should be 2 after upgrade";
    }

    private static void testTerritoriesManagement() {
        Player player = new Player(1, "Player1");
        Territory t1 = new Territory("T1");
        Territory t2 = new Territory("T2");

        assert player.getTerritories().isEmpty() : "Should have no territories initially";

        player.addTerritory(t1);
        assert player.getTerritories().size() == 1 : "Should have 1 territory after adding";
        assert player.getTerritories().contains(t1) : "Should contain the added territory";

        player.addTerritory(t2);
        assert player.getTerritories().size() == 2 : "Should have 2 territories after adding another";

        player.removeTerritory(t1);
        assert player.getTerritories().size() == 1 : "Should have 1 territory after removing";
        assert !player.getTerritories().contains(t1) : "Should not contain the removed territory";
        assert player.getTerritories().contains(t2) : "Should still contain the other territory";
    }

    private static void testAlliances() {
        Player player = new Player(1, "Player1");

        assert player.getAllies().isEmpty() : "Should have no allies initially";

        player.addAlly(2);
        assert player.isAlliedWith(2) : "Should be allied with player 2";
        assert player.getAllies().size() == 1 : "Should have 1 ally";

        player.addAlly(3);
        assert player.isAlliedWith(3) : "Should be allied with player 3";
        assert player.getAllies().size() == 2 : "Should have 2 allies";

        player.removeAlly(2);
        assert !player.isAlliedWith(2) : "Should no longer be allied with player 2";
        assert player.isAlliedWith(3) : "Should still be allied with player 3";
        assert player.getAllies().size() == 1 : "Should have 1 ally after removing";
    }
}