package risc;

import java.util.*;

public class Territory {
    private final String name;
    private Player owner; // 地块归属
    private final List<Territory> neighbors;
    private int size;

    // NEW: 多家单位共存 => playerID -> (unitLevel -> count)
    private final Map<Integer, Map<Integer, Integer>> stationedUnits;

    public Territory(String name) {
        this(name, 1);
    }

    public Territory(String name, int size) {
        this.name = name;
        this.size = size;
        this.neighbors = new ArrayList<>();
        this.stationedUnits = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Territory> getNeighbors() {
        return neighbors;
    }

    public void addNeighbor(Territory t) {
        if (!neighbors.contains(t)) {
            neighbors.add(t);
        }
    }

    public String neighborsString() {
        StringBuilder sb = new StringBuilder();
        for (Territory t : neighbors) {
            sb.append(t.name).append(" ");
        }
        return sb.toString().trim();
    }

    // NEW: 取得指定player的驻军情况
    public Map<Integer,Integer> getStationedUnitsMap(int playerID) {
        return stationedUnits.getOrDefault(playerID, new HashMap<>());
    }

    // NEW: 增加若干单位
    public void addUnits(int playerID, int level, int count) {
        if (count <= 0) return;
        stationedUnits.putIfAbsent(playerID, new HashMap<>());
        Map<Integer,Integer> levelMap = stationedUnits.get(playerID);
        levelMap.put(level, levelMap.getOrDefault(level, 0) + count);
    }

    // NEW: 移除若干单位
    public boolean removeUnits(int playerID, int level, int count) {
        Map<Integer,Integer> levelMap = stationedUnits.get(playerID);
        if (levelMap == null) {
            return false;
        }
        int cur = levelMap.getOrDefault(level, 0);
        if (cur < count) {
            return false;
        }
        int remain = cur - count;
        if (remain == 0) {
            levelMap.remove(level);
        } else {
            levelMap.put(level, remain);
        }
        if (levelMap.isEmpty()) {
            stationedUnits.remove(playerID);
        }
        return true;
    }

    // NEW: 移除playerID在此地的全部驻军 => 返回移除的(等级->数量)
    public Map<Integer,Integer> removeAllUnitsOfPlayer(int playerID) {
        return stationedUnits.remove(playerID);
    }

    public int getTotalUnits() {
        int sum = 0;
        for (Map<Integer, Integer> levelMap : stationedUnits.values()) {
            for (int c : levelMap.values()) {
                sum += c;
            }
        }
        return sum;
    }

    public int getFoodProduction() {
        return size;
    }

    public int getTechProduction() {
        return size;
    }

    // NEW: 用于打印查看
    public String stationedUnitsString() {
        // eg: "P0->{0=5,1=2}, P1->{0=3}"
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Map<Integer,Integer>> e : stationedUnits.entrySet()) {
            sb.append("P").append(e.getKey()).append("->").append(e.getValue()).append(" ; ");
        }
        if (sb.length() == 0) {
            sb.append("No units");
        }
        return sb.toString();
    }
}
