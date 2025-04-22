package risc;

import java.util.Map;

public class TerritoryTest {
    public static void main(String[] args) {
        testBasicProperties();
        testNeighbors();
        testUnitManagement();
        testResourceProduction();
        testStationedUnitsString();

        System.out.println("All TerritoryTest tests passed!");
    }

    private static void testBasicProperties() {
        Territory territory = new Territory("Test", 3);

        assert "Test".equals(territory.getName()) : "Name should be Test";
        assert territory.getSize() == 3 : "Size should be 3";

        Player owner = new Player(1, "Player1");
        territory.setOwner(owner);

        assert territory.getOwner() == owner : "Owner should be set correctly";

        territory.setSize(5);
        assert territory.getSize() == 5 : "Size should be updated to 5";
    }

    private static void testNeighbors() {
        Territory t1 = new Territory("T1");
        Territory t2 = new Territory("T2");
        Territory t3 = new Territory("T3");

        assert t1.getNeighbors().isEmpty() : "Should have no neighbors initially";

        t1.addNeighbor(t2);
        assert t1.getNeighbors().size() == 1 : "Should have 1 neighbor after adding";
        assert t1.getNeighbors().contains(t2) : "Should contain the added neighbor";

        t1.addNeighbor(t2); // Add same neighbor again
        assert t1.getNeighbors().size() == 1 : "Should still have 1 neighbor after adding the same one";

        t1.addNeighbor(t3);
        assert t1.getNeighbors().size() == 2 : "Should have 2 neighbors after adding another";

        String neighbors = t1.neighborsString();
        assert neighbors.contains("T2") : "Neighbors string should contain T2";
        assert neighbors.contains("T3") : "Neighbors string should contain T3";
    }

    private static void testUnitManagement() {
        Territory territory = new Territory("Test");

        assert territory.getTotalUnits() == 0 : "Should have 0 units initially";
        assert territory.getStationedUnitsMap(1).isEmpty() : "Player 1 should have no units initially";

        // Test adding units
        territory.addUnits(1, 0, 5);
        assert territory.getTotalUnits() == 5 : "Should have 5 units after adding";
        assert territory.getStationedUnitsMap(1).get(0) == 5 : "Player 1 should have 5 level-0 units";

        // Test adding more units of different level
        territory.addUnits(1, 1, 3);
        assert territory.getTotalUnits() == 8 : "Should have 8 units after adding more";
        assert territory.getStationedUnitsMap(1).get(1) == 3 : "Player 1 should have 3 level-1 units";

        // Test adding units for different player
        territory.addUnits(2, 0, 2);
        assert territory.getTotalUnits() == 10 : "Should have 10 units after adding player 2 units";
        assert territory.getStationedUnitsMap(2).get(0) == 2 : "Player 2 should have 2 level-0 units";

        // Test unsuccessful remove (too many)
        assert !territory.removeUnits(1, 0, 10) : "Should not be able to remove 10 units when only 5 exist";
        assert territory.getStationedUnitsMap(1).get(0) == 5 : "Should still have 5 level-0 units";

        // Test successful remove
        assert territory.removeUnits(1, 0, 3) : "Should be able to remove 3 units";
        assert territory.getStationedUnitsMap(1).get(0) == 2 : "Should have 2 level-0 units left";

        // Test removing all of a level
        assert territory.removeUnits(1, 0, 2) : "Should be able to remove remaining 2 units";
        assert !territory.getStationedUnitsMap(1).containsKey(0) : "Should have no more level-0 units";

        // Test removing non-existent units
        assert !territory.removeUnits(3, 0, 1) : "Should not be able to remove units from non-existent player";

        // Test trying to add 0 units (should do nothing)
        territory.addUnits(1, 2, 0);
        assert !territory.getStationedUnitsMap(1).containsKey(2) : "Should not add level-2 units when count is 0";

        // Test removing all units from a player
        Map<Integer, Integer> removedUnits = territory.removeAllUnitsOfPlayer(1);
        assert removedUnits != null : "Removed units map should not be null";
        assert removedUnits.size() == 1 : "Should have 1 entry in removed units";
        assert removedUnits.get(1) == 3 : "Should remove 3 level-1 units";
        assert territory.getStationedUnitsMap(1).isEmpty() : "Player 1 should have no units after removing all";

        // Test removing units from player with no units
        removedUnits = territory.removeAllUnitsOfPlayer(3);
        assert removedUnits == null : "Should return null when removing from non-existent player";
    }

    private static void testResourceProduction() {
        Territory territory = new Territory("Test", 3);

        assert territory.getFoodProduction() == 3 : "Food production should be same as size";
        assert territory.getTechProduction() == 3 : "Tech production should be same as size";

        territory.setSize(5);

        assert territory.getFoodProduction() == 5 : "Food production should be updated with size";
        assert territory.getTechProduction() == 5 : "Tech production should be updated with size";
    }

    private static void testStationedUnitsString() {
        Territory territory = new Territory("Test");

        // Test empty territory
        String emptyUnits = territory.stationedUnitsString();
        assert emptyUnits.equals("No units") : "Empty territory should report 'No units'";

        // Add some units and test string output
        territory.addUnits(1, 0, 5);
        territory.addUnits(1, 1, 3);
        territory.addUnits(2, 0, 2);

        String unitsString = territory.stationedUnitsString();
        assert unitsString.contains("P1") : "Units string should contain P1";
        assert unitsString.contains("P2") : "Units string should contain P2";
        assert unitsString.contains("0=5") : "Units string should contain level 0 count for P1";
        assert unitsString.contains("1=3") : "Units string should contain level 1 count for P1";
        assert unitsString.contains("0=2") : "Units string should contain level 0 count for P2";
    }
}