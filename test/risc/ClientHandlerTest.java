package risc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Enhanced test class for ClientHandler with improved coverage.
 * This version doesn't use Mockito.
 */
class ClientHandlerTest {

    private Socket mockSocket;
    private ByteArrayOutputStream testOut;
    private ByteArrayInputStream testIn;
    private ClientHandler clientHandler;
    private TestRiscServer mockServer;
    private TestGame mockGame;
    private PlayerAccount playerAccount;
    private StringWriter serverOutput;
    private PrintWriter serverWriter;

    @BeforeEach
    void setUp() throws Exception {
        // Set up mock server and game
        mockServer = new TestRiscServer(2, "testGame");
        mockGame = new TestGame();
        mockServer.setGame(mockGame);

        // Set up player account
        playerAccount = new PlayerAccount("testUser");

        // Set up socket I/O
        testOut = new ByteArrayOutputStream();
        serverOutput = new StringWriter();
        serverWriter = new PrintWriter(serverOutput);

        // Initial command is empty for now
        testIn = new ByteArrayInputStream("".getBytes());

        // Create mock socket
        mockSocket = new TestSocket(testIn, testOut);

        // Create client handler
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(0, clientHandler.getPlayerID());
        assertEquals(playerAccount, clientHandler.getAccount());
    }

    @Test
    void testRun() throws IOException {
        // Set up new input for run method
        ByteArrayInputStream runInput = new ByteArrayInputStream("".getBytes());
        ((TestSocket)mockSocket).setInputStream(runInput);

        // Run the client handler thread
        clientHandler.run();

        // Verify welcome message
        String output = testOut.toString();
        assertTrue(output.contains("Welcome, testUser!"));
    }

    @Test
    void testSendMessage() throws IOException {
        // Override the output stream
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);

        // Run to initialize PrintWriter
        clientHandler.run();

        // Send a message
        clientHandler.sendMessage("Test message");

        // Verify message was sent
        String output = newOut.toString();
        assertTrue(output.contains("Test message"));
    }

    @Test
    void testCloseConnection() throws IOException {
        clientHandler.closeConnection();

        // Verify socket was closed
        assertTrue(((TestSocket)mockSocket).isClosed);
    }

    @Test
    void testCollectOrders_MoveOrder() throws IOException {
        // Set up mock game with a player
        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Set up input to simulate move command
        String simulatedInput = "M\nsourceTerritory destinationTerritory 0 5\nD\n";
        ByteArrayInputStream moveInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(moveInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectOrders output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectOrders
        clientHandler.collectOrders(mockGame);

        // Verify move order was added
        assertTrue(mockGame.hasOrder(MoveOrder.class));

        // Verify output to client
        String output = newOut.toString();
        assertTrue(output.contains("Move order added"));
    }

    @Test
    void testCollectOrders_AttackOrder() throws IOException {
        // Set up mock game with a player
        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Set up input to simulate attack command
        String simulatedInput = "A\nsourceTerritory targetTerritory 1 5\nD\n";
        ByteArrayInputStream attackInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(attackInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectOrders output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectOrders
        clientHandler.collectOrders(mockGame);

        // Verify attack order was added
        assertTrue(mockGame.hasOrder(AttackOrder.class));

        // Verify output to client
        String output = newOut.toString();
        assertTrue(output.contains("Attack order added"));
    }

    @Test
    void testCollectOrders_UpgradeOrder() throws IOException {
        // Set up mock game with a player
        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Set up input to simulate upgrade command
        String simulatedInput = "U\nterritoryName 0 1 3\nD\n";
        ByteArrayInputStream upgradeInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(upgradeInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectOrders output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectOrders
        clientHandler.collectOrders(mockGame);

        // Verify upgrade order was added
        assertTrue(mockGame.hasOrder(UpgradeUnitOrder.class));

        // Verify output to client
        String output = newOut.toString();
        assertTrue(output.contains("Upgrade order added"));
    }

    @Test
    void testCollectOrders_TechUpgradeOrder() throws IOException {
        // Set up mock game with a player
        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Set up input to simulate tech upgrade command
        String simulatedInput = "T\nD\n";
        ByteArrayInputStream techInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(techInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectOrders output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectOrders
        clientHandler.collectOrders(mockGame);

        // Verify tech upgrade order was added
        assertTrue(mockGame.hasOrder(TechUpgradeOrder.class));

        // Verify output to client
        String output = newOut.toString();
        assertTrue(output.contains("Tech upgrade order added"));
    }

    @Test
    void testCollectOrders_InvalidCommand() throws IOException {
        // Set up mock game with a player
        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Set up input to simulate invalid command
        String simulatedInput = "X\nD\n";
        ByteArrayInputStream invalidInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(invalidInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectOrders output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectOrders
        clientHandler.collectOrders(mockGame);

        // Verify error message
        String output = newOut.toString();
        assertTrue(output.contains("Invalid command"));
    }

    @Test
    void testCollectOrders_InvalidMoveFormat() throws IOException {
        // Set up mock game with a player
        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Set up input to simulate move with invalid format
        String simulatedInput = "M\nsourceTerritory\nD\n";
        ByteArrayInputStream invalidInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(invalidInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectOrders output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectOrders
        clientHandler.collectOrders(mockGame);

        // Verify error message
        String output = newOut.toString();
        assertTrue(output.contains("Invalid move order format"));
    }

    @Test
    void testCollectOrders_InvalidAttackFormat() throws IOException {
        // Set up mock game with a player
        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Set up input to simulate attack with invalid format
        String simulatedInput = "A\nsourceTerritory\nD\n";
        ByteArrayInputStream invalidInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(invalidInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectOrders output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectOrders
        clientHandler.collectOrders(mockGame);

        // Verify error message
        String output = newOut.toString();
        assertTrue(output.contains("Invalid attack order format"));
    }

    @Test
    void testCollectOrders_InvalidUpgradeFormat() throws IOException {
        // Set up mock game with a player
        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Set up input to simulate upgrade with invalid format
        String simulatedInput = "U\nterritoryName\nD\n";
        ByteArrayInputStream invalidInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(invalidInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectOrders output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectOrders
        clientHandler.collectOrders(mockGame);

        // Verify error message
        String output = newOut.toString();
        assertTrue(output.contains("Invalid format for upgrade order"));
    }

    @Test
    void testCollectOrders_InvalidNumberFormat() throws IOException {
        // Set up mock game with a player
        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Set up input to simulate move with invalid number format
        String simulatedInput = "M\nsourceTerritory destinationTerritory abc 5\nD\n";
        ByteArrayInputStream invalidInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(invalidInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectOrders output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectOrders
        clientHandler.collectOrders(mockGame);

        // Verify error message
        String output = newOut.toString();
        assertTrue(output.contains("Invalid number format for move order"));
    }

    @Test
    void testCollectInitialPlacement() throws IOException {
        // Set up mock game
        mockGame.setInitialUnits(10);

        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Create territories
        Territory t1 = new Territory("A");
        Territory t2 = new Territory("B");
        List<Territory> territories = new ArrayList<>();
        territories.add(t1);
        territories.add(t2);
        mockPlayer.setTerritories(territories);

        // Set up input to simulate initial placement
        String simulatedInput = "6\n";
        ByteArrayInputStream placementInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(placementInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectInitialPlacement output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectInitialPlacement
        clientHandler.collectInitialPlacement(mockGame);

        // Verify units were allocated
        assertEquals(6, t1.getUnitMap().getOrDefault(0, 0));
        assertEquals(4, t2.getUnitMap().getOrDefault(0, 0));

        // Verify output
        String output = newOut.toString();
        assertTrue(output.contains("Initial unit placement completed"));
    }

    @Test
    void testCollectInitialPlacement_InvalidInput() throws IOException {
        // Set up mock game
        mockGame.setInitialUnits(10);

        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Create territories
        Territory t1 = new Territory("A");
        Territory t2 = new Territory("B");
        List<Territory> territories = new ArrayList<>();
        territories.add(t1);
        territories.add(t2);
        mockPlayer.setTerritories(territories);

        // Set up input sequence: invalid input, then valid input
        String simulatedInput = "invalid\n20\n6\n";
        ByteArrayInputStream placementInput = new ByteArrayInputStream(simulatedInput.getBytes());
        ((TestSocket)mockSocket).setInputStream(placementInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectInitialPlacement output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectInitialPlacement
        clientHandler.collectInitialPlacement(mockGame);

        // Verify output contains error messages
        String output = newOut.toString();
        assertTrue(output.contains("Error reading input") ||
                output.contains("Invalid input"));
    }

    @Test
    void testCollectInitialPlacement_SingleTerritory() throws IOException {
        // Set up mock game
        mockGame.setInitialUnits(10);

        TestPlayer mockPlayer = new TestPlayer(0, "TestPlayer");
        mockGame.addTestPlayer(mockPlayer);

        // Create a single territory
        Territory t1 = new Territory("A");
        List<Territory> territories = new ArrayList<>();
        territories.add(t1);
        mockPlayer.setTerritories(territories);

        // No input needed as the single territory gets all units automatically
        ByteArrayInputStream emptyInput = new ByteArrayInputStream("".getBytes());
        ((TestSocket)mockSocket).setInputStream(emptyInput);

        // Initialize PrintWriter
        clientHandler.run();

        // Reset output to capture only collectInitialPlacement output
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        ((TestSocket)mockSocket).setOutputStream(newOut);
        clientHandler = new ClientHandler(mockSocket, mockServer, 0, playerAccount);
        clientHandler.run();

        // Call collectInitialPlacement
        clientHandler.collectInitialPlacement(mockGame);

        // Verify all units were allocated to the single territory
        assertEquals(10, t1.getUnitMap().getOrDefault(0, 0));

        // Verify output
        String output = newOut.toString();
        assertTrue(output.contains("automatically allocated"));
        assertTrue(output.contains("Initial unit placement completed"));
    }

    // Test utility classes

    /**
     * Mock socket implementation that doesn't throw exceptions
     */
    private class TestSocket extends Socket {
        private InputStream inputStream;
        private OutputStream outputStream;
        private boolean isClosed = false;

        public TestSocket(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public void close() {
            isClosed = true;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void setOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }
    }

    /**
     * Test implementation of RiscServer
     */
    private class TestRiscServer extends RiscServer {
        private Game game;

        public TestRiscServer(int desiredPlayers, String gameID) {
            super(desiredPlayers, gameID);
            this.game = new Game();
        }

        @Override
        public Game getGame() {
            return game;
        }

        public void setGame(Game game) {
            this.game = game;
        }
    }

    /**
     * Test implementation of Game
     */
    private class TestGame extends Game {
        private int initialUnits = 10;
        private List<Player> players = new ArrayList<>();
        private List<Order> orders = new ArrayList<>();

        @Override
        public int getInitialUnits() {
            return initialUnits;
        }

        public void setInitialUnits(int units) {
            this.initialUnits = units;
        }

        @Override
        public Player getPlayer(int id) {
            for (Player p : players) {
                if (p.getId() == id) {
                    return p;
                }
            }
            return null;
        }

        public void addTestPlayer(Player player) {
            players.add(player);
        }

        @Override
        public void addOrder(Order order) {
            orders.add(order);
        }

        public boolean hasOrder(Class<?> orderType) {
            for (Order o : orders) {
                if (orderType.isInstance(o)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Test implementation of Player
     */
    private class TestPlayer extends Player {
        private List<Territory> territories = new ArrayList<>();

        public TestPlayer(int id, String name) {
            super(id, name);
        }

        @Override
        public List<Territory> getTerritories() {
            return territories;
        }

        public void setTerritories(List<Territory> territories) {
            this.territories = territories;
        }
    }
}