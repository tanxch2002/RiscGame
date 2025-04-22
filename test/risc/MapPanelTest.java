package risc;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class MapPanelTest {
    public static void main(String[] args) {
        testInitialization();
        testUpdateMapData();
        testMoveOrders();
        testPainting();
    }

    private static void testInitialization() {
        MapPanel panel = new MapPanel();
        assert panel.getPreferredSize().width == 600 : "Width should be 600";
        assert panel.getPreferredSize().height == 600 : "Height should be 600";
    }

    private static void testUpdateMapData() {
        MapPanel panel = new MapPanel();
        Map<String, ClientTerritoryData> data = new HashMap<>();

        ClientTerritoryData terr1 = new ClientTerritoryData("A", 0, 0);
        data.put("A", terr1);

        ClientTerritoryData terr2 = new ClientTerritoryData("NewTerr", 0, 0);
        data.put("NewTerr", terr2);

        panel.updateMapData(data);
    }

    private static void testMoveOrders() {
        MapPanel panel = new MapPanel();
        MoveOrder order = new MoveOrder(1, "A", "B", 2, 5);

        panel.addMoveOrder(order);
        panel.clearMoveOrders();
    }

    private static void testPainting() {
        MapPanel panel = new MapPanel();
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();

        panel.paintComponent(g);

        Map<String, ClientTerritoryData> data = new HashMap<>();
        ClientTerritoryData terrA = new ClientTerritoryData("A", 100, 150);
        ClientTerritoryData terrB = new ClientTerritoryData("B", 250, 150);
        terrB.neighborNames.add("A");
        data.put("A", terrA);
        data.put("B", terrB);

        panel.updateMapData(data);
        panel.paintComponent(g);

        g.dispose();
    }
}