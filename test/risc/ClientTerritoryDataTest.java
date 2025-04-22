package risc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ClientTerritoryDataTest {
    public static void main(String[] args) {
        testConstructorAndGetters();
        testUnitStorage();
    }

    private static void testConstructorAndGetters() {
        ClientTerritoryData data = new ClientTerritoryData("TestTerr", 100, 200);

        assert "TestTerr".equals(data.name) : "Name should be TestTerr";
        assert data.x == 100 : "X coordinate should be 100";
        assert data.y == 200 : "Y coordinate should be 200";
        assert "None".equals(data.ownerName) : "Default owner name should be None";
        assert Color.LIGHT_GRAY.equals(data.ownerColor) : "Default color should be light gray";
        assert data.radius == 30 : "Default radius should be 30";

        assert data.x == data.getX() : "getX should return x";
        assert data.y == data.getY() : "getY should return y";
        assert data.radius == data.getRadius() : "getRadius should return radius";
        assert data.ownerName.equals(data.getOwnerName()) : "getOwnerName should return ownerName";
        assert data.ownerColor.equals(data.getOwnerColor()) : "getOwnerColor should return ownerColor";
    }

    private static void testUnitStorage() {
        ClientTerritoryData data = new ClientTerritoryData("TestTerr", 100, 200);

        Map<Integer, Integer> unitsMap = new HashMap<>();
        unitsMap.put(0, 5);
        unitsMap.put(1, 3);

        data.unitsByPlayer.put("P1", unitsMap);

        Map<String, Map<Integer, Integer>> units = data.getUnitsByPlayer();
        assert units.containsKey("P1") : "Units map should contain P1";
        assert units.get("P1").get(0) == 5 : "P1 should have 5 level-0 units";
        assert units.get("P1").get(1) == 3 : "P1 should have 3 level-1 units";
    }
}