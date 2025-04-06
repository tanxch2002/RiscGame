package risc;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final int id;
    private String name;  // 改为可 setName，以便在RiscServer里改成用户名
    private boolean alive;
    private final List<Territory> territories;

    // 新增资源
    private int food;
    private int tech;

    // 最大科技等级
    private int maxTechLevel = 1;
    private boolean isTechUpgrading = false;
    private int nextTechLevel = 1;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.alive = true;
        this.territories = new ArrayList<>();
        this.food = 0;
        this.tech = 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // 新增
    public void setName(String newName) {
        this.name = newName;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public List<Territory> getTerritories() {
        return territories;
    }

    public void addTerritory(Territory t) {
        if (!territories.contains(t)) {
            territories.add(t);
        }
    }

    public void removeTerritory(Territory t) {
        territories.remove(t);
    }

    //=== 新增资源方法
    public int getFood() { return food; }
    public int getTech() { return tech; }
    public void addFood(int delta) { this.food += delta; }
    public void addTech(int delta) { this.tech += delta; }

    // 花费资源
    public boolean spendFood(int amt) {
        if (food < amt) return false;
        food -= amt;
        return true;
    }
    public boolean spendTech(int amt) {
        if (tech < amt) return false;
        tech -= amt;
        return true;
    }

    //=== 最大科技等级
    public int getMaxTechLevel() {
        return maxTechLevel;
    }

    public boolean isTechUpgrading() {
        return isTechUpgrading;
    }

    public void startTechUpgrade(int nextLevel) {
        this.isTechUpgrading = true;
        this.nextTechLevel = nextLevel;
    }

    public void finishTechUpgrade() {
        this.isTechUpgrading = false;
        this.maxTechLevel = nextTechLevel;
    }
}
