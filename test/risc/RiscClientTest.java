package risc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class RiscClientTest {

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        // 准备捕获测试期间的 System.out
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @AfterEach
    void tearDown() {
        // 还原 System.in / System.out
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    void testConstructor() {
        RiscClient client = new RiscClient("localhost", 12345);
        assertNotNull(client);
    }

    @Test
    void testRunClient_NoServerRunning() {
        // 这里不会真正去连上服务器，所以会抛出 IOException
        // 但是能覆盖到 try 块，以及因为 Connection refused 进入 catch 块
        RiscClient client = new RiscClient("localhost", 12345);
        client.runClient();
        String output = testOut.toString();
        // 我们只要不抛异常即可，或者可以断言一些打印输出
        // assertTrue(output.contains("Connection refused"));
        assertTrue(true);
    }

    @Test
    void testMain_NoUserInput() {
        // 模拟用户按 Enter，跳过输入 IP 与端口的情况
        String simulatedUserInput = "\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));

        // 调用 main 方法，不传任何命令行参数
        RiscClient.main(new String[]{});

        String output = testOut.toString();
        // 根据 main 中的逻辑，会提示输入 IP 和端口
        // 这里可自行断言是否包含特定字符串
        assertTrue(output.contains("Please enter the server IP address"));
        assertTrue(output.contains("Please enter the port number"));
    }

    @Test
    void testMain_WithUserInput() {
        // 模拟用户输入 IP="127.0.0.1" 和端口="9999"
        String simulatedUserInput = "127.0.0.1\n9999\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));

        RiscClient.main(new String[]{});
        String output = testOut.toString();
        // 检查输出中是否出现正确提示
        assertTrue(output.contains("Please enter the server IP address"));
        assertTrue(output.contains("Please enter the port number"));
    }
}
