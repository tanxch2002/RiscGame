package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    /**
     * Test retrieving the player ID from an AttackOrder.
     */
    @Test
    void getPlayerID() {
        Order o = new AttackOrder(1, "S", "D", 3);
        assertEquals(1, o.getPlayerID());
    }

    /**
     * Test retrieving the source territory name from a MoveOrder.
     */
    @Test
    void getSourceName() {
        Order o = new MoveOrder(2, "Src", "Dest", 4);
        assertEquals("Src", o.getSourceName());
    }

    /**
     * Test retrieving the destination territory name from an AttackOrder.
     */
    @Test
    void getDestName() {
        Order o = new AttackOrder(1, "X", "Y", 3);
        assertEquals("Y", o.getDestName());
    }

    /**
     * Test retrieving the number of units from an AttackOrder.
     */
    @Test
    void getNumUnits() {
        Order o = new AttackOrder(1, "S", "D", 5);
        assertEquals(5, o.getNumUnits());
    }
}