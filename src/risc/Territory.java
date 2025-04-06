package risc;

import java.util.*;

public class Territory {
    private final String name;
    private Player owner;

    // 原先的 int units 替换为: Map<unitLevel, unitCount>
    private  Map<Integer, Integer> unitMap;
    private final List<Territory> neighbors;

    // 新增 size
    private int size;

    public Territory(String name) {
        this(name, 1);
    }

    // 新增：带size的构造函数
    public Territory(String name, int size) {
        this.name = name;
        this.size = size;
        this.unitMap = new HashMap<>();
        this.neighbors = new ArrayList<>();
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

    public Map<Integer, Integer> getUnitMap() {
        return unitMap;
    }

    // 帮助方法：增加若干个单位到指定等级
    public void addUnits(int level, int count) {
        unitMap.put(level, unitMap.getOrDefault(level, 0) + count);
    }

    // 帮助方法：减少若干个单位，如果不够则返回false
    public boolean removeUnits(int level, int count) {
        int cur = unitMap.getOrDefault(level, 0);
        if (cur < count) return false;
        if (cur == count) {
            unitMap.remove(level);
        } else {
            unitMap.put(level, cur - count);
        }
        return true;
    }

    // 返回领土中所有单位的总数量
    public int getTotalUnits() {
        int sum = 0;
        for (int c : unitMap.values()) {
            sum += c;
        }
        return sum;
    }

    // 两种资源产量均= size（可自行定义公式）
    public int getFoodProduction() {
        return size;
    }
    public int getTechProduction() {
        return size;
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
}
