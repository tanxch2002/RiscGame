package risc;

import java.awt.Color;
import java.util.*;

/**
 * Stores all client‑side info of a single territory.
 */
public class ClientTerritoryData {
    public final String name;
    public String ownerName = "None";
    public Color ownerColor = Color.LIGHT_GRAY;

    /**
     * unitsByPlayer → playerTag (e.g. "P0") → (level → count)
     * GUI 依赖服务端打印格式：P0->{0=5,1=2}
     */
    public final Map<String, Map<Integer, Integer>> unitsByPlayer = new HashMap<>();

    public final List<String> neighborNames = new ArrayList<>();
    public int foodProduction = 0, techProduction = 0, size = 1;
    public int x, y;                       // screen coords
    public int radius = 30;                // circle radius

    public ClientTerritoryData(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    /* -------- getters -------- */
    public Map<String, Map<Integer, Integer>> getUnitsByPlayer() { return unitsByPlayer; }
    public String getOwnerName()  { return ownerName; }
    public Color  getOwnerColor() { return ownerColor; }
    public int    getX()          { return x; }
    public int    getY()          { return y; }
    public int    getRadius()     { return radius; }
}
