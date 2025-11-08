package chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int PORT = 5000;

    // Shared: nickname → PrintWriter (thread-safe)
    protected final ConcurrentHashMap<String, PrintWriter> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new ChatServer().start();
    }

    public void start() {
        System.out.println("[Server] Starting on port " + PORT + " ...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] Client connected from " + socket.getRemoteSocketAddress());

                // Create a new handler thread for this client
                ClientHandler handler = new ClientHandler(socket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("[Server] Fatal error: " + e.getMessage());
        }
    }

    // Broadcast message to all connected clients
    public void broadcast(String msg) {
        for (PrintWriter pw : clients.values()) {
            pw.println(msg);
        }
    }
    public void broadcastExcept(String exceptNick, String msg) {
        clients.forEach((nick, pw) -> {
            if (!nick.equals(exceptNick)) pw.println(msg);
        });
    }
    public void sendTo(String nick, String msg) {
        PrintWriter pw = clients.get(nick);
        if (pw != null) pw.println(msg);
    }
}
