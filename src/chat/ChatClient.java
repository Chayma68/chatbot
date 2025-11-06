package chat;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "127.0.0.1";
        int port    = (args.length > 1) ? Integer.parseInt(args[1]) : 5000;
        String username = (args.length > 2) ? args[2] : null;

        try (Socket socket = new Socket(host, port)) {
            System.out.println("[CLIENT] Connected to " + host + ":" + port);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out   = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // Lecture serveur en arrière-plan (thread)
            Thread listener = new Thread(() -> {
                try {
                    String srvMsg;
                    while ((srvMsg = in.readLine()) != null) {
                        System.out.println(srvMsg);
                    }
                } catch (IOException ignored) {}
                System.out.println("[CLIENT] Disconnected.");
                System.exit(0);
            });
            listener.setDaemon(true);
            listener.start();

            // Envoyer le username comme premier message (si fourni)
            Scanner sc = new Scanner(System.in);
            String prompt = in.readLine(); // "Enter your username:"
            if (username == null) {
                System.out.print(prompt + " ");
                username = sc.nextLine();
            } else {
                System.out.println(prompt + " (auto) " + username);
            }
            out.println(username);

            System.out.println("Type messages. /quit pour quitter.");
            while (true) {
                String userInput = sc.nextLine();
                out.println(userInput);
                if ("/quit".equalsIgnoreCase(userInput)) break;
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Error: " + e.getMessage());
        }
    }
}
