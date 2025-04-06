package risc;

/**
 * Represents a move order in the game.
 * Now it also has a level field to specify which level units are moving.
 */
public class MoveOrder extends Order {
    private final int level;

    /**
     * @param playerID 发起移动的玩家 ID
     * @param source 源领土
     * @param dest   目标领土
     * @param level  要移动的单位等级
     * @param numUnits 要移动的该等级单位数量
     */
    public MoveOrder(int playerID, String source, String dest, int level, int numUnits) {
        super(playerID, source, dest, numUnits);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
