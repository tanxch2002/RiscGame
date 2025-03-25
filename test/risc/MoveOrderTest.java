package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveOrderTest {

    @Test
    void testConstructorAndGetters() {
        MoveOrder mo = new MoveOrder(2, "X", "Y", 7);
        assertEquals(2, mo.getPlayerID());
        assertEquals("X", mo.getSourceName());
        assertEquals("Y", mo.getDestName());
        assertEquals(7, mo.getNumUnits());
    }
}
