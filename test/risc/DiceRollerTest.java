package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiceRollerTest {
    @Test
    void rollD20() {
        // 连续多次检查返回值是否在 [1..20] 之间
        for(int i = 0; i < 50; i++){
            int r = DiceRoller.rollD20();
            assertTrue(r >= 1 && r <= 20, "rollD20 should return between 1 and 20");
        }
    }

    @Test
    void testRollD20() {
        // 同样执行，以便覆盖两个测试方法
        rollD20();
    }
}
