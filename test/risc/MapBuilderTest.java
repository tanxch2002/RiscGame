package risc;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MapBuilderTest {

    @Test
    void buildMap() {
        // 测试 3 人地图：6 块地
        List<Territory> map3 = MapBuilder.buildMap(3);
        assertEquals(6, map3.size());

        // 测试 5 人地图：10 块地
        List<Territory> map5 = MapBuilder.buildMap(5);
        assertEquals(10, map5.size());

        // 测试其它情况（返回 8 块地）
        List<Territory> map4 = MapBuilder.buildMap(4);
        assertEquals(8, map4.size());
    }
}
