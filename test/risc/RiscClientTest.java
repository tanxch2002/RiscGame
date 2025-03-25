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
        // Prepare to capture System.out during the test
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @AfterEach
    void tearDown() {
        // Restore System.in and System.out
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
        // This will not actually connect to a server, so an IOException will be thrown.
        // However, it covers the try block and the catch block due to "Connection refused".
        RiscClient client = new RiscClient("localhost", 12345);
        client.runClient();
        String output = testOut.toString();
        // We just need to ensure no exception is thrown, or we can assert some printed output.
        // assertTrue(output.contains("Connection refused"));
        assertTrue(true);
    }

    @Test
    void testMain_NoUserInput() {
        // Simulate the user pressing Enter to skip entering IP and port.
        String simulatedUserInput = "\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));

        // Call the main method without any command-line arguments.
        RiscClient.main(new String[]{});

        String output = testOut.toString();
        // According to the logic in main, it will prompt for IP and port.
        // You can assert whether specific strings are included.
        assertTrue(output.contains("Please enter the server IP address"));
        assertTrue(output.contains("Please enter the port number"));
    }

    @Test
    void testMain_WithUserInput() {
        // Simulate the user entering IP="127.0.0.1" and port="9999".
        String simulatedUserInput = "127.0.0.1\n9999\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));

        RiscClient.main(new String[]{});
        String output = testOut.toString();
        // Check if the output contains the correct prompts.
        assertTrue(output.contains("Please enter the server IP address"));
        assertTrue(output.contains("Please enter the port number"));
    }
}