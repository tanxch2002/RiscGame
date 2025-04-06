package risc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * 全局服务器，负责：
 * 1) 账号管理（登录/注册）
 * 2) 维护并管理多个 RiscServer（多局游戏）
 */
public class GlobalServer {
    private final int port;
    // 用于存储用户的账号密码（示例中用Map模拟，实际项目可使用数据库）
    private final Map<String, String> userCredentials;
    // 在线玩家账户信息（可根据需要做更复杂的会话管理）
    private final Map<String, PlayerAccount> onlineUsers;

    // 所有游戏房间: gameID -> RiscServer
    private final Map<String, RiscServer> games;

    public GlobalServer(int port) {
        this.port = port;
        this.userCredentials = new HashMap<>();
        this.onlineUsers = new HashMap<>();
        this.games = new HashMap<>();

        // 这里示例性放一个账号
        userCredentials.put("test", "123");
    }

    public static void main(String[] args) {
        int port = 12345;   // 或者从 args[] 获取
        GlobalServer gs = new GlobalServer(port);
        gs.start();
    }

    public void start() {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("GlobalServer started, listening on port " + port);
            while (true) {
                Socket clientSocket = ss.accept();
                // 对每个新客户端，用一个线程处理其登录/注册，并选择或创建游戏
                Thread t = new Thread(() -> handleClient(clientSocket));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理一个客户端的“登录->选房->交给对应 RiscServer”的流程
     */
    private void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Welcome to the Global RISC Server.");

            // 执行登录/注册流程
            PlayerAccount account = doLoginOrRegister(in, out);
            if (account == null) {
                out.println("Login/Registration failed, closing...");
                socket.close();
                return;
            }

            // 选择或创建游戏
            String gameID = selectOrCreateGame(in, out);
            if (gameID == null) {
                out.println("No valid game selected, closing...");
                socket.close();
                return;
            }

            RiscServer server = games.get(gameID);
            if (server == null) {
                out.println("Selected game not found, closing...");
                socket.close();
                return;
            }

            // 将未关闭的 socket 传递给 RiscServer 进行后续处理
            server.addNewClient(socket, account);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 登录/注册示例
     */
    private PlayerAccount doLoginOrRegister(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Please enter 'L' to login, 'R' to register:");
        while (true) {
            String line = in.readLine();
            if (line == null) return null;
            line = line.trim().toUpperCase();
            if (line.startsWith("L")) {
                out.println("Enter username:");
                String user = in.readLine();
                out.println("Enter password:");
                String pass = in.readLine();
                if (userCredentials.containsKey(user) && userCredentials.get(user).equals(pass)) {
                    // 登录成功
                    PlayerAccount account = new PlayerAccount(user);
                    onlineUsers.put(user, account);
                    return account;
                } else {
                    out.println("Invalid credential. Try again (L/R).");
                }
            } else if (line.startsWith("R")) {
                out.println("Choose a new username:");
                String newUser = in.readLine();
                if (userCredentials.containsKey(newUser)) {
                    out.println("User already exists, pick another. (L/R?)");
                    continue;
                }
                out.println("Choose a password:");
                String newPass = in.readLine();
                // 注册
                userCredentials.put(newUser, newPass);
                out.println("Registered successfully as " + newUser);
                PlayerAccount account = new PlayerAccount(newUser);
                onlineUsers.put(newUser, account);
                return account;
            } else {
                out.println("Please press 'L' or 'R' only.");
            }
        }
    }

    /**
     * 让玩家选择加入已有游戏或创建新游戏
     */
    private String selectOrCreateGame(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Existing games: " + games.keySet());
        out.println("Use: 'join <gameID>' or 'new <numPlayers>' to create a new game");
        while (true) {
            String line = in.readLine();
            if (line == null) return null;
            line = line.trim();
            if (line.startsWith("join")) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2 && games.containsKey(parts[1])) {
                    return parts[1];
                } else {
                    out.println("Game not found. Try again.");
                }
            } else if (line.startsWith("new")) {
                // new 3
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    try {
                        int desiredPlayers = Integer.parseInt(parts[1]);
                        // 创建新游戏
                        String newID = UUID.randomUUID().toString().substring(0, 8);
                        RiscServer rs = new RiscServer(desiredPlayers, newID);
                        games.put(newID, rs);
                        // 启动该 RiscServer 的主要逻辑（如等待玩家到齐、startGame 等）
                        rs.startServerLogic();
                        out.println("New game created. ID=" + newID);
                        return newID;
                    } catch (NumberFormatException ex) {
                        out.println("Invalid numPlayers. Try again.");
                    }
                } else {
                    out.println("Usage: new <numPlayers>");
                }
            } else {
                out.println("Invalid input. Use 'join <id>' or 'new <num>'.");
            }
        }
    }
}
