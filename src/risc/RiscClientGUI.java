package risc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.regex.*;

/** Swing 客户端 GUI   */
public class RiscClientGUI extends JFrame {

    /* -------- UI 元件 -------- */
    private JTextArea textArea;
    private MapPanel mapPanel;
    private JTextField inputField;
    private JButton sendBtn, connectBtn;

    /* -------- 网络 -------- */
    private Socket sock;
    private BufferedReader in;
    private PrintWriter out;

    private String host = "localhost";
    private int    port = 12345;

    /* -------- 颜色映射 -------- */
    private static final Map<String, Color> playerColors = new HashMap<>();
    private static final Color[] palette = {
            Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN};
    private static int colorIdx = 0;

    /* -------- 正则 -------- */
    private static final Pattern TERR_P  = Pattern.compile("^(\\w+)\\s*\\(([^)]+)\\)");
    private static final Pattern SIZE_P  = Pattern.compile("Size:\\s*(\\d+)\\s*,\\s*Neighbors:\\s*(.*)");
    private static final Pattern UNIT_P  = Pattern.compile("StationedUnits:\\s*(.*)");
    private static final Pattern PLAYER_BLOCK_P = Pattern.compile("P(\\d+)->\\{([^}]*)}");
    private static final Pattern LV_CNT_P = Pattern.compile("(\\d+)=(\\d+)");
    private static final Pattern MOVE_P  = Pattern.compile(
            "Move order added: L(\\d+) x(\\d+) from (\\w+) -> (\\w+)");

    public RiscClientGUI() {
        super("RISC Client GUI");
        buildUI();
        setMinimumSize(new Dimension(900, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private Color colorFor(String player) {
        if (player == null || player.equals("None")) return Color.LIGHT_GRAY;
        return playerColors.computeIfAbsent(player,
                k -> palette[colorIdx++ % palette.length]);
    }

    /* ====================== UI ====================== */
    private void buildUI() {
        /* ----- 上栏 host / port ----- */
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField hostF = new JTextField(host, 10);
        JTextField portF = new JTextField(String.valueOf(port), 5);
        connectBtn = new JButton("Connect");
        top.add(new JLabel("Host:"));
        top.add(hostF);
        top.add(new JLabel("Port:"));
        top.add(portF);
        top.add(connectBtn);

        /* ----- 地图 & 消息 ----- */
        mapPanel = new MapPanel();
        JScrollPane mapScr = new JScrollPane(mapPanel);
        mapScr.setBorder(BorderFactory.createTitledBorder("Game Map"));

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane msgScr = new JScrollPane(textArea);
        msgScr.setBorder(BorderFactory.createTitledBorder("Server Messages"));
        msgScr.setPreferredSize(new Dimension(260, 200));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapScr, msgScr);
        split.setResizeWeight(0.75);

        /* ----- 底栏输入 ----- */
        inputField = new JTextField();
        sendBtn = new JButton("Send");
        sendBtn.setEnabled(false);
        inputField.setEnabled(false);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendBtn, BorderLayout.EAST);

        /* ----- 布局 ----- */
        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        /* ----- 监听 ----- */
        connectBtn.addActionListener(e -> {
            host = hostF.getText().trim();
            try {
                port = Integer.parseInt(portF.getText().trim());
                connect();
            } catch (NumberFormatException ex) {
                log("Invalid port.\n");
            }
        });

        Action sendAct = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { send(); }
        };
        sendBtn.addActionListener(sendAct);
        inputField.addActionListener(sendAct);
    }

    /* ====================== 网络 ====================== */
    private void connect() {
        try {
            if (sock != null && !sock.isClosed()) sock.close();
            sock = new Socket(host, port);
            in  = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(sock.getOutputStream(), true);

            log("Connected to " + host + ":" + port + "\n");
            connectBtn.setEnabled(false);
            sendBtn.setEnabled(true);
            inputField.setEnabled(true);

            initMap();
            new Thread(this::reader).start();
        } catch (IOException ex) {
            log("Connect failed: " + ex.getMessage() + "\n");
        }
    }

    private void send() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty() && out != null) {
            out.println(msg);
            inputField.setText("");
        }
    }

    private void log(String s) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(s);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    /* ====================== 地图辅助 ====================== */
    private void initMap() {
        Map<String, ClientTerritoryData> init = new HashMap<>();
        MapPanel.territoryPositions.forEach((n, p) ->
                init.put(n, new ClientTerritoryData(n, p.x, p.y)));
        SwingUtilities.invokeLater(() -> mapPanel.updateMapData(init));
    }

    /* ====================== 读取线程 ====================== */
    private void reader() {
        try {
            boolean reading = false;
            Map<String, ClientTerritoryData> tmp = new HashMap<>();
            ClientTerritoryData cur = null;

            String line;
            while ((line = in.readLine()) != null) {

                /* ----- Move 信息 ----- */
                Matcher mv = MOVE_P.matcher(line);
                if (mv.find()) {
                    mapPanel.addMoveOrder(new MoveOrder(
                            0, mv.group(3), mv.group(4),
                            Integer.parseInt(mv.group(1)),
                            Integer.parseInt(mv.group(2))));
                    log(line + "\n");
                    continue;
                }

                /* ----- MapState 标记 ----- */
                if (line.contains("===== Current Map State")) {
                    reading = true; tmp.clear(); cur = null;
                    log(line + "\n");
                    continue;
                }
                if (line.contains("============================")) {
                    reading = false;
                    SwingUtilities.invokeLater(
                            () -> mapPanel.updateMapData(new HashMap<>(tmp)));
                    log(line + "\n");
                    continue;
                }

                if (reading) {
                    String t = line.trim();
                    Matcher mTerr = TERR_P.matcher(t);
                    Matcher mSize = SIZE_P.matcher(t);
                    Matcher mUnit = UNIT_P.matcher(t);

                    /* --- 领地行 --- */
                    if (mTerr.matches()) {
                        String name  = mTerr.group(1);
                        String owner = mTerr.group(2);
                        Point pos = MapPanel.territoryPositions
                                .getOrDefault(name, new Point(50, 50));
                        cur = tmp.computeIfAbsent(name,
                                k -> new ClientTerritoryData(k, pos.x, pos.y));
                        cur.ownerName  = owner;
                        cur.ownerColor = colorFor(owner);
                        cur.neighborNames.clear();
                        cur.unitsByPlayer.clear();
                    }
                    /* --- Size / 邻接 --- */
                    else if (mSize.matches() && cur != null) {
                        cur.size = Integer.parseInt(mSize.group(1));
                        cur.neighborNames.clear();
                        String ns = mSize.group(2).trim();
                        if (!ns.isBlank())
                            cur.neighborNames.addAll(List.of(ns.split("\\s+")));
                        cur.foodProduction = cur.techProduction = cur.size;
                    }
                    /* --- Units --- */
                    else if (mUnit.matches() && cur != null) {
                        cur.unitsByPlayer.clear();
                        String all = mUnit.group(1);
                        Matcher pb = PLAYER_BLOCK_P.matcher(all);
                        while (pb.find()) {
                            String tag = "P" + pb.group(1);
                            String inside = pb.group(2);
                            Matcher lv = LV_CNT_P.matcher(inside);
                            while (lv.find()) {
                                int level = Integer.parseInt(lv.group(1));
                                int cnt   = Integer.parseInt(lv.group(2));
                                cur.unitsByPlayer
                                        .computeIfAbsent(tag, k -> new HashMap<>())
                                        .put(level, cnt);
                            }
                        }
                    }
                } else {
                    log(line + "\n");
                }
            }
        } catch (IOException ex) {
            log("Disconnected.\n");
        } finally {
            SwingUtilities.invokeLater(() -> {
                try { if (sock != null) sock.close(); } catch (IOException ignored) {}
                connectBtn.setEnabled(true);
                sendBtn.setEnabled(false);
                inputField.setEnabled(false);
            });
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new RiscClientGUI().setVisible(true));
    }
}
