package risc;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MapBuilderTest {

    @Test
    void buildMap() {
        // Test map for 3 players: should have 6 territories
        List<Territory> map3 = MapBuilder.buildMap(3);
        assertEquals(6, map3.size());

        // Test map for 5 players: should have 10 territories
        List<Territory> map5 = MapBuilder.buildMap(5);
        assertEquals(10, map5.size());

        // Test other cases (should return 8 territories)
        List<Territory> map4 = MapBuilder.buildMap(4);
        assertEquals(8, map4.size());
    }
}