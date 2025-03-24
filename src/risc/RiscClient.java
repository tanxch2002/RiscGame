package risc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Simple text-based client that connects to the RISC server
 * and lets the user type orders in the console.
 */
public class RiscClient {
    private final String host;
    private final int port;

    public RiscClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void runClient() {
        try (Socket socket = new Socket(host, port);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {

            // Start a thread to read messages from server and print them
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = serverIn.readLine()) != null) {
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    // connection closed
                }
            });
            readerThread.start();

            // Continuously send user input to server
            String userMsg;
            while ((userMsg = userIn.readLine()) != null) {
                serverOut.println(userMsg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Usage: java risc.RiscClient <hostname> <port>
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }
        RiscClient client = new RiscClient(host, port);
        client.runClient();
    }
}
