package risc;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ClientTerritoryDataTest {

    @Test
    void testConstructorAndGetters() {
        ClientTerritoryData data = new ClientTerritoryData("Narnia", 100, 200);

        assertEquals("Narnia", data.getName());
        assertEquals("None", data.getOwnerName());
        assertEquals(Color.LIGHT_GRAY, data.getOwnerColor());
        assertTrue(data.getUnits().isEmpty());
        assertTrue(data.getNeighborNames().isEmpty());
        assertEquals(0, data.getFoodProduction());
        assertEquals(0, data.getTechProduction());
        assertEquals(1, data.getSize());
        assertEquals(100, data.getX());
        assertEquals(200, data.getY());
        assertEquals(30, data.getRadius());
    }

    @Test
    void testUpdateData() {
        ClientTerritoryData data = new ClientTerritoryData("Mordor", 50, 50);

        Color ownerColor = Color.RED;
        Map<Integer, Integer> units = new HashMap<>();
        units.put(0, 10);
        units.put(1, 5);

        List<String> neighbors = new ArrayList<>();
        neighbors.add("Gondor");
        neighbors.add("Hogwarts");

        data.updateData("Player1", 3, neighbors, units, ownerColor, 4, 2);

        assertEquals("Player1", data.getOwnerName());
        assertEquals(3, data.getSize());
        assertEquals(2, data.getNeighborNames().size());
        assertTrue(data.getNeighborNames().contains("Gondor"));
        assertTrue(data.getNeighborNames().contains("Hogwarts"));
        assertEquals(ownerColor, data.getOwnerColor());
        assertEquals(4, data.getFoodProduction());
        assertEquals(2, data.getTechProduction());
        assertEquals(10, data.getUnits().get(0));
        assertEquals(5, data.getUnits().get(1));
    }

    @Test
    void testToString() {
        ClientTerritoryData data = new ClientTerritoryData("Elantris", 120, 150);
        data.updateData("Player2", 2, new ArrayList<>(), new HashMap<>(), Color.BLUE, 2, 2);

        String result = data.toString();
        assertTrue(result.contains("Elantris"));
        assertTrue(result.contains("Player2"));
    }
}