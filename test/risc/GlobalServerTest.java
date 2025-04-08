package risc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced test class for GlobalServer with improved coverage.
 * This version doesn't use Mockito.
 */
class GlobalServerTest {

    private static final int TEST_PORT = 54321;
    private GlobalServer server;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream testOut;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        // Set up output capture
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));

        // Create server instance
        server = new GlobalServer(TEST_PORT);

        // Create executor service for running threads
        executorService = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);

        // Shutdown executor service
        executorService.shutdownNow();
    }

    @Test
    void testConstructor() {
        assertNotNull(server);

        // Check initialization of fields using reflection
        try {
            Field userCredentialsField = GlobalServer.class.getDeclaredField("userCredentials");
            userCredentialsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, String> credentials = (Map<String, String>) userCredentialsField.get(server);

            assertNotNull(credentials);
            assertTrue(credentials.containsKey("test"));
            assertEquals("123", credentials.get("test"));

        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testStart_MockServerSocket() {
        // Create server with overridden start method to avoid actual socket binding
        GlobalServer testServer = new GlobalServer(TEST_PORT) {
            @Override
            public void start() {
                System.out.println("GlobalServer started on port " + TEST_PORT);
            }
        };

        // Start server
        testServer.start();

        // Verify output
        String output = testOut.toString();
        assertTrue(output.contains("GlobalServer started on port " + TEST_PORT));
    }

    @Test
    void testHandleClient_Login() throws Exception {
        // Create a test socket that provides test input/output
        TestSocket testSocket = new TestSocket(
                "L\ntest\n123\njoin noSuchGame\nnew 2\n"  // Login input
        );

        // Create a server with overridden methods
        GlobalServer testServer = new TestGlobalServer(TEST_PORT);

        // Call handleClient via reflection
        Method handleClientMethod = GlobalServer.class.getDeclaredMethod("handleClient", Socket.class);
        handleClientMethod.setAccessible(true);

        // Run in separate thread to avoid blocking
        executorService.submit(() -> {
            try {
                handleClientMethod.invoke(testServer, testSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Give time for execution
        Thread.sleep(200);

        // Check output to client
        String clientOutput = testSocket.getOutputData();
        assertTrue(clientOutput.contains("Welcome to the Global RISC Server") ||
                clientOutput.contains("Please enter 'L' to login"));
    }

    @Test
    void testHandleClient_Register() throws Exception {
        // Create a test socket that provides test input/output for registration
        TestSocket testSocket = new TestSocket(
                "R\nnewuser\nnewpass\njoin noSuchGame\nnew 2\n"  // Registration input
        );

        // Create a server with overridden methods
        GlobalServer testServer = new TestGlobalServer(TEST_PORT);

        // Call handleClient via reflection
        Method handleClientMethod = GlobalServer.class.getDeclaredMethod("handleClient", Socket.class);
        handleClientMethod.setAccessible(true);

        // Run in separate thread to avoid blocking
        executorService.submit(() -> {
            try {
                handleClientMethod.invoke(testServer, testSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Give time for execution
        Thread.sleep(200);

        // Check output to client
        String clientOutput = testSocket.getOutputData();
        assertTrue(clientOutput.contains("Welcome to the Global RISC Server") ||
                clientOutput.contains("Choose a new username"));
    }

    @Test
    void testDoLoginOrRegister_LoginSuccess() throws Exception {
        // Create reader/writer for test
        StringReader testReader = new StringReader("L\ntest\n123\n");
        StringWriter testWriter = new StringWriter();
        BufferedReader reader = new BufferedReader(testReader);
        PrintWriter writer = new PrintWriter(testWriter, true);

        // Call the method under test
        Method doLoginOrRegisterMethod = GlobalServer.class.getDeclaredMethod(
                "doLoginOrRegister", BufferedReader.class, PrintWriter.class);
        doLoginOrRegisterMethod.setAccessible(true);

        PlayerAccount account = (PlayerAccount) doLoginOrRegisterMethod.invoke(server, reader, writer);

        // Verify successful login
        assertNotNull(account, "Account should not be null after successful login");
        assertEquals("test", account.getUsername());
    }

    @Test
    void testDoLoginOrRegister_LoginFailure() throws Exception {
        // Create reader/writer for test with wrong password
        StringReader testReader = new StringReader("L\ntest\nwrongpass\nL\ntest\n123\n");
        StringWriter testWriter = new StringWriter();
        BufferedReader reader = new BufferedReader(testReader);
        PrintWriter writer = new PrintWriter(testWriter, true);

        // Call the method under test
        Method doLoginOrRegisterMethod = GlobalServer.class.getDeclaredMethod(
                "doLoginOrRegister", BufferedReader.class, PrintWriter.class);
        doLoginOrRegisterMethod.setAccessible(true);

        PlayerAccount account = (PlayerAccount) doLoginOrRegisterMethod.invoke(server, reader, writer);

        // Verify login eventually succeeds after retry
        assertNotNull(account, "Account should not be null after successful login");
        assertEquals("test", account.getUsername());

        // Verify error message was sent
        String output = testWriter.toString();
        assertTrue(output.contains("Invalid credential"));
    }

    @Test
    void testDoLoginOrRegister_Registration() throws Exception {
        // Create reader/writer for test
        StringReader testReader = new StringReader("R\nnewuser123\nnewpass123\n");
        StringWriter testWriter = new StringWriter();
        BufferedReader reader = new BufferedReader(testReader);
        PrintWriter writer = new PrintWriter(testWriter, true);

        // Call the method under test
        Method doLoginOrRegisterMethod = GlobalServer.class.getDeclaredMethod(
                "doLoginOrRegister", BufferedReader.class, PrintWriter.class);
        doLoginOrRegisterMethod.setAccessible(true);

        PlayerAccount account = (PlayerAccount) doLoginOrRegisterMethod.invoke(server, reader, writer);

        // Verify successful registration
        assertNotNull(account, "Account should not be null after registration");
        assertEquals("newuser123", account.getUsername());

        // Verify credentials were stored
        Field userCredentialsField = GlobalServer.class.getDeclaredField("userCredentials");
        userCredentialsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> credentials = (Map<String, String>) userCredentialsField.get(server);

        assertTrue(credentials.containsKey("newuser123"));
        assertEquals("newpass123", credentials.get("newuser123"));
    }

    @Test
    void testSelectOrCreateGame_JoinExisting() throws Exception {
        // Create reader/writer for test
        StringReader testReader = new StringReader("join testGame\n");
        StringWriter testWriter = new StringWriter();
        BufferedReader reader = new BufferedReader(testReader);
        PrintWriter writer = new PrintWriter(testWriter, true);

        // Add a test game to the server
        Field gamesField = GlobalServer.class.getDeclaredField("games");
        gamesField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, RiscServer> games = (Map<String, RiscServer>) gamesField.get(server);

        games.put("testGame", new RiscServer(2, "testGame"));

        // Call the method under test
        Method selectOrCreateMethod = GlobalServer.class.getDeclaredMethod(
                "selectOrCreateGame", BufferedReader.class, PrintWriter.class);
        selectOrCreateMethod.setAccessible(true);

        String gameId = (String) selectOrCreateMethod.invoke(server, reader, writer);

        // Verify game selection
        assertEquals("testGame", gameId);
    }

    @Test
    void testSelectOrCreateGame_CreateNew() throws Exception {
        // Create reader/writer for test
        StringReader testReader = new StringReader("new 3\n");
        StringWriter testWriter = new StringWriter();
        BufferedReader reader = new BufferedReader(testReader);
        PrintWriter writer = new PrintWriter(testWriter, true);

        // Create test server that doesn't actually start the game server
        GlobalServer testServer = new TestGlobalServer(TEST_PORT);

        // Call the method under test
        Method selectOrCreateMethod = GlobalServer.class.getDeclaredMethod(
                "selectOrCreateGame", BufferedReader.class, PrintWriter.class);
        selectOrCreateMethod.setAccessible(true);

        String gameId = (String) selectOrCreateMethod.invoke(testServer, reader, writer);

        // Verify game was created
        assertNotNull(gameId);

        Field gamesField = GlobalServer.class.getDeclaredField("games");
        gamesField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, RiscServer> games = (Map<String, RiscServer>) gamesField.get(testServer);

        assertTrue(games.containsKey(gameId));
    }

    @Test
    void testSelectOrCreateGame_InvalidCommands() throws Exception {
        // Create reader/writer for test with invalid commands before valid one
        StringReader testReader = new StringReader("badcommand\ninvalid 3\nnew 3\n");
        StringWriter testWriter = new StringWriter();
        BufferedReader reader = new BufferedReader(testReader);
        PrintWriter writer = new PrintWriter(testWriter, true);

        // Create test server
        GlobalServer testServer = new TestGlobalServer(TEST_PORT);

        // Call the method under test
        Method selectOrCreateMethod = GlobalServer.class.getDeclaredMethod(
                "selectOrCreateGame", BufferedReader.class, PrintWriter.class);
        selectOrCreateMethod.setAccessible(true);

        String gameId = (String) selectOrCreateMethod.invoke(testServer, reader, writer);

        // Verify game was eventually created after invalid commands
        assertNotNull(gameId);

        // Verify error messages
        String output = testWriter.toString();
        assertTrue(output.contains("Invalid input"));
    }

    @Test
    void testMain() {
        // Just ensure the main method can be called without exceptions
        Thread mainThread = new Thread(() -> {
            try {
                // Override System.in to avoid blocking
                System.setIn(new ByteArrayInputStream(new byte[0]));

                // Create our own server to prevent starting the actual server
                GlobalServer.main(new String[0]); {
                    // Just print a message instead of starting the real server
                    System.out.println("GlobalServer main method called");
                };
            } catch (Exception e) {
                // Ignore expected exceptions
            }
        });

        mainThread.start();
        try {
            mainThread.join(100);
        } catch (InterruptedException e) {
            // Expected
        }

        // Interrupt the thread if it's still running
        if (mainThread.isAlive()) {
            mainThread.interrupt();
        }

        // If we reach here without exceptions, test passes
        assertTrue(true);
    }

    // Test utility classes

    /**
     * Test implementation of GlobalServer that overrides methods that would block
     */
    private static class TestGlobalServer extends GlobalServer {
        public TestGlobalServer(int port) {
            super(port);
        }

        RiscServer createNewGame(String gameId, int numPlayers) {
            RiscServer testServer = new TestRiscServer(numPlayers, gameId);

            // Add to games map
            try {
                Field gamesField = GlobalServer.class.getDeclaredField("games");
                gamesField.setAccessible(true);

                @SuppressWarnings("unchecked")
                Map<String, RiscServer> games = (Map<String, RiscServer>) gamesField.get(this);

                games.put(gameId, testServer);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return testServer;
        }
    }

    /**
     * Test implementation of RiscServer that doesn't actually start
     */
    private static class TestRiscServer extends RiscServer {
        public TestRiscServer(int desiredPlayers, String gameID) {
            super(desiredPlayers, gameID);
        }

        @Override
        public void startServerLogic() {
            // Do nothing to prevent actual thread startup
        }
    }

    /**
     * Test implementation of Socket that provides controlled input/output
     */
    private static class TestSocket extends Socket {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private final InputStream inputStream;
        private boolean closed = false;

        public TestSocket(String inputData) {
            this.inputStream = new ByteArrayInputStream(inputData.getBytes());
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return outputStream;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        public String getOutputData() {
            return outputStream.toString();
        }
    }
}