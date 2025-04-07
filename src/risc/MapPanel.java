package risc;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A JPanel that graphically displays the RISC game map.
 */
public class MapPanel extends JPanel {

    // Stores territory data using territory name as the key.
    private Map<String, ClientTerritoryData> territoriesData = new HashMap<>();

    // List to hold active move orders.
    private List<MoveOrder> moveOrders = new ArrayList<>();

    // Predefined layout with positions shifted down to avoid legend overlap.
    public static final Map<String, Point> territoryPositions = new HashMap<>();

    // A larger, bold font for territory names.
    private static final Font TERRITORY_NAME_FONT = new Font("SansSerif", Font.BOLD, 16);

    static {
        /*
         * Shift everything ~50px down from the original example
         * and adjust E further to avoid overlap with B.
         */
        territoryPositions.put("A", new Point(100, 150));
        territoryPositions.put("B", new Point(250, 150));
        territoryPositions.put("C", new Point(100, 300));
        territoryPositions.put("D", new Point(400, 150));
        // E moved from (250, 250) to (250, 280) to avoid overlap with B.
        territoryPositions.put("E", new Point(250, 280));
        territoryPositions.put("F", new Point(200, 350));
        territoryPositions.put("G", new Point(100, 450));
        territoryPositions.put("H", new Point(350, 400));
        territoryPositions.put("I", new Point(450, 300));
        territoryPositions.put("J", new Point(300, 500));
    }

    public MapPanel() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE);
    }

    /**
     * Updates the map data and triggers a repaint.
     * @param newData A map where the key is the territory name and the value is its data.
     */
    public void updateMapData(Map<String, ClientTerritoryData> newData) {
        // Ensure positions are assigned from the predefined layout.
        for (Map.Entry<String, ClientTerritoryData> entry : newData.entrySet()) {
            String name = entry.getKey();
            ClientTerritoryData data = entry.getValue();
            if (territoryPositions.containsKey(name)) {
                Point pos = territoryPositions.get(name);
                data.x = pos.x;
                data.y = pos.y;
            } else {
                // Fallback for unknown territories.
                System.err.println("Warning: No position defined for territory: " + name);
                data.x = 50;
                data.y = 50 + territoriesData.size() * 10;
            }
        }
        this.territoriesData = newData;
        repaint(); // Request a redraw.
    }

    /**
     * Adds a move order to be drawn on the map.
     */
    public void addMoveOrder(MoveOrder order) {
        moveOrders.add(order);
        repaint();
    }

    /**
     * Clears all move orders.
     */
    public void clearMoveOrders() {
        moveOrders.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        // Enable anti-aliasing for smoother lines and text.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the legend first.
        drawLegend(g2d);

        if (territoriesData == null || territoriesData.isEmpty()) {
            g2d.drawString("Map data not yet available...", 50, 80);
        } else {
            // --- Draw Connections (Lines) ---
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(2)); // Thicker lines.
            Set<String> drawnConnections = new HashSet<>();

            for (ClientTerritoryData src : territoriesData.values()) {
                for (String neighborName : src.getNeighborNames()) {
                    ClientTerritoryData dest = territoriesData.get(neighborName);
                    if (dest != null) {
                        String connKey1 = src.getName() + "-" + dest.getName();
                        String connKey2 = dest.getName() + "-" + src.getName();
                        if (!drawnConnections.contains(connKey1) && !drawnConnections.contains(connKey2)) {
                            g2d.drawLine(src.getX(), src.getY(), dest.getX(), dest.getY());
                            drawnConnections.add(connKey1);
                        }
                    }
                }
            }

            // --- Draw Territories (Circles and Text) ---
            for (ClientTerritoryData tData : territoriesData.values()) {
                int x = tData.getX();
                int y = tData.getY();
                int r = tData.getRadius();

                // Draw the territory circle.
                g2d.setColor(tData.getOwnerColor());
                g2d.fillOval(x - r, y - r, 2 * r, 2 * r);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(x - r, y - r, 2 * r, 2 * r);

                // --- Display Territory Information ---
                Font originalFont = g2d.getFont();

                // 1) Territory Name using the larger, bold font.
                g2d.setFont(TERRITORY_NAME_FONT);
                FontMetrics fmName = g2d.getFontMetrics();
                int nameHeight = fmName.getAscent();
                int currentY = y - r - nameHeight;
                String nameStr = tData.getName();
                int nameWidth = fmName.stringWidth(nameStr);
                g2d.drawString(nameStr, x - nameWidth / 2, currentY);

                // Restore the original font for other texts.
                g2d.setFont(originalFont);
                FontMetrics fm = g2d.getFontMetrics();
                int textHeight = fm.getAscent();
                currentY += nameHeight + 2;

                // 2) Owner Name (if any).
                String ownerStr = tData.getOwnerName().equals("None") ? "" : tData.getOwnerName();
                if (!ownerStr.isEmpty()) {
                    int ownerWidth = fm.stringWidth(ownerStr);
                    g2d.setColor(isColorDark(tData.getOwnerColor()) ? Color.WHITE : Color.BLACK);
                    g2d.drawString(ownerStr, x - ownerWidth / 2, y - r / 3);
                    g2d.setColor(Color.BLACK);
                }

                // 3) Units.
                List<Integer> levels = new ArrayList<>(tData.getUnits().keySet());
                Collections.sort(levels);
                StringBuilder unitsStr = new StringBuilder();
                for (int level : levels) {
                    unitsStr.append("L").append(level).append(":")
                            .append(tData.getUnits().get(level)).append(" ");
                }
                if (unitsStr.length() == 0) {
                    unitsStr.append("No Units");
                }
                int unitsWidth = fm.stringWidth(unitsStr.toString().trim());
                g2d.setColor(isColorDark(tData.getOwnerColor()) ? Color.WHITE : Color.BLACK);
                g2d.drawString(unitsStr.toString().trim(), x - unitsWidth / 2, y + textHeight / 2);
                g2d.setColor(Color.BLACK);

                // 4) Size & Resource Production (below the circle).
                currentY = y + r + textHeight + 2;
                String sizeStr = "Sz:" + tData.getSize();
                String prodStr = "F:" + tData.getFoodProduction() + " T:" + tData.getTechProduction();
                g2d.drawString(sizeStr, x - fm.stringWidth(sizeStr) / 2, currentY);
                currentY += textHeight;
                g2d.drawString(prodStr, x - fm.stringWidth(prodStr) / 2, currentY);
            }
        }

        // --- Draw Move Orders ---
        drawMoveOrders(g2d);
    }

    /**
     * Draws a legend in the top-left corner to explain the map elements.
     */
    private void drawLegend(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        int legendX = 20;
        int legendY = 20;
        g2d.drawString("图例 (Legend):", legendX, legendY);
        g2d.drawString("• 圆圈代表领地 (Territory)", legendX, legendY + 15);
        g2d.drawString("• 线条表示相邻关系，可移动/进攻 (Adjacency)", legendX, legendY + 30);
        g2d.drawString("• Sz=Size, F=Food, T=Tech (资源产量)", legendX, legendY + 45);
        g2d.drawString("• 箭头表示移动 (Move Order)", legendX, legendY + 60);
    }

    /**
     * Draws the move orders as arrows on the map.
     */
    private void drawMoveOrders(Graphics2D g2d) {
        if (moveOrders == null || moveOrders.isEmpty()) {
            return;
        }
        g2d.setColor(Color.MAGENTA);
        g2d.setStroke(new BasicStroke(3));
        for (MoveOrder order : moveOrders) {
            // Get source and destination centers from the territory positions using getSourceName() and getDestName().
            ClientTerritoryData src = territoriesData.get(order.getSourceName());
            ClientTerritoryData dest = territoriesData.get(order.getDestName());
            if (src != null && dest != null) {
                int x1 = src.getX();
                int y1 = src.getY();
                int x2 = dest.getX();
                int y2 = dest.getY();
                drawArrow(g2d, x1, y1, x2, y2);
                // Draw the move order text along the arrow.
                String text = "L" + order.getLevel() + " x" + order.getNumUnits();
                int tx = (x1 + x2) / 2;
                int ty = (y1 + y2) / 2;
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2d.drawString(text, tx, ty);
            }
        }
    }

    /**
     * Draws an arrow from (x1,y1) to (x2,y2).
     */
    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        // Draw main line.
        g2d.drawLine(x1, y1, x2, y2);
        // Draw arrowhead.
        double phi = Math.toRadians(30);
        int barb = 10;
        double dy = y2 - y1;
        double dx = x2 - x1;
        double theta = Math.atan2(dy, dx);
        double x, y;
        for (int j = 0; j < 2; j++) {
            double rho = theta + (j == 0 ? phi : -phi);
            x = x2 - barb * Math.cos(rho);
            y = y2 - barb * Math.sin(rho);
            g2d.drawLine(x2, y2, (int)x, (int)y);
        }
    }

    // Helper method to decide if a color is dark (for choosing text color).
    private boolean isColorDark(Color color) {
        double darkness = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return darkness >= 0.5;
    }
}
