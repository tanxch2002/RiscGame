package risc;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/** Panel for rendering the client-side game map */
public class MapPanel extends JPanel {

    /**
     * Fixed layout: center coordinates for each territory
     */
    public static final Map<String, Point> territoryPositions = new HashMap<>();
    static {
        territoryPositions.put("A", new Point(100, 150));
        territoryPositions.put("B", new Point(250, 150));
        territoryPositions.put("C", new Point(100, 300));
        territoryPositions.put("D", new Point(400, 150));
        territoryPositions.put("E", new Point(250, 280));
        territoryPositions.put("F", new Point(200, 350));
        territoryPositions.put("G", new Point(100, 450));
        territoryPositions.put("H", new Point(350, 400));
        territoryPositions.put("I", new Point(450, 300));
        territoryPositions.put("J", new Point(300, 500));
    }

    private Map<String, ClientTerritoryData> terrs = new HashMap<>();
    private final java.util.List<MoveOrder> moves = new ArrayList<>();
    private static final Font NAME_FONT = new Font("SansSerif", Font.BOLD, 16);

    public MapPanel() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE);
    }

    /* -------- External API -------- */
    /**
     * Update the map data with new territory information
     */
    public void updateMapData(Map<String, ClientTerritoryData> data) {
        data.forEach((n, d) -> {
            Point p = territoryPositions.getOrDefault(n,
                    new Point(50, 50 + terrs.size() * 20));
            d.x = p.x; d.y = p.y;
        });
        this.terrs = data;
        revalidate();        // Ask JScrollPane to re-layout
        repaint();
    }

    /**
     * Add a move order to render
     */
    public void addMoveOrder(MoveOrder o) {
        moves.add(o);
        repaint();
    }

    /**
     * Clear all rendered move orders
     */
    public void clearMoveOrders() {
        moves.clear();
        repaint();
    }

    /* ====================== Rendering ====================== */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawLegend(g2);
        if (terrs.isEmpty()) {
            g2.drawString("Map not ready", 50, 80);
            return;
        }
        drawLinks(g2);
        drawTerritories(g2);
        drawMoves(g2);
    }

    /**
     * Draws a legend explaining map symbols
     */
    private void drawLegend(Graphics2D g) {
        int x = 20, y = 20;
        g.drawString("Legend:", x, y);
        g.drawString("Circle = Territory", x, y + 15);
        g.drawString("Gray line = Adjacency", x, y + 30);
        g.drawString("Arrow = Move", x, y + 45);
    }

    /**
     * Draw network links between adjacent territories
     */
    private void drawLinks(Graphics2D g) {
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(2));

        Set<String> seen = new HashSet<>();
        terrs.values().forEach(src -> src.neighborNames.forEach(n -> {
            ClientTerritoryData dst = terrs.get(n);
            if (dst != null) {
                String k = src.name + "-" + n, rev = n + "-" + src.name;
                if (seen.add(k) && seen.add(rev))
                    g.drawLine(src.x, src.y, dst.x, dst.y);
            }
        }));
    }

    /**
     * Draw each territory as a colored circle with labels and unit counts
     */
    private void drawTerritories(Graphics2D g) {
        for (ClientTerritoryData t : terrs.values()) {
            int r = t.radius, x = t.x, y = t.y;

            // Fill circle and draw border
            g.setColor(t.ownerColor);
            g.fillOval(x - r, y - r, 2 * r, 2 * r);
            g.setColor(Color.BLACK);
            g.drawOval(x - r, y - r, 2 * r, 2 * r);

            // Draw territory name above circle
            g.setFont(NAME_FONT);
            FontMetrics fmN = g.getFontMetrics();
            g.drawString(t.name, x - fmN.stringWidth(t.name) / 2, y - r - 4);

            // Display owner name inside circle
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            Color txtCol = isDark(t.ownerColor) ? Color.WHITE : Color.BLACK;
            g.setColor(txtCol);
            g.drawString(t.ownerName, x - fm.stringWidth(t.ownerName) / 2, y);
            g.setColor(Color.BLACK);

            // Below circle, list unit counts by player
            int offsetY = y + r + fm.getAscent();
            for (Map.Entry<String, Map<Integer, Integer>> e : t.unitsByPlayer.entrySet()) {
                StringBuilder sb = new StringBuilder(e.getKey()).append(": ");
                e.getValue().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(ev ->
                                sb.append("L").append(ev.getKey())
                                        .append(":").append(ev.getValue()).append(" "));
                String line = sb.toString().trim();
                g.drawString(line, x - fm.stringWidth(line) / 2, offsetY);
                offsetY += fm.getAscent();
            }
        }
    }

    /**
     * Draw arrows and labels for move orders
     */
    private void drawMoves(Graphics2D g) {
        if (moves.isEmpty()) return;
        g.setColor(Color.MAGENTA);
        g.setStroke(new BasicStroke(3));

        for (MoveOrder o : moves) {
            ClientTerritoryData s = terrs.get(o.getSourceName());
            ClientTerritoryData d = terrs.get(o.getDestName());
            if (s == null || d == null) continue;

            drawArrow(g, s.x, s.y, d.x, d.y);
            String t = "L" + o.getLevel() + " x" + o.getNumUnits();
            g.drawString(t, (s.x + d.x) / 2, (s.y + d.y) / 2);
        }
    }

    /**
     * Draws an arrow from (x1,y1) to (x2,y2)
     */
    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
        double phi = Math.toRadians(30);
        int barb = 10;
        double theta = Math.atan2(y2 - y1, x2 - x1);
        for (int j = 0; j < 2; j++) {
            double rho = theta + (j == 0 ? phi : -phi);
            int x = (int) (x2 - barb * Math.cos(rho));
            int y = (int) (y2 - barb * Math.sin(rho));
            g.drawLine(x2, y2, x, y);
        }
    }

    /**
     * Determines if a color is dark based on its luminance
     */
    private boolean isDark(Color c) {
        double d = 1 - (0.299 * c.getRed()
                + 0.587 * c.getGreen()
                + 0.114 * c.getBlue()) / 255.0;
        return d >= 0.5;
    }
}