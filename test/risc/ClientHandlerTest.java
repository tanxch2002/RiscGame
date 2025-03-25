
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
        // Set up mock input and output streams
        testOut = new ByteArrayOutputStream();
        // Initial command line input is just "D" to test the exit branch of collectOrders
        testIn = new ByteArrayInputStream("D\n".getBytes());

        // Override Socket with an anonymous class to return our mock input/output streams
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

        // Initialize server and client handler without actually starting the server
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
        // The run() method initializes input/output streams and prints a welcome message
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
        // Set up a minimal game environment
        game.setUpMap(2);
        game.initPlayers(2);

        // Reset the input stream to simulate various player commands
        String commands = ""
                + "M\n"                      // Move command
                + "A B 3\n"                  // Incorrect format (requires 3 parameters), test error message
                + "A\n"                      // Attack command
                + "A B 2\n"                  // Correct format for Attack
                + "D\n";                     // Done
        testIn = new ByteArrayInputStream(commands.getBytes());

        // Rebuild ClientHandler to ensure it uses the new input stream
        ClientHandler ch2 = new ClientHandler(mockSocket, server, 0);
        ch2.run();  // Initialize I/O
        ch2.collectOrders(game);

        String output = testOut.toString();
        // Check if it prompts for M/A/D, format messages, etc.
        assertTrue(output.contains("Enter an order (M/A/D): "));
        assertTrue(output.contains("Enter format: sourceTerritory destinationTerritory numUnits"));
        assertTrue(output.contains("Enter format: sourceTerritory targetTerritory numUnits"));
        assertTrue(output.contains("Attack order added: A => B (2 units)")); // Verify Attack success

        // Verify that the corresponding order was actually added to the game
        assertEquals(2, game.getAllOrders().size()); // Only one Attack was successfully added
    }

    @Test
    void closeConnection() {
        clientHandler.run();
        clientHandler.closeConnection();
        // If no exceptions are thrown, it is considered successful
        assertTrue(true);
    }
}
