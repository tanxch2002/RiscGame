package risc;

/**
 * Represents an attack order in the game.
 * This class extends the Order class and specifies the details of an attack,
 * including the player ID, source territory, destination territory, number of units,
 * and the level of attacking units.
 */
public class AttackOrder extends Order {
    private final int level;

    /**
     * @param playerID 攻击发起者的 playerID
     * @param source   源领土
     * @param dest     目标领土
     * @param level    攻击士兵的等级
     * @param numUnits 该等级士兵的数量
     */
    public AttackOrder(int playerID, String source, String dest, int level, int numUnits) {
        super(playerID, source, dest, numUnits);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
