package risc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.Socket;

class ClientHandlerTest {

    private Socket mockSocket;
    private ByteArrayOutputStream testOut;
    private ByteArrayInputStream testIn;
    private ClientHandler clientHandler;
    private RiscServer server;
    private Game game;

    @BeforeEach
    void setUp() throws Exception {
        // 构造模拟输入、输出流
        testOut = new ByteArrayOutputStream();
        // 初始命令行只输入 "D" 用以测试 collectOrders 的退出分支
        testIn = new ByteArrayInputStream("D\n".getBytes());

        // 用匿名类覆盖 Socket，返回我们伪造的输入/输出流
        mockSocket = new Socket() {
            @Override
            public InputStream getInputStream() {
                return testIn;
            }
            @Override
            public OutputStream getOutputStream() {
                return testOut;
            }
        };

        // 这里不真正启动服务器，只是演示
        server = new RiscServer(2);
        clientHandler = new ClientHandler(mockSocket, server, 0);
        game = new Game();
    }

    @Test
    void getPlayerID() {
        assertEquals(0, clientHandler.getPlayerID());
    }

    @Test
    void run() {
        // run() 方法中会进行输入输出流的初始化，并打印欢迎信息
        clientHandler.run();
        String output = testOut.toString();
        assertTrue(output.contains("Welcome, Player 1!"));
    }

    @Test
    void sendMessage() {
        clientHandler.run();
        clientHandler.sendMessage("Hello");
        String output = testOut.toString();
        assertTrue(output.contains("Hello"));
    }

    @Test
    void collectOrders() throws IOException {
        clientHandler.run();
        // 配置一个最小的 game 环境
        game.setUpMap(2);
        game.initPlayers(2);

        // 重置输入流，模拟玩家输入多种命令
        String commands = ""
                + "M\n"                      // Move 命令
                + "A B 3\n"                 // 格式错误（需要 3 个参数），测试报错
                + "A\n"                      // Attack 命令
                + "A B 2\n"                 // Attack 格式正确
                + "D\n";                    // Done
        testIn = new ByteArrayInputStream(commands.getBytes());

        // 重建 ClientHandler，保证它使用新的 inputStream
        ClientHandler ch2 = new ClientHandler(mockSocket, server, 0);
        ch2.run();  // 初始化 I/O
        ch2.collectOrders(game);

        String output = testOut.toString();
        // 检查是否提示输入M/A/D，提示格式等
        assertTrue(output.contains("Enter an order (M/A/D): "));
        assertTrue(output.contains("Enter format: sourceTerritory destinationTerritory numUnits"));
        assertTrue(output.contains("Enter format: sourceTerritory targetTerritory numUnits"));
        assertTrue(output.contains("Attack order added: A => B (2 units)")); // 验证 Attack 成功

        // 验证游戏里是否真的添加了对应的订单
        assertEquals(2, game.getAllOrders().size()); // 只成功添加了一个 Attack
    }



    @Test
    void closeConnection() {
        clientHandler.run();
        clientHandler.closeConnection();
        // 如果没有异常抛出，就算成功了
        assertTrue(true);
    }
}
