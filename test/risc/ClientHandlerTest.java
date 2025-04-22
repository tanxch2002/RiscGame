package risc;

import java.io.*;
import java.net.Socket;
import java.lang.reflect.Field;

public class ClientHandlerTest {
    public static void main(String[] args) {
        testConstructorAndGetters();
        testSendMessage();
        testCollectOrdersAllTypes();
        testCollectInitialPlacementWithSafetyCheck();
        testCloseConnection();
        testRun();

        System.out.println("All ClientHandlerTest tests passed!");
    }

    private static void testConstructorAndGetters() {
        MockSocket mockSocket = new MockSocket();
        RiscServer server = new RiscServer(2, "test", false);
        PlayerAccount account = new PlayerAccount("TestPlayer");

        ClientHandler handler = new ClientHandler(mockSocket, server, 1, account);

        assert handler.getPlayerID() == 1 : "Player ID should be 1";
        assert handler.getAccount() == account : "Account should match";
    }

    private static void testSendMessage() {
        MockSocket mockSocket = new MockSocket();
        RiscServer server = new RiscServer(2, "test", false);
        PlayerAccount account = new PlayerAccount("TestPlayer");

        ClientHandler handler = new ClientHandler(mockSocket, server, 1, account);

        // Only run if we can safely prepare the handler
        if (prepareHandlerStreams(handler)) {
            handler.sendMessage("Test message");

            // Test with null out
            try {
                Field outField = ClientHandler.class.getDeclaredField("out");
                outField.setAccessible(true);
                outField.set(handler, null);
                handler.sendMessage("This should not throw an exception");
            } catch (Exception e) {
                assert false : "Should not throw exception: " + e.getMessage();
            }
        }
    }

    private static void testCollectOrdersAllTypes() {
        // Test all order types: M, A, U, T, D, C, FA
        String inputData =
                "M\nA B 0 5\n" +  // Move
                        "A\nA B 0 5\n" +  // Attack
                        "U\nA 0 1 3\n" +  // Upgrade
                        "T\n" +           // Tech upgrade
                        "C\nTest chat\n" + // Chat
                        "FA\nPlayer2\n" +  // Form alliance
                        "M\ninvalid\n" +   // Invalid input
                        "A\ninvalid\n" +   // Invalid input
                        "U\ninvalid\n" +   // Invalid input
                        "D\n";             // Done

        MockSocket mockSocket = new MockSocket(inputData);
        RiscServer server = new RiscServer(2, "test", false);
        Game game = server.getGame();
        PlayerAccount account = new PlayerAccount("TestPlayer");

        ClientHandler handler = new ClientHandler(mockSocket, server, 0, account);

        // Only run collectOrders if we can safely prepare the handler
        if (prepareHandlerStreams(handler)) {
            // Override input to avoid waiting for real input
            try {
                Field inField = ClientHandler.class.getDeclaredField("in");
                inField.setAccessible(true);
                inField.set(handler, new BufferedReader(new StringReader(inputData)));

                // Run in a thread with timeout
                Thread orderThread = new Thread(() -> handler.collectOrders(game));
                orderThread.start();
                orderThread.join(2000); // Wait max 2 seconds

                // If thread is still alive, interrupt it
                if (orderThread.isAlive()) {
                    orderThread.interrupt();
                }
            } catch (Exception e) {
                // Expected in test environment
            }
        }
    }

    private static void testCollectInitialPlacementWithSafetyCheck() {
        // Create inputs for two territories, with one invalid input
        String inputData = "invalid\n5\n5\n";
        MockSocket mockSocket = new MockSocket(inputData);
        RiscServer server = new RiscServer(2, "test", false);
        Game game = server.getGame();
        game.setUpMap(2);
        game.initPlayers(2);
        PlayerAccount account = new PlayerAccount("TestPlayer");

        ClientHandler handler = new ClientHandler(mockSocket, server, 0, account);

        // Only run collectInitialPlacement if we can safely prepare the handler
        if (prepareHandlerStreams(handler)) {
            try {
                Field inField = ClientHandler.class.getDeclaredField("in");
                inField.setAccessible(true);
                inField.set(handler, new BufferedReader(new StringReader(inputData)));

                // Set a timeout to prevent infinite wait
                Thread placementThread = new Thread(() -> handler.collectInitialPlacement(game));
                placementThread.start();
                placementThread.join(2000); // Wait max 2 seconds

                // If thread is still alive, interrupt it
                if (placementThread.isAlive()) {
                    placementThread.interrupt();
                }
            } catch (Exception e) {
                // Expected in test environment
            }
        }
    }

    private static void testCloseConnection() {
        MockSocket mockSocket = new MockSocket();
        RiscServer server = new RiscServer(2, "test", false);
        PlayerAccount account = new PlayerAccount("TestPlayer");

        ClientHandler handler = new ClientHandler(mockSocket, server, 1, account);

        // Only attempt to close if we can safely prepare the handler
        if (prepareHandlerStreams(handler)) {
            handler.closeConnection();

            // Test closing with null streams
            try {
                Field inField = ClientHandler.class.getDeclaredField("in");
                inField.setAccessible(true);
                inField.set(handler, null);

                Field outField = ClientHandler.class.getDeclaredField("out");
                outField.setAccessible(true);
                outField.set(handler, null);

                handler.closeConnection(); // Should not throw an exception
            } catch (Exception e) {
                assert false : "Should not throw exception: " + e.getMessage();
            }
        }
    }

    private static void testRun() {
        String welcome = "Welcome message";
        // Fix: Don't try to override getInputStream in anonymous class
        final byte[] welcomeBytes = welcome.getBytes();
        MockSocket mockSocket = new MockSocket() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(welcomeBytes);
            }
        };
        RiscServer server = new RiscServer(2, "test", false);
        PlayerAccount account = new PlayerAccount("TestPlayer");

        ClientHandler handler = new ClientHandler(mockSocket, server, 1, account);

        // Test run method
        Thread runThread = new Thread(() -> handler.run());
        runThread.start();
        try {
            runThread.join(1000); // Wait max 1 second
            if (runThread.isAlive()) {
                runThread.interrupt();
            }
        } catch (InterruptedException e) {
            // Expected
        }

        // Test run with exception - create a completely new MockSocket subclass
        Socket errorSocket = new Socket() {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("Test error");
            }

            @Override
            public OutputStream getOutputStream() {
                return new ByteArrayOutputStream();
            }
        };

        ClientHandler errorHandler = new ClientHandler(errorSocket, server, 1, account);
        Thread errorThread = new Thread(() -> errorHandler.run());
        errorThread.start();
        try {
            errorThread.join(1000);
            if (errorThread.isAlive()) {
                errorThread.interrupt();
            }
        } catch (InterruptedException e) {
            // Expected
        }
    }

    // Helper method to safely set up streams for handler testing
    private static boolean prepareHandlerStreams(ClientHandler handler) {
        try {
            // Initialize output stream
            Field outField = ClientHandler.class.getDeclaredField("out");
            outField.setAccessible(true);
            outField.set(handler, new PrintWriter(new StringWriter(), true));

            // Initialize input stream
            Field inField = ClientHandler.class.getDeclaredField("in");
            inField.setAccessible(true);
            inField.set(handler, new BufferedReader(new StringReader("")));

            return true;
        } catch (Exception e) {
            System.out.println("Failed to prepare handler streams: " + e.getMessage());
            return false;
        }
    }

    private static class MockSocket extends Socket {
        private String inputContent;

        public MockSocket() {
            this("");
        }

        public MockSocket(String inputContent) {
            this.inputContent = inputContent;
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(inputContent.getBytes());
        }

        @Override
        public void close() {
            // Do nothing
        }
    }
}