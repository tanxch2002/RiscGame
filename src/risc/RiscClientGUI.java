package risc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

/**
 * RISC 客户端的简单 Swing GUI，
 * 在这里添加了实时显示地图的功能。
 */
public class RiscClientGUI extends JFrame {
    private JTextArea textArea;     // 用于显示普通服务器消息
    private JTextArea mapTextArea;  // 用于显示地图状态的区域
    private JTextField inputField;  // 用户输入指令
    private JButton sendButton;
    private JButton connectButton;

    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;

    // 默认主机和端口，可根据需求修改
    private String host = "localhost";
    private int port = 12345;

    public RiscClientGUI() {
        super("RISC 客户端 GUI");
        setupUI();
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * 初始化界面布局
     */
    private void setupUI() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 上方的“连接”区域
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("主机:"));
        JTextField hostField = new JTextField(host, 10);
        topPanel.add(hostField);
        topPanel.add(new JLabel("端口:"));
        JTextField portField = new JTextField(String.valueOf(port), 5);
        topPanel.add(portField);

        connectButton = new JButton("连接");
        topPanel.add(connectButton);

        // 中间区域 - 左侧放普通消息，右侧放地图
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 5, 5));

        // 左侧显示一般服务器消息
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane leftScroll = new JScrollPane(textArea);
        leftScroll.setBorder(BorderFactory.createTitledBorder("服务器消息"));
        centerPanel.add(leftScroll);

        // 右侧显示地图
        mapTextArea = new JTextArea();
        mapTextArea.setEditable(false);
        JScrollPane rightScroll = new JScrollPane(mapTextArea);
        rightScroll.setBorder(BorderFactory.createTitledBorder("地图状态"));
        centerPanel.add(rightScroll);

        // 底部输入区域
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("发送");
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // 组合
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);

        // 按钮事件
        connectButton.addActionListener(e -> {
            this.host = hostField.getText().trim();
            try {
                this.port = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException ex) {
                appendMessage("端口号不合法。\n");
                return;
            }
            handleConnect(e);
        });
        sendButton.addActionListener(this::handleSend);
    }

    /**
     * 连接服务器的逻辑
     */
    private void handleConnect(ActionEvent e) {
        try {
            socket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new PrintWriter(socket.getOutputStream(), true);

            appendMessage("已连接到服务器: " + host + ":" + port + "\n");
            connectButton.setEnabled(false);

            // 启动后台线程持续读取服务器消息
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMsg;
                    // 可选：用于标记当前是否在读取地图的“连续行”
                    boolean readingMap = false;

                    while ((serverMsg = serverIn.readLine()) != null) {
                        // 如果服务器发来的消息带有“Map state”之类关键字，说明后面跟的是地图信息
                        if (serverMsg.contains("map state") || serverMsg.contains("Map state")) {
                            // 为了防止一次性刷新，先清空 mapTextArea
                            mapTextArea.setText("");
                            // 先把当前行一起放到地图区
                            appendMapLine(serverMsg + "\n");
                            readingMap = true;
                            continue;
                        }

                        // 如果我们处在“地图读取”状态，但遇到空行或别的提示，可能说明地图部分结束了
                        // 这里演示：如果读到空行，就算结束
                        if (readingMap) {
                            if (serverMsg.trim().isEmpty()) {
                                readingMap = false;
                            } else {
                                // 如果看起来还是领地信息（形如 "A(Player1): 10 units, neighbors: ..."）
                                // 就继续显示在地图区域
                                appendMapLine(serverMsg + "\n");
                                continue;
                            }
                        }

                        // 如果既不是“地图关键字”，也不在地图读取中，就输出到普通消息窗口
                        appendMessage(serverMsg + "\n");
                    }
                } catch (IOException ex) {
                    appendMessage("与服务器的连接已断开。\n");
                }
            });
            readerThread.start();

        } catch (IOException ex) {
            appendMessage("连接失败: " + ex.getMessage() + "\n");
        }
    }

    /**
     * 发送用户指令给服务器
     */
    private void handleSend(ActionEvent e) {
        String userMsg = inputField.getText().trim();
        if (userMsg.isEmpty() || serverOut == null) {
            return;
        }
        serverOut.println(userMsg);
        inputField.setText("");
    }

    /**
     * 在普通消息区追加文本（线程安全）
     */
    private void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> textArea.append(msg));
    }

    /**
     * 在地图区域追加文本（线程安全）
     */
    private void appendMapLine(String line) {
        SwingUtilities.invokeLater(() -> mapTextArea.append(line));
    }

    /**
     * main 方法，直接运行本界面
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RiscClientGUI clientGUI = new RiscClientGUI();
            clientGUI.setVisible(true);
        });
    }
}
