package risc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced test class for MapPanel with improved coverage.
 * This version doesn't use Mockito.
 */
class MapPanelTest {

    private MapPanel panel;
    private Map<String, ClientTerritoryData> testData;

    @BeforeEach
    void setUp() {
        panel = new MapPanel();
        testData = new HashMap<>();

        // Create test territories
        for (String name : new String[]{"A", "B", "C"}) {
            Point position = MapPanel.territoryPositions.getOrDefault(name, new Point(100 * testData.size(), 100));
            ClientTerritoryData territory = new ClientTerritoryData(name, position.x, position.y);

            // Set sample data
            Map<Integer, Integer> units = new HashMap<>();
            units.put(0, 5);
            units.put(1, 3);

            List<String> neighbors = new ArrayList<>();
            if (!name.equals("A")) neighbors.add("A");
            if (!name.equals("B")) neighbors.add("B");
            if (!name.equals("C")) neighbors.add("C");

            territory.updateData(
                    "Player" + (testData.size() + 1),
                    2, // size
                    neighbors,
                    units,
                    getColorForPlayer(testData.size()),
                    3, // food production
                    2  // tech production
            );

            testData.put(name, territory);
        }
    }

    private Color getColorForPlayer(int index) {
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.ORANGE};
        return colors[index % colors.length];
    }

    @Test
    void testConstructor() {
        assertEquals(600, panel.getPreferredSize().width);
        assertEquals(600, panel.getPreferredSize().height);
        assertEquals(Color.WHITE, panel.getBackground());
    }

    @Test
    void testUpdateMapData() {
        panel.updateMapData(testData);

        // Create a graphics context to force paintComponent to execute
        BufferedImage testImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = testImage.createGraphics();
        panel.paintComponent(g2d);
        g2d.dispose();

        // If no exceptions were thrown, the test passes
        assertTrue(true);
    }

    @Test
    void testMoveOrderHandling() {
        panel.updateMapData(testData);

        // Add move orders
        MoveOrder order1 = new MoveOrder(0, "A", "B", 1, 3);
        MoveOrder order2 = new MoveOrder(0, "B", "C", 0, 2);

        panel.addMoveOrder(order1);
        panel.addMoveOrder(order2);

        // Create a graphics context to force paintComponent to execute
        BufferedImage testImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = testImage.createGraphics();
        panel.paintComponent(g2d);

        // Clear orders and verify it doesn't cause errors
        panel.clearMoveOrders();
        panel.paintComponent(g2d);
        g2d.dispose();

        // If no exceptions were thrown, the test passes
        assertTrue(true);
    }

    @Test
    void testPaintComponent_EmptyData() {
        // Test paintComponent with empty data
        BufferedImage testImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = testImage.createGraphics();
        panel.paintComponent(g2d);
        g2d.dispose();

        // If no exceptions were thrown, the test passes
        assertTrue(true);
    }

    @Test
    void testDrawArrow() {
        // Get access to the private drawArrow method using reflection
        java.lang.reflect.Method drawArrowMethod = null;
        try {
            drawArrowMethod = MapPanel.class.getDeclaredMethod("drawArrow", Graphics2D.class, int.class, int.class, int.class, int.class);
            drawArrowMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            fail("Failed to access drawArrow method: " + e.getMessage());
        }

        // Create a graphics context
        BufferedImage testImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = testImage.createGraphics();

        // Call the drawArrow method
        try {
            drawArrowMethod.invoke(panel, g2d, 100, 100, 300, 300);
        } catch (Exception e) {
            fail("Failed to call drawArrow method: " + e.getMessage());
        }

        g2d.dispose();

        // If no exceptions were thrown, the test passes
        assertTrue(true);
    }

    @Test
    void testDrawLegend() {
        // Get access to the private drawLegend method using reflection
        java.lang.reflect.Method drawLegendMethod = null;
        try {
            drawLegendMethod = MapPanel.class.getDeclaredMethod("drawLegend", Graphics2D.class);
            drawLegendMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            fail("Failed to access drawLegend method: " + e.getMessage());
        }

        // Create a graphics context
        BufferedImage testImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = testImage.createGraphics();

        // Call the drawLegend method
        try {
            drawLegendMethod.invoke(panel, g2d);
        } catch (Exception e) {
            fail("Failed to call drawLegend method: " + e.getMessage());
        }

        g2d.dispose();

        // If no exceptions were thrown, the test passes
        assertTrue(true);
    }

    @Test
    void testDrawMoveOrders() {
        // Get access to the private drawMoveOrders method using reflection
        java.lang.reflect.Method drawMoveOrdersMethod = null;
        try {
            drawMoveOrdersMethod = MapPanel.class.getDeclaredMethod("drawMoveOrders", Graphics2D.class);
            drawMoveOrdersMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            fail("Failed to access drawMoveOrders method: " + e.getMessage());
        }

        // Update map with test data
        panel.updateMapData(testData);

        // Add move orders
        MoveOrder order1 = new MoveOrder(0, "A", "B", 1, 3);
        panel.addMoveOrder(order1);

        // Create a graphics context
        BufferedImage testImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = testImage.createGraphics();

        // Call the drawMoveOrders method
        try {
            drawMoveOrdersMethod.invoke(panel, g2d);
        } catch (Exception e) {
            fail("Failed to call drawMoveOrders method: " + e.getMessage());
        }

        g2d.dispose();

        // If no exceptions were thrown, the test passes
        assertTrue(true);
    }

    @Test
    void testIsColorDark() {
        // Get access to the private isColorDark method using reflection
        java.lang.reflect.Method isColorDarkMethod = null;
        try {
            isColorDarkMethod = MapPanel.class.getDeclaredMethod("isColorDark", Color.class);
            isColorDarkMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            fail("Failed to access isColorDark method: " + e.getMessage());
        }

        // Test with various colors
        try {
            assertTrue((Boolean) isColorDarkMethod.invoke(panel, Color.BLACK));
            assertTrue((Boolean) isColorDarkMethod.invoke(panel, new Color(0, 0, 100)));
            assertTrue((Boolean) isColorDarkMethod.invoke(panel, new Color(50, 50, 50)));

            assertFalse((Boolean) isColorDarkMethod.invoke(panel, Color.WHITE));
            assertFalse((Boolean) isColorDarkMethod.invoke(panel, Color.YELLOW));
            assertFalse((Boolean) isColorDarkMethod.invoke(panel, new Color(200, 200, 200)));
        } catch (Exception e) {
            fail("Failed to call isColorDark method: " + e.getMessage());
        }
    }

    @Test
    void testPaintComponent_WithTerritoriesAndMoveOrders() {
        // Set up panel with test data
        panel.updateMapData(testData);

        // Add move orders
        panel.addMoveOrder(new MoveOrder(0, "A", "B", 1, 3));
        panel.addMoveOrder(new MoveOrder(0, "B", "C", 0, 2));

        // Create a graphics context
        BufferedImage testImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = testImage.createGraphics();

        // Call paintComponent
        panel.paintComponent(g2d);
        g2d.dispose();

        // If no exceptions were thrown, the test passes
        assertTrue(true);
    }

    @Test
    void testPaintComponent_WithNonexistentTerritoryMoveOrder() {
        // Set up panel with test data
        panel.updateMapData(testData);

        // Add move order with territory that doesn't exist in the data
        panel.addMoveOrder(new MoveOrder(0, "A", "Z", 1, 3));

        // Create a graphics context
        BufferedImage testImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = testImage.createGraphics();

        // Call paintComponent - should handle nonexistent territory gracefully
        panel.paintComponent(g2d);
        g2d.dispose();

        // If no exceptions were thrown, the test passes
        assertTrue(true);
    }
}