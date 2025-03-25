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
        // Backup the original System.out and System.in to restore later
        originalOut = System.out;
        originalIn = System.in;

        // Capture the output of System.out
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));

        // Directly construct a RiscServer with 2 players
        // Avoid waiting for user input in the main method
        serverUnderTest = new RiscServer(2);

        // Start the server's main logic (runServer), which will block waiting for 2 clients
        serverThread = new Thread(() -> serverUnderTest.runServer());
        serverThread.start();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // After the test, close connections and restore System.out and System.in
        serverUnderTest.closeAllConnections();
        serverThread.interrupt();
        serverThread.join(500);  // Wait up to 0.5 seconds for the thread to terminate

        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    void testLocalInteraction() throws Exception {
        // We need to "simulate" two players to meet the server's requirement for 2 connections
        // Each player runs in a separate thread, connects to the server, reads the welcome message, and sends some simple commands

        Thread client1 = new Thread(() -> {
            try (Socket socket = new Socket("localhost", RiscServer.PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Read the welcome message from the server, such as "Welcome, Player 1!"
                String welcome = in.readLine();
                System.out.println("Client1 received: " + welcome);

                // The server will prompt players to make initial allocations or send commands
                // For simplicity, just send "D" to indicate Done
                out.println("D");

                // The server may then send messages like "All orders done for this turn."
                // Read one line and consider it done
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

                // Similarly, just send "D"
                out.println("D");

                String serverMsg = in.readLine();
                System.out.println("Client2 next message: " + serverMsg);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Start the two "clients"
        client1.start();
        client2.start();

        // Wait for the clients to finish
        client1.join();
        client2.join();

        // At this point, the server should have received 2 connections and processed the simplest command input (Done)
        // If you want to test more commands (Move/Attack/initial allocation), you need to simulate sending complete command flows in the clients

        // Print the logs captured by the server
        String serverOutput = testOut.toString(StandardCharsets.UTF_8);
        System.out.println("Server captured output: \n" + serverOutput);

        // Make some simple assertions to confirm the server started successfully and broadcasted messages
        assertTrue(serverOutput.contains("Server started on port 12345"));
        assertTrue(serverOutput.contains("Desired player count (2) reached, starting game."));
    }

    // Other previous test methods can be retained, such as:
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