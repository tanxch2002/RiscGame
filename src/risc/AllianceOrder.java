package risc;

/**
 * Alliance request order, representing a player's intention to form an alliance with a target player.
 * An alliance is only established at the end of the turn if both the initiator and the target
 * issue alliance requests to each other during the same turn.
 */
public class AllianceOrder extends Order {
    private final String targetPlayerName;

    /**
     * @param playerID         the ID of the player initiating the alliance
     * @param targetPlayerName the username of the target player
     */
    public AllianceOrder(int playerID, String targetPlayerName) {
        super(playerID, null, null, 0);
        this.targetPlayerName = targetPlayerName;
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }
}
