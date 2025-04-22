package risc;

public class PlayerAccountTest {
    public static void main(String[] args) {
        testConstructorAndGetUsername();
    }

    private static void testConstructorAndGetUsername() {
        PlayerAccount account = new PlayerAccount("TestUser");

        assert "TestUser".equals(account.getUsername()) : "Username should be TestUser";
    }
}