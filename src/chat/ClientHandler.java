package chat;

import java.io.*;
import java.net.Socket;

class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username = "Anonymous";

    ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void send(String msg) {
        if (out != null) out.println(msg);
    }

    @Override
    public void run() {
        try (socket) {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // 1) Premier message = pseudo
            out.println("Enter your username:");
            String name = in.readLine();
            if (name != null && !name.isBlank()) username = name.trim();

            server.broadcast("🟢 " + username + " joined the chat.", this);
            System.out.println("[SERVER] " + username + " connected from " + socket.getRemoteSocketAddress());

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("/quit")) break;
                String msg = "💬 " + username + ": " + line;
                server.broadcast(msg, this);
                // Echo au sender pour feedback
                send("(you) " + line);
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Client error: " + e.getMessage());
        } finally {
            server.unregister(this);
            server.broadcast("🔴 " + username + " left the chat.", this);
            System.out.println("[SERVER] " + username + " disconnected.");
        }
    }
}
