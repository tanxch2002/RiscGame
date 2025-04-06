package risc;

/**
 * 表示对单位进行升级的指令。
 * 例如把领土 territoryName 中若干个 currentLevel 的单位升级到 targetLevel。
 */
public class UpgradeUnitOrder extends Order {
    private final int currentLevel;
    private final int targetLevel;

    /**
     * @param playerID     发起指令的玩家ID
     * @param territoryName 进行升级操作的领土
     * @param currentLevel 当前单位等级
     * @param targetLevel  目标单位等级
     * @param numUnits     升级的单位数量
     */
    public UpgradeUnitOrder(int playerID,
                            String territoryName,
                            int currentLevel,
                            int targetLevel,
                            int numUnits) {
        super(playerID, territoryName, territoryName, numUnits);
        this.currentLevel = currentLevel;
        this.targetLevel = targetLevel;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
