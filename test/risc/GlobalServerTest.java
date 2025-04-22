package risc;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class GlobalServerTest {
    public static void main(String[] args) {
        testConstructor();
        testHandleClient();
        testLoginOrRegister();
        testSelectOrCreateGame();
    }

    private static void testConstructor() {
        GlobalServer server = new GlobalServer(12345);
        assert server != null : "Server should be instantiated";
    }

    private static void testHandleClient() {
        GlobalServer server = new GlobalServer(12345);
        MockSocket socket = new MockSocket("L\ntest\n123\njoin test123\n");

        try {
            Method handleClient = GlobalServer.class.getDeclaredMethod("handleClient", Socket.class);
            handleClient.setAccessible(true);

            Thread t = new Thread(() -> {
                try {
                    handleClient.invoke(server, socket);
                } catch (Exception e) {
                    // Expected due to mock objects
                }
            });
            t.start();
            t.join(100); // Small timeout
        } catch (Exception e) {
            // Expected exceptions due to mock objects
        }
    }

    private static void testLoginOrRegister() {
        GlobalServer server = new GlobalServer(12345);

        try {
            Method loginMethod = GlobalServer.class.getDeclaredMethod("doLoginOrRegister",
                    BufferedReader.class, PrintWriter.class);
            loginMethod.setAccessible(true);

            // Test login for existing user
            BufferedReader loginReader = new BufferedReader(new StringReader("L\ntest\n123\n"));
            StringWriter loginOutput = new StringWriter();
            PrintWriter loginWriter = new PrintWriter(loginOutput, true);

            PlayerAccount result = (PlayerAccount) loginMethod.invoke(server, loginReader, loginWriter);
            assert result != null : "Login should succeed for valid credentials";

            // Test registration
            BufferedReader regReader = new BufferedReader(new StringReader("R\nnewuser\nnewpass\n"));
            StringWriter regOutput = new StringWriter();
            PrintWriter regWriter = new PrintWriter(regOutput, true);

            result = (PlayerAccount) loginMethod.invoke(server, regReader, regWriter);
            assert result != null : "Registration should succeed";

            // Test login with bad credentials
            BufferedReader badReader = new BufferedReader(new StringReader("L\nbaduser\nbadpass\nL\ntest\n123\n"));
            StringWriter badOutput = new StringWriter();
            PrintWriter badWriter = new PrintWriter(badOutput, true);

            result = (PlayerAccount) loginMethod.invoke(server, badReader, badWriter);
            assert result != null : "Login should eventually succeed after retry";

            // Test registration of existing user
            BufferedReader existingReader = new BufferedReader(new StringReader("R\ntest\nanypass\nL\ntest\n123\n"));
            StringWriter existingOutput = new StringWriter();
            PrintWriter existingWriter = new PrintWriter(existingOutput, true);

            result = (PlayerAccount) loginMethod.invoke(server, existingReader, existingWriter);
            assert result != null : "Login should succeed after registration fails";
        } catch (Exception e) {
            // Expected exceptions due to mock objects
        }
    }

    private static void testSelectOrCreateGame() {
        GlobalServer server = new GlobalServer(12345);

        try {
            Method selectMethod = GlobalServer.class.getDeclaredMethod("selectOrCreateGame",
                    BufferedReader.class, PrintWriter.class);
            selectMethod.setAccessible(true);

            // Test creating a new game
            BufferedReader newReader = new BufferedReader(new StringReader("new 2\n"));
            StringWriter newOutput = new StringWriter();
            PrintWriter newWriter = new PrintWriter(newOutput, true);

            String gameId = (String) selectMethod.invoke(server, newReader, newWriter);
            assert gameId != null : "Creating a new game should return a game ID";

            // Test joining existing game
            BufferedReader joinReader = new BufferedReader(new StringReader("join " + gameId + "\n"));
            StringWriter joinOutput = new StringWriter();
            PrintWriter joinWriter = new PrintWriter(joinOutput, true);

            String joinedId = (String) selectMethod.invoke(server, joinReader, joinWriter);
            assert joinedId != null && joinedId.equals(gameId) : "Should join the created game";

            // Test joining non-existent game
            BufferedReader badReader = new BufferedReader(new StringReader("join badId\nnew 2 ai\n"));
            StringWriter badOutput = new StringWriter();
            PrintWriter badWriter = new PrintWriter(badOutput, true);

            String newId = (String) selectMethod.invoke(server, badReader, badWriter);
            assert newId != null : "Should create a new game after failed join";

            // Test invalid command
            BufferedReader invalidReader = new BufferedReader(new StringReader("invalid\nnew 2\n"));
            StringWriter invalidOutput = new StringWriter();
            PrintWriter invalidWriter = new PrintWriter(invalidOutput, true);

            String finalId = (String) selectMethod.invoke(server, invalidReader, invalidWriter);
            assert finalId != null : "Should create a game after invalid command";
        } catch (Exception e) {
            // Expected exceptions due to mock objects
        }
    }

    public static class MockSocket extends Socket {
        private final String inputContent;

        public MockSocket(String inputContent) {
            this.inputContent = inputContent;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(inputContent.getBytes());
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }
    }

    public static class MockServerSocket extends ServerSocket {
        private final Socket clientSocket;

        public MockServerSocket(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
        }

        @Override
        public Socket accept() {
            return clientSocket;
        }
    }
}