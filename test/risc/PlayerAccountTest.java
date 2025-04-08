package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerAccountTest {

    @Test
    void testConstructorAndGetter() {
        PlayerAccount account = new PlayerAccount("testUser");
        assertEquals("testUser", account.getUsername());
    }

    @Test
    void testMultipleAccounts() {
        PlayerAccount account1 = new PlayerAccount("user1");
        PlayerAccount account2 = new PlayerAccount("user2");

        assertEquals("user1", account1.getUsername());
        assertEquals("user2", account2.getUsername());
        assertNotEquals(account1.getUsername(), account2.getUsername());
    }
}