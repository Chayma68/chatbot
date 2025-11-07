package chat;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    // 1) Port + shared list of client writers (thread-safe)
    private static final int PORT = 5000;
    private final List<PrintWriter> clientWriters = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        new ChatServer().start();
    }

    private void start() {
        System.out.println("[Server] Starting on port " + PORT + " ...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept(); // blocks
                System.out.println("[Server] Client connected from " + socket.getRemoteSocketAddress());
                // 2) Hand off to a handler thread
                Thread t = new Thread(new ClientHandler(socket, clientWriters));
                t.start();
            }
        } catch (IOException e) {
            System.err.println("[Server] Fatal error: " + e.getMessage());
        }
    }

    // 3) One handler per client (we'll fill it in next)
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final List<PrintWriter> allClients;
        private String nick = "(anon)";

        ClientHandler(Socket socket, List<PrintWriter> allClients) {
            this.socket = socket;
            this.allClients = allClients;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // TEMP: register writer so we can broadcast later
                allClients.add(out);

                // Step 1 behavior: just echo first line, then close
                nick = in.readLine(); // later: validate nickname, etc.
                System.out.println("[Server] Nick received: " + nick);
                out.println("* hi " + nick + " (server says hello) *");

            } catch (IOException e) {
                System.err.println("[Server] Client error: " + e.getMessage());
            } finally {
                // Cleanup
                try { socket.close(); } catch (IOException ignored) {}
                System.out.println("[Server] Client disconnected: " + nick);
            }
        }
    }
}
