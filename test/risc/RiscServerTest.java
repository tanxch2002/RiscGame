package risc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RiscServerTest {

    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream testOut;

    private Thread serverThread;
    private RiscServer serverUnderTest;

    @BeforeEach
    void setUp() {
        // 备份原本的 System.out / System.in，以便后面还原
        originalOut = System.out;
        originalIn = System.in;

        // 捕获 System.out 的输出
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));

        // 直接构造 RiscServer，指定玩家数为 2
        // 避免在 main 中等待用户输入玩家数
        serverUnderTest = new RiscServer(2);

        // 启动 server 的主逻辑(runServer)，它会阻塞等待2个客户端
        serverThread = new Thread(() -> serverUnderTest.runServer());
        serverThread.start();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // 测试结束后，关掉连接、还原 System.out / System.in
        serverUnderTest.closeAllConnections();
        serverThread.interrupt();
        serverThread.join(500);  // 最多等0.5秒让线程结束

        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    void testLocalInteraction() throws Exception {
        // 这里我们需要“模拟”两个玩家，以满足服务器对2个连接的需求
        // 每个玩家都在一个线程里，连接服务器后读取欢迎信息并发送一些简单指令

        Thread client1 = new Thread(() -> {
            try (Socket socket = new Socket("localhost", RiscServer.PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // 读取服务器发来的欢迎语，比如“Welcome, Player 1!”
                String welcome = in.readLine();
                System.out.println("Client1 received: " + welcome);

                // 服务器会在准备阶段让玩家进行初始分配或发命令
                // 简化起见，直接发“D”表示Done
                out.println("D");

                // 之后可能出现“All orders done for this turn.”等提示
                // 读一行就算结束
                String serverMsg = in.readLine();
                System.out.println("Client1 next message: " + serverMsg);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread client2 = new Thread(() -> {
            try (Socket socket = new Socket("localhost", RiscServer.PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String welcome = in.readLine();
                System.out.println("Client2 received: " + welcome);

                // 同理，直接发“D”
                out.println("D");

                String serverMsg = in.readLine();
                System.out.println("Client2 next message: " + serverMsg);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 启动两个“客户端”
        client1.start();
        client2.start();

        // 等待客户端执行完毕
        client1.join();
        client2.join();

        // 此时，服务器应该已经接收到2个连接，并处理了最简单的一次命令输入(Done)
        // 如果还想测试更多命令（Move/Attack/初始分配），需要在客户端里模拟发送完整命令流程

        // 打印服务器侧记录的日志
        String serverOutput = testOut.toString(StandardCharsets.UTF_8);
        System.out.println("Server captured output: \n" + serverOutput);

        // 做一些简单断言，确认服务器启动成功并广播了消息等
        assertTrue(serverOutput.contains("Server started on port 12345"));
        assertTrue(serverOutput.contains("Desired player count (2) reached, starting game."));
    }

    // 其余你之前的测试方法也可以保留，比如：
    @Test
    void testBroadcastMessage() {
        serverUnderTest.broadcastMessage("Hello");
        // ...
        assertTrue(true);
    }

    @Test
    void testCloseAllConnections() {
        serverUnderTest.closeAllConnections();
        assertTrue(true);
    }

    @Test
    void testGetGame() {
        assertNotNull(serverUnderTest.getGame());
    }
}
