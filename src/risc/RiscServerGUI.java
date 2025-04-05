package risc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * RISC 服务器的简单 Swing GUI 实现，
 * 将原先基于控制台的 RiscServer 逻辑包装到窗口界面中。
 */
public class RiscServerGUI extends JFrame {
    private JTextArea textArea;        // 显示服务器日志
    private JButton startServerButton; // 启动服务器
    private JTextField numPlayersField;

    private int desiredPlayers = 2;    // 默认玩家数量
    private final int PORT = RiscServer.PORT; // 默认端口 12345

    private ServerSocket serverSocket;
    private boolean isServerRunning = false;
    private Thread serverThread;

    // 我们这里也可以直接持有一个 RiscServer 的实例
    private RiscServer riscServer;

    public RiscServerGUI() {
        super("RISC 服务器 GUI");
        setupUI();
        setSize(550, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 顶部面板，输入想要的玩家数
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("玩家数量 (2-5):"));
        numPlayersField = new JTextField(String.valueOf(desiredPlayers), 5);
        topPanel.add(numPlayersField);

        startServerButton = new JButton("启动服务器");
        topPanel.add(startServerButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 用于显示服务器日志的文本区域
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 按钮事件
        startServerButton.addActionListener(this::handleStartServer);

        getContentPane().add(mainPanel);
    }

    /**
     * 点击“启动服务器”后执行。
     */
    private void handleStartServer(ActionEvent e) {
        if (isServerRunning) {
            appendMessage("服务器已经在运行。\n");
            return;
        }
        // 校验玩家数量
        try {
            int np = Integer.parseInt(numPlayersField.getText().trim());
            if (np < RiscServer.MIN_PLAYERS || np > RiscServer.MAX_PLAYERS) {
                appendMessage("玩家数量不合法，必须在 2 到 5 之间。\n");
                return;
            }
            desiredPlayers = np;
        } catch (NumberFormatException ex) {
            appendMessage("玩家数量输入格式不合法。\n");
            return;
        }

        // 启动服务器线程
        serverThread = new Thread(() -> runServer());
        serverThread.start();
        isServerRunning = true;
        startServerButton.setEnabled(false);
    }

    /**
     * 实际的服务器逻辑运行在此线程中。
     */
    private void runServer() {
        try (ServerSocket ss = new ServerSocket(PORT)) {
            this.serverSocket = ss;
            appendMessage("服务器已启动，端口号: " + PORT + "\n");
            appendMessage("等待 " + desiredPlayers + " 名玩家连接...\n");

            // 新建一个 RiscServer 实例
            riscServer = new RiscServer(desiredPlayers) {
                // 重写部分方法，将日志输出到我们的 GUI
                @Override
                public void runServer() {
                    try {
                        while (getClientHandlers().size() < desiredPlayers) {
                            Socket socket = ss.accept();
                            appendMessage("玩家连接: " + socket.getInetAddress() + "\n");
                            ClientHandler handler = new ClientHandler(socket, this, getClientHandlers().size());
                            getClientHandlers().add(handler);
                            handler.start();

                            if (getClientHandlers().size() == desiredPlayers) {
                                appendMessage("所有玩家已连接，开始游戏。\n");
                                break;
                            }
                        }
                        // 调用 RiscServer 的 startGame() 逻辑
                        startGame();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void broadcastMessage(String msg) {
                    // 覆盖广播方法，把信息显示到 GUI
                    appendMessage("[广播] " + msg + "\n");
                    super.broadcastMessage(msg);
                }

                // 内部方法，用于在 GUI 中追加文本
                private void appendMessage(String m) {
                    SwingUtilities.invokeLater(() -> textArea.append(m));
                }
            };

            // 运行服务器的主要逻辑
            riscServer.runServer();

        } catch (IOException ex) {
            appendMessage("服务器启动失败: " + ex.getMessage() + "\n");
        }
    }

    /**
     * 在文本区域中追加消息（线程安全）。
     */
    private void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> textArea.append(msg));
    }

    /**
     * main 方法，用于启动服务器 GUI。
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RiscServerGUI serverGUI = new RiscServerGUI();
            serverGUI.setVisible(true);
        });
    }
}
