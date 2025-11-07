package chat;

import java.io.*;
import java.net.Socket;
import java.util.List;

class ClientHandler implements Runnable {
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
