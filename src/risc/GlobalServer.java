package risc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Global server responsible for:
 * 1) Account management (login/registration)
 * 2) Maintaining and managing multiple RiscServers (multiple game rooms)
 */
public class GlobalServer {
    private final int port;
    // Used to store users' account credentials (using a Map for demonstration; in a real project, a database could be used)
    private final Map<String, String> userCredentials;
    // Online player account information (more complex session management can be implemented as needed)
    private final Map<String, PlayerAccount> onlineUsers;

    // All game rooms: gameID -> RiscServer
    private final Map<String, RiscServer> games;

    public GlobalServer(int port) {
        this.port = port;
        this.userCredentials = new HashMap<>();
        this.onlineUsers = new HashMap<>();
        this.games = new HashMap<>();

        // For demonstration, add a sample account here
        userCredentials.put("test", "123");
    }

    public static void main(String[] args) {
        int port = 12345;   // or obtain from args[]
        GlobalServer gs = new GlobalServer(port);
        gs.start();
    }

    public void start() {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("GlobalServer started, listening on port " + port);
            while (true) {
                Socket clientSocket = ss.accept();
                // For each new client, start a thread to handle login/registration and game selection or creation
                Thread t = new Thread(() -> handleClient(clientSocket));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a client's process: login -> select a room -> hand off to the corresponding RiscServer.
     */
    private void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Welcome to the Global RISC Server.");

            // Execute the login/registration process
            PlayerAccount account = doLoginOrRegister(in, out);
            if (account == null) {
                out.println("Login/Registration failed, closing...");
                socket.close();
                return;
            }

            // Choose or create a game
            String gameID = selectOrCreateGame(in, out);
            if (gameID == null) {
                out.println("No valid game selected, closing...");
                socket.close();
                return;
            }

            RiscServer server = games.get(gameID);
            if (server == null) {
                out.println("Selected game not found, closing...");
                socket.close();
                return;
            }

            // Pass the open socket to RiscServer for further handling
            server.addNewClient(socket, account);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Example login/registration process.
     */
    private PlayerAccount doLoginOrRegister(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Please enter 'L' to login, 'R' to register:");
        while (true) {
            String line = in.readLine();
            if (line == null) return null;
            line = line.trim().toUpperCase();
            if (line.startsWith("L")) {
                out.println("Enter username:");
                String user = in.readLine();
                out.println("Enter password:");
                String pass = in.readLine();
                if (userCredentials.containsKey(user) && userCredentials.get(user).equals(pass)) {
                    // Login successful
                    PlayerAccount account = new PlayerAccount(user);
                    onlineUsers.put(user, account);
                    return account;
                } else {
                    out.println("Invalid credential. Try again (L/R).");
                }
            } else if (line.startsWith("R")) {
                out.println("Choose a new username:");
                String newUser = in.readLine();
                if (userCredentials.containsKey(newUser)) {
                    out.println("User already exists, pick another. (L/R?)");
                    continue;
                }
                out.println("Choose a password:");
                String newPass = in.readLine();
                // Registration
                userCredentials.put(newUser, newPass);
                out.println("Registered successfully as " + newUser);
                PlayerAccount account = new PlayerAccount(newUser);
                onlineUsers.put(newUser, account);
                return account;
            } else {
                out.println("Please press 'L' or 'R' only.");
            }
        }
    }

    /**
     * Allows the player to choose between joining an existing game or creating a new one.
     */
    private String selectOrCreateGame(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Existing games: " + games.keySet());
        out.println("Use: 'join <gameID>' or 'new <numPlayers>' to create a new game");
        while (true) {
            String line = in.readLine();
            if (line == null) return null;
            line = line.trim();
            if (line.startsWith("join")) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2 && games.containsKey(parts[1])) {
                    return parts[1];
                } else {
                    out.println("Game not found. Try again.");
                }
            } else if (line.startsWith("new")) {
                // new 3
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    try {
                        int desiredPlayers = Integer.parseInt(parts[1]);
                        // Create a new game
                        String newID = UUID.randomUUID().toString().substring(0, 8);
                        RiscServer rs = new RiscServer(desiredPlayers, newID);
                        games.put(newID, rs);
                        // Start the main logic of the RiscServer (such as waiting for players to join, starting the game, etc.)
                        rs.startServerLogic();
                        out.println("New game created. ID=" + newID);
                        return newID;
                    } catch (NumberFormatException ex) {
                        out.println("Invalid numPlayers. Try again.");
                    }
                } else {
                    out.println("Usage: new <numPlayers>");
                }
            } else {
                out.println("Invalid input. Use 'join <id>' or 'new <num>'.");
            }
        }
    }
}
