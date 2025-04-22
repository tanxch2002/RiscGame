package risc;

import java.util.List;

public class MapBuilderTest {
    public static void main(String[] args) {
        testBuildMapForDifferentPlayerCounts();
        testTerritoryConnections();
    }

    private static void testBuildMapForDifferentPlayerCounts() {
        List<Territory> map3 = MapBuilder.buildMap(3);
        assert map3.size() == 6 : "3-player map should have 6 territories";

        List<Territory> map5 = MapBuilder.buildMap(5);
        assert map5.size() == 10 : "5-player map should have 10 territories";

        List<Territory> map2 = MapBuilder.buildMap(2);
        assert map2.size() == 8 : "2-player map should have 8 territories";

        List<Territory> map4 = MapBuilder.buildMap(4);
        assert map4.size() == 8 : "4-player map should have 8 territories";
    }

    private static void testTerritoryConnections() {
        List<Territory> territories = MapBuilder.buildMap(3);

        for (Territory t : territories) {
            assert !t.getNeighbors().isEmpty() : "Each territory should have neighbors";

            for (Territory neighbor : t.getNeighbors()) {
                assert neighbor.getNeighbors().contains(t) :
                        "Neighbor relationships should be bidirectional";
            }
        }
    }
}