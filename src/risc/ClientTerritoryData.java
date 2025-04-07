package risc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores data about a single territory for client-side graphical representation.
 */
public class ClientTerritoryData {
    String name;
    String ownerName; // "Player1", "Player2", "None"
    Color ownerColor; // Color derived from ownerName
    Map<Integer, Integer> units; // Level -> Count
    List<String> neighborNames;
    int foodProduction;
    int techProduction;
    int size;
    int x, y; // Screen coordinates for drawing
    int radius = 30; // Visual radius for drawing

    public ClientTerritoryData(String name, int x, int y) {
        this.name = name;
        this.ownerName = "None";
        this.ownerColor = Color.LIGHT_GRAY; // Default color
        this.units = new HashMap<>();
        this.neighborNames = new ArrayList<>();
        this.foodProduction = 0;
        this.techProduction = 0;
        this.size = 1;
        this.x = x;
        this.y = y;
    }

    // Getters
    public String getName() { return name; }
    public String getOwnerName() { return ownerName; }
    public Color getOwnerColor() { return ownerColor; }
    public Map<Integer, Integer> getUnits() { return units; }
    public List<String> getNeighborNames() { return neighborNames; }
    public int getFoodProduction() { return foodProduction; }
    public int getTechProduction() { return techProduction; }
    public int getSize() { return size; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return radius; }

    /**
     * Update the territory data.
     */
    public void updateData(String owner, int size, List<String> neighbors,
                           Map<Integer, Integer> units, Color color,
                           int foodProd, int techProd) {
        this.ownerName = owner;
        this.size = size;
        this.neighborNames = neighbors;
        this.units = units;
        this.ownerColor = color;
        this.foodProduction = foodProd;
        this.techProduction = techProd;
    }

    @Override
    public String toString() {
        return "ClientTerritoryData{" +
                "name='" + name + '\'' +
                ", ownerName='" + ownerName + '\'' +
                '}';
    }
}
