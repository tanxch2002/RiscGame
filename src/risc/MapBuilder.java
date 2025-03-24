package risc;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building maps based on the number of players.
 */
public class MapBuilder {

    public static List<Territory> buildMap(int desiredPlayers) {
        if (desiredPlayers == 3) {
            return buildSixMap();
        } else if (desiredPlayers == 5) {
            return buildTenMap();
        } else {
            return buildEightMap();
        }
    }

    private static List<Territory> buildSixMap() {
        List<Territory> territories = new ArrayList<>();
        Territory t1 = new Territory("A");
        Territory t2 = new Territory("B");
        Territory t3 = new Territory("C");
        Territory t4 = new Territory("D");
        Territory t5 = new Territory("E");
        Territory t6 = new Territory("F");

        t1.addNeighbor(t2);
        t1.addNeighbor(t3);

        t2.addNeighbor(t1);
        t2.addNeighbor(t4);

        t3.addNeighbor(t1);
        t3.addNeighbor(t4);
        t3.addNeighbor(t5);

        t4.addNeighbor(t2);
        t4.addNeighbor(t3);
        t4.addNeighbor(t6);

        t5.addNeighbor(t3);
        t5.addNeighbor(t6);

        t6.addNeighbor(t4);
        t6.addNeighbor(t5);

        territories.add(t1);
        territories.add(t2);
        territories.add(t3);
        territories.add(t4);
        territories.add(t5);
        territories.add(t6);
        return territories;
    }

    private static List<Territory> buildEightMap() {
        List<Territory> territories = new ArrayList<>();
        Territory t1 = new Territory("A");
        Territory t2 = new Territory("B");
        Territory t3 = new Territory("C");
        Territory t4 = new Territory("D");
        Territory t5 = new Territory("E");
        Territory t6 = new Territory("F");
        Territory t7 = new Territory("G");
        Territory t8 = new Territory("H");

        t1.addNeighbor(t2);
        t1.addNeighbor(t3);

        t2.addNeighbor(t1);
        t2.addNeighbor(t4);
        t2.addNeighbor(t5);

        t3.addNeighbor(t1);
        t3.addNeighbor(t6);
        t3.addNeighbor(t7);

        t4.addNeighbor(t2);
        t4.addNeighbor(t8);

        t5.addNeighbor(t2);
        t5.addNeighbor(t6);
        t5.addNeighbor(t8);

        t6.addNeighbor(t3);
        t6.addNeighbor(t5);
        t6.addNeighbor(t7);

        t7.addNeighbor(t3);
        t7.addNeighbor(t6);
        t7.addNeighbor(t8);

        t8.addNeighbor(t4);
        t8.addNeighbor(t5);
        t8.addNeighbor(t7);

        territories.add(t1);
        territories.add(t2);
        territories.add(t3);
        territories.add(t4);
        territories.add(t5);
        territories.add(t6);
        territories.add(t7);
        territories.add(t8);
        return territories;
    }

    private static List<Territory> buildTenMap() {
        List<Territory> territories = new ArrayList<>();
        Territory t1 = new Territory("A");
        Territory t2 = new Territory("B");
        Territory t3 = new Territory("C");
        Territory t4 = new Territory("D");
        Territory t5 = new Territory("E");
        Territory t6 = new Territory("F");
        Territory t7 = new Territory("G");
        Territory t8 = new Territory("H");
        Territory t9 = new Territory("I");
        Territory t10 = new Territory("J");

        t1.addNeighbor(t2);
        t1.addNeighbor(t3);

        t2.addNeighbor(t1);
        t2.addNeighbor(t4);
        t2.addNeighbor(t5);

        t3.addNeighbor(t1);
        t3.addNeighbor(t6);
        t3.addNeighbor(t7);

        t4.addNeighbor(t2);
        t4.addNeighbor(t8);

        t5.addNeighbor(t2);
        t5.addNeighbor(t6);
        t5.addNeighbor(t9);

        t6.addNeighbor(t3);
        t6.addNeighbor(t5);
        t6.addNeighbor(t7);
        t6.addNeighbor(t10);

        t7.addNeighbor(t3);
        t7.addNeighbor(t6);
        t7.addNeighbor(t8);

        t8.addNeighbor(t4);
        t8.addNeighbor(t7);
        t8.addNeighbor(t9);

        t9.addNeighbor(t5);
        t9.addNeighbor(t8);
        t9.addNeighbor(t10);

        t10.addNeighbor(t6);
        t10.addNeighbor(t9);

        territories.add(t1);
        territories.add(t2);
        territories.add(t3);
        territories.add(t4);
        territories.add(t5);
        territories.add(t6);
        territories.add(t7);
        territories.add(t8);
        territories.add(t9);
        territories.add(t10);
        return territories;
    }
}
