package chat;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner console = new Scanner(System.in)
        ) {
            // 1) Send nickname (protocol: first line is nick)
            System.out.print("Enter your nickname: ");
            String nick = console.nextLine().trim();
            out.println(nick);

            // 2) Background thread: prints everything from server to console
            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException ignored) {
                } finally {
                    System.out.println("* Disconnected from server *");
                }
            });
            reader.setDaemon(true);
            reader.start();

            // 3) Main thread: read from keyboard and send to server
            System.out.println("* You can start typing. Use /quit to exit. *");
            while (true) {
                if (!console.hasNextLine()) break;
                String msg = console.nextLine();
                out.println(msg);
                if ("/quit".equalsIgnoreCase(msg.trim())) break;
            }
        } catch (IOException e) {
            System.err.println("[Client] Error: " + e.getMessage());
        }
    }
}
