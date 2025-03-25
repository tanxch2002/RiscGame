package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void getPlayerID() {
        Order o = new AttackOrder(1, "S", "D", 3);
        assertEquals(1, o.getPlayerID());
    }

    @Test
    void getSourceName() {
        Order o = new MoveOrder(2, "Src", "Dest", 4);
        assertEquals("Src", o.getSourceName());
    }

    @Test
    void getDestName() {
        Order o = new AttackOrder(1, "X", "Y", 3);
        assertEquals("Y", o.getDestName());
    }

    @Test
    void getNumUnits() {
        Order o = new AttackOrder(1, "S", "D", 5);
        assertEquals(5, o.getNumUnits());
    }
}
