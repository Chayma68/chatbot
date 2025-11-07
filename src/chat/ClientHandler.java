package chat;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatServer server;
    private String nick; // nickname of this client
    private PrintWriter out;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out = new PrintWriter(socket.getOutputStream(), true);

            // 1️⃣ Read nickname (first line from client)
            String requested = in.readLine();
            if (requested == null) {
                return; // client closed immediately
            }
            requested = requested.trim();

            // Check if nickname is empty
            if (requested.isEmpty()) {
                out.println("* Nickname cannot be empty. Connection closed. *");
                return;
            }

            // 2️⃣ Enforce unique nickname
            ConcurrentHashMap<String, PrintWriter> clients = server.clients;
            if (clients.putIfAbsent(requested, out) != null) {
                out.println("* Nickname already in use. Try another. *");
                return;
            }

            nick = requested; // nickname accepted
            System.out.println("[Server] Nick registered: " + nick);
            server.broadcast("* " + nick + " joined *");

            // 3️⃣ Chat loop: read and broadcast
            String line;
            while ((line = in.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.equalsIgnoreCase("/quit")) {
                    break;
                }

                if (!trimmed.isEmpty()) {
                    server.broadcast("[" + nick + "] " + line);
                }
            }

        } catch (IOException e) {
            System.err.println("[Server] Client error: " + e.getMessage());
        } finally {
            // 4️⃣ Cleanup
            try { socket.close(); } catch (IOException ignored) {}

            if (nick != null) {
                server.clients.remove(nick);
                server.broadcast("* " + nick + " left *");
            }

            System.out.println("[Server] Client disconnected: " + (nick == null ? "<unregistered>" : nick));
        }
    }
}
