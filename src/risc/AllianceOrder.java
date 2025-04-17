package risc;

/**
 * 结盟请求的订单，代表玩家发起对某一目标玩家的结盟意图。
 * 只有在该回合内，发起方与目标方都对彼此发起了请求，结盟才会在回合末成立。
 */
public class AllianceOrder extends Order {
    private final String targetPlayerName;

    /**
     * @param playerID         结盟发起方ID
     * @param targetPlayerName 目标玩家的用户名（String）
     */
    public AllianceOrder(int playerID, String targetPlayerName) {
        super(playerID, null, null, 0);
        this.targetPlayerName = targetPlayerName;
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }
}
