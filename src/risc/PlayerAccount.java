package risc;

/**
 * 表示一个玩家的账号。
 * 可在此保存更多数据：是否在其他游戏中、历史成绩、断线重连信息等等。
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
