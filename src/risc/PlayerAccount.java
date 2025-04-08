package risc;

/**
 * Represents a player's account.
 * Additional data can be stored here, such as whether the player is active in other games,
 * historical performance records, reconnection information after disconnections, etc.
 */

public class PlayerAccount {
    private final String username;

    public PlayerAccount(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
