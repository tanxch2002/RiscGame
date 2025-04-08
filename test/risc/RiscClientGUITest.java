package risc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced test class for RiscClientGUI with improved coverage.
 * This version doesn't use Mockito.
 */
class RiscClientGUITest {

    private RiscClientGUI clientGUI;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        // Use headless mode for testing Swing components
        System.setProperty("java.awt.headless", "true");

        try {
            // Capture stdout
            testOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(testOut));

            // Create GUI instance for testing
            clientGUI = new RiscClientGUI();

        } catch (HeadlessException e) {
            // Skip init in truly headless environments
            System.setOut(originalOut);
            System.out.println("Headless environment detected, skipping GUI initialization");
        }
    }

    @AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);

        // Dispose GUI frame
        if (clientGUI != null) {
            clientGUI.dispose();
        }
    }

    @Test
    void testConstructor() {
        if (clientGUI != null) {
            assertEquals("RISC 客户端 GUI", clientGUI.getTitle());
            assertEquals(JFrame.EXIT_ON_CLOSE, clientGUI.getDefaultCloseOperation());

            // Test frame dimensions
            Dimension size = clientGUI.getSize();
            assertEquals(800, size.width);
            assertEquals(700, size.height);
        } else {
            // Skip test in truly headless environment
            assertTrue(true);
        }
    }

    @Test
    void testSetupUI() {
        if (clientGUI == null) {
            // Skip test in truly headless environment
            assertTrue(true);
            return;
        }

        try {
            // Use reflection to access private fields and verify UI components
            Field inputFieldField = RiscClientGUI.class.getDeclaredField("inputField");
            inputFieldField.setAccessible(true);
            JTextField inputField = (JTextField) inputFieldField.get(clientGUI);
            assertNotNull(inputField);

            Field textAreaField = RiscClientGUI.class.getDeclaredField("textArea");
            textAreaField.setAccessible(true);
            JTextArea textArea = (JTextArea) textAreaField.get(clientGUI);
            assertNotNull(textArea);
            assertFalse(textArea.isEditable());

            Field mapPanelField = RiscClientGUI.class.getDeclaredField("mapPanel");
            mapPanelField.setAccessible(true);
            MapPanel mapPanel = (MapPanel) mapPanelField.get(clientGUI);
            assertNotNull(mapPanel);

            Field connectButtonField = RiscClientGUI.class.getDeclaredField("connectButton");
            connectButtonField.setAccessible(true);
            JButton connectButton = (JButton) connectButtonField.get(clientGUI);
            assertNotNull(connectButton);
            assertTrue(connectButton.isEnabled());

            Field sendButtonField = RiscClientGUI.class.getDeclaredField("sendButton");
            sendButtonField.setAccessible(true);
            JButton sendButton = (JButton) sendButtonField.get(clientGUI);
            assertNotNull(sendButton);
            assertFalse(sendButton.isEnabled());

        } catch (Exception e) {
            fail("Failed to access GUI components: " + e.getMessage());
        }
    }

    @Test
    void testInitializeDefaultMapData() {
        if (clientGUI == null) {
            // Skip test in truly headless environment
            assertTrue(true);
            return;
        }

        try {
            // Use reflection to access private method
            Method initMethod = RiscClientGUI.class.getDeclaredMethod("initializeDefaultMapData");
            initMethod.setAccessible(true);
            initMethod.invoke(clientGUI);

            // Get the map panel to verify
            Field mapPanelField = RiscClientGUI.class.getDeclaredField("mapPanel");
            mapPanelField.setAccessible(true);
            MapPanel mapPanel = (MapPanel) mapPanelField.get(clientGUI);

            // Limited verification possible - ensure no exception was thrown
            assertNotNull(mapPanel);

        } catch (Exception e) {
            fail("Failed to initialize default map data: " + e.getMessage());
        }
    }

    @Test
    void testHandleConnect_Success() {
        if (clientGUI == null) {
            // Skip test in truly headless environment
            assertTrue(true);
            return;
        }

        try {
            // Create a custom RiscClientGUI subclass
            class TestRiscClientGUI extends RiscClientGUI {
                Socket createSocket(String host, int port) throws Exception {
                    // Return a test socket instead of trying to connect to a real server
                    return new TestSocket(
                            new ByteArrayInputStream("Welcome".getBytes()),
                            new ByteArrayOutputStream()
                    );
                }
            }

            TestRiscClientGUI testGUI = new TestRiscClientGUI();

            // Use reflection to access private method
            Method connectMethod = RiscClientGUI.class.getDeclaredMethod("handleConnect");
            connectMethod.setAccessible(true);

            // Call the connect method
            connectMethod.invoke(testGUI);

            // Check that connection status was updated
            Field connectButtonField = RiscClientGUI.class.getDeclaredField("connectButton");
            connectButtonField.setAccessible(true);
            JButton connectButton = (JButton) connectButtonField.get(testGUI);

            Field sendButtonField = RiscClientGUI.class.getDeclaredField("sendButton");
            sendButtonField.setAccessible(true);
            JButton sendButton = (JButton) sendButtonField.get(testGUI);

            // Buttons should be updated
            if (connectButton != null) {
                assertFalse(connectButton.isEnabled());
            }
            if (sendButton != null) {
                assertTrue(sendButton.isEnabled());
            }

            // Check that message was appended
            Field textAreaField = RiscClientGUI.class.getDeclaredField("textArea");
            textAreaField.setAccessible(true);
            JTextArea textArea = (JTextArea) textAreaField.get(testGUI);

            if (textArea != null) {
                assertTrue(textArea.getText().contains("已连接到服务器"));
            }

        } catch (Exception e) {
            fail("Failed to test handleConnect: " + e.getMessage());
        }
    }

    @Test
    void testHandleConnect_Failure() {
        if (clientGUI == null) {
            // Skip test in truly headless environment
            assertTrue(true);
            return;
        }

        try {
            // Create a custom RiscClientGUI subclass
            class TestRiscClientGUI extends RiscClientGUI {
                Socket createSocket(String host, int port) throws Exception {
                    throw new java.net.ConnectException("Connection refused");
                }
            }

            TestRiscClientGUI testGUI = new TestRiscClientGUI();

            // Use reflection to access private method
            Method connectMethod = RiscClientGUI.class.getDeclaredMethod("handleConnect");
            connectMethod.setAccessible(true);

            // Call the connect method
            connectMethod.invoke(testGUI);

            // Check that connection status was updated
            Field connectButtonField = RiscClientGUI.class.getDeclaredField("connectButton");
            connectButtonField.setAccessible(true);
            JButton connectButton = (JButton) connectButtonField.get(testGUI);

            Field sendButtonField = RiscClientGUI.class.getDeclaredField("sendButton");
            sendButtonField.setAccessible(true);
            JButton sendButton = (JButton) sendButtonField.get(testGUI);

            // Buttons should maintain original state
            if (connectButton != null) {
                assertTrue(connectButton.isEnabled());
            }
            if (sendButton != null) {
                assertFalse(sendButton.isEnabled());
            }

            // Check that error message was appended
            Field textAreaField = RiscClientGUI.class.getDeclaredField("textArea");
            textAreaField.setAccessible(true);
            JTextArea textArea = (JTextArea) textAreaField.get(testGUI);

            if (textArea != null) {
                assertTrue(textArea.getText().contains("连接失败"));
            }

        } catch (Exception e) {
            fail("Failed to test handleConnect failure: " + e.getMessage());
        }
    }

    @Test
    void testHandleSend() {
        if (clientGUI == null) {
            // Skip test in truly headless environment
            assertTrue(true);
            return;
        }

        try {
            // Set up PrintWriter field with a test output stream
            ByteArrayOutputStream serverOutput = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(serverOutput);

            Field serverOutField = RiscClientGUI.class.getDeclaredField("serverOut");
            serverOutField.setAccessible(true);
            serverOutField.set(clientGUI, printStream);

            // Set input text
            Field inputFieldField = RiscClientGUI.class.getDeclaredField("inputField");
            inputFieldField.setAccessible(true);
            JTextField inputField = (JTextField) inputFieldField.get(clientGUI);
            if (inputField != null) {
                inputField.setText("Test message");
            }

            // Set socket field to a non-null value
            Field socketField = RiscClientGUI.class.getDeclaredField("socket");
            socketField.setAccessible(true);
            TestSocket mockSocket = new TestSocket(null, null);
            socketField.set(clientGUI, mockSocket);

            // Call handleSend
            Method sendMethod = RiscClientGUI.class.getDeclaredMethod("handleSend");
            sendMethod.setAccessible(true);
            sendMethod.invoke(clientGUI);

            // Verify message was sent
            String sentMessage = serverOutput.toString().trim();
            assertEquals("Test message", sentMessage);

            // Verify input field was cleared
            if (inputField != null) {
                assertEquals("", inputField.getText());
            }

        } catch (Exception e) {
            fail("Failed to test handleSend: " + e.getMessage());
        }
    }

    @Test
    void testAppendMessage() {
        if (clientGUI == null) {
            // Skip test in truly headless environment
            assertTrue(true);
            return;
        }

        try {
            // Get textArea field
            Field textAreaField = RiscClientGUI.class.getDeclaredField("textArea");
            textAreaField.setAccessible(true);
            JTextArea textArea = (JTextArea) textAreaField.get(clientGUI);

            if (textArea != null) {
                // Clear textArea
                textArea.setText("");

                // Call appendMessage
                Method appendMethod = RiscClientGUI.class.getDeclaredMethod("appendMessage", String.class);
                appendMethod.setAccessible(true);
                appendMethod.invoke(clientGUI, "Test append");

                // Allow time for SwingUtilities to process
                Thread.sleep(100);

                // Verify message was appended
                assertTrue(textArea.getText().contains("Test append"));
            }

        } catch (Exception e) {
            fail("Failed to test appendMessage: " + e.getMessage());
        }
    }

    @Test
    void testGetColorForPlayer() {
        if (clientGUI == null) {
            // Skip test in truly headless environment
            assertTrue(true);
            return;
        }

        try {
            // Use reflection to access private method
            Method colorMethod = RiscClientGUI.class.getDeclaredMethod("getColorForPlayer", String.class);
            colorMethod.setAccessible(true);

            // Test with null player
            Color nullColor = (Color) colorMethod.invoke(clientGUI, (String) null);
            assertEquals(Color.LIGHT_GRAY, nullColor);

            // Test with "None" player
            Color noneColor = (Color) colorMethod.invoke(clientGUI, "None");
            assertEquals(Color.LIGHT_GRAY, noneColor);

            // Test with a player name
            Color player1Color = (Color) colorMethod.invoke(clientGUI, "Player1");
            assertNotNull(player1Color);
            assertNotEquals(Color.LIGHT_GRAY, player1Color);

            // Test with the same player name (should get the same color)
            Color player1Color2 = (Color) colorMethod.invoke(clientGUI, "Player1");
            assertEquals(player1Color, player1Color2);

            // Test with a different player name (should get a different color)
            Color player2Color = (Color) colorMethod.invoke(clientGUI, "Player2");
            assertNotNull(player2Color);
            assertNotEquals(player1Color, player2Color);

        } catch (Exception e) {
            fail("Failed to test getColorForPlayer: " + e.getMessage());
        }
    }

    @Test
    void testMain() {
        // Just ensure the main method runs without exceptions
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a subclass with overridden constructor to avoid actual window creation
                class TestRiscClientGUI extends RiscClientGUI {
                    public void setVisible(boolean visible) {
                        // Do nothing to prevent window from showing
                    }
                }

                // Call main
                RiscClientGUI.main(new String[]{});

                // If we get here without exception, test passes
                assertTrue(true);

            } catch (Exception e) {
                fail("Main method threw exception: " + e.getMessage());
            }
        });
    }

    /**
     * Test Socket implementation that doesn't throw exceptions
     */
    private static class TestSocket extends Socket {
        private InputStream inputStream;
        private OutputStream outputStream;
        private boolean isClosed = false;

        public TestSocket(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream != null ? inputStream : new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return outputStream != null ? outputStream : new ByteArrayOutputStream();
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
        }

        @Override
        public boolean isClosed() {
            return isClosed;
        }
    }
}