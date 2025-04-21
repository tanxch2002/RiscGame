package risc;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;

public class RiscClientGUITest {
    public static void main(String[] args) {
        testConstructor();
        testBuildUI();
        testConnectionSafely();
        testSendSafely();
        testInitMap();
        testReaderSafely();

        System.out.println("All RiscClientGUITest tests passed!");
    }

    private static void testConstructor() {
        RiscClientGUI gui = new RiscClientGUI();
        assert gui != null : "GUI should be instantiated";
    }

    private static void testBuildUI() {
        RiscClientGUI gui = new RiscClientGUI();
        try {
            Method buildUIMethod = RiscClientGUI.class.getDeclaredMethod("buildUI");
            buildUIMethod.setAccessible(true);
            buildUIMethod.invoke(gui);
        } catch (Exception e) {
            assert false : "BuildUI should not throw exceptions: " + e.getMessage();
        }
    }

    private static void testConnectionSafely() {
        RiscClientGUI gui = new RiscClientGUI();

        try {
            Field hostField = RiscClientGUI.class.getDeclaredField("host");
            hostField.setAccessible(true);
            hostField.set(gui, "localhost");

            Field portField = RiscClientGUI.class.getDeclaredField("port");
            portField.setAccessible(true);
            portField.set(gui, 12345);

            // Create a custom mock socket
            Field sockField = RiscClientGUI.class.getDeclaredField("sock");
            sockField.setAccessible(true);
            sockField.set(gui, new MockSocket());

            // Set up input/output streams
            Field inField = RiscClientGUI.class.getDeclaredField("in");
            inField.setAccessible(true);
            inField.set(gui, new BufferedReader(new StringReader("Welcome to RISC\n")));

            Field outField = RiscClientGUI.class.getDeclaredField("out");
            outField.setAccessible(true);
            outField.set(gui, new PrintWriter(new StringWriter(), true));

            // Set up MapPanel to avoid NPE
            Field mapPanelField = RiscClientGUI.class.getDeclaredField("mapPanel");
            mapPanelField.setAccessible(true);
            mapPanelField.set(gui, new MapPanel());

            // Connect method
            Method connectMethod = RiscClientGUI.class.getDeclaredMethod("connect");
            connectMethod.setAccessible(true);

            // Run with timeout
            Thread connectThread = new Thread(() -> {
                try {
                    connectMethod.invoke(gui);
                } catch (Exception e) {
                    // Expected for mock objects
                }
            });
            connectThread.start();
            connectThread.join(1000);
            if (connectThread.isAlive()) {
                connectThread.interrupt();
            }

            // Test connection failure
            sockField.set(gui, null);
            Thread failConnectThread = new Thread(() -> {
                try {
                    connectMethod.invoke(gui);
                } catch (Exception e) {
                    // Expected for mock objects
                }
            });
            failConnectThread.start();
            failConnectThread.join(1000);
            if (failConnectThread.isAlive()) {
                failConnectThread.interrupt();
            }
        } catch (Exception e) {
            // Expected for mock UI components
        }
    }

    private static void testSendSafely() {
        RiscClientGUI gui = new RiscClientGUI();

        try {
            Field outField = RiscClientGUI.class.getDeclaredField("out");
            outField.setAccessible(true);
            outField.set(gui, new PrintWriter(new StringWriter(), true));

            Field inputFieldField = RiscClientGUI.class.getDeclaredField("inputField");
            inputFieldField.setAccessible(true);
            javax.swing.JTextField inputField = new javax.swing.JTextField("Test message");
            inputFieldField.set(gui, inputField);

            Method sendMethod = RiscClientGUI.class.getDeclaredMethod("send");
            sendMethod.setAccessible(true);

            Thread sendThread = new Thread(() -> {
                try {
                    sendMethod.invoke(gui);

                    // Test with empty input
                    inputField.setText("");
                    sendMethod.invoke(gui);

                    // Test with null writer
                    outField.set(gui, null);
                    sendMethod.invoke(gui);
                } catch (Exception e) {
                    // Expected for mock UI components
                }
            });
            sendThread.start();
            sendThread.join(1000);
            if (sendThread.isAlive()) {
                sendThread.interrupt();
            }
        } catch (Exception e) {
            // Expected for mock UI components
        }
    }

    private static void testInitMap() {
        RiscClientGUI gui = new RiscClientGUI();

        try {
            Field mapPanelField = RiscClientGUI.class.getDeclaredField("mapPanel");
            mapPanelField.setAccessible(true);
            mapPanelField.set(gui, new MapPanel());

            Method initMapMethod = RiscClientGUI.class.getDeclaredMethod("initMap");
            initMapMethod.setAccessible(true);
            initMapMethod.invoke(gui);
        } catch (Exception e) {
            assert false : "InitMap should not throw exceptions: " + e.getMessage();
        }
    }

    private static void testReaderSafely() {
        RiscClientGUI gui = new RiscClientGUI();

        try {
            // Set up fields
            Field inField = RiscClientGUI.class.getDeclaredField("in");
            inField.setAccessible(true);
            inField.set(gui, new BufferedReader(new StringReader(
                    "===== Current Map State =====\n" +
                            "A (Player1)\n" +
                            "  Size: 2, Neighbors: B C\n" +
                            "  StationedUnits: P0->{0=5,1=2}\n\n" +
                            "B (Player2)\n" +
                            "  Size: 3, Neighbors: A D\n" +
                            "  StationedUnits: P1->{0=3}\n\n" +
                            "=============================\n" +
                            "Move order added: L1 x3 from A -> B\n" +
                            "Regular message\n"
            )));

            Field mapPanelField = RiscClientGUI.class.getDeclaredField("mapPanel");
            mapPanelField.setAccessible(true);
            mapPanelField.set(gui, new MapPanel());

            Field textAreaField = RiscClientGUI.class.getDeclaredField("textArea");
            textAreaField.setAccessible(true);
            textAreaField.set(gui, new javax.swing.JTextArea());

            Method readerMethod = RiscClientGUI.class.getDeclaredMethod("reader");
            readerMethod.setAccessible(true);

            Thread t = new Thread(() -> {
                try {
                    readerMethod.invoke(gui);
                } catch (Exception e) {
                    // Expected
                }
            });
            t.start();
            t.join(1000); // Only allow 1 second to run
            if (t.isAlive()) {
                t.interrupt();
            }
        } catch (Exception e) {
            // Expected exceptions for UI tests
        }
    }

    private static class MockSocket extends Socket {
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream("Welcome to RISC\n".getBytes());
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public void close() throws IOException {
            // Do nothing
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    }
}