package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SyncTalkServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("SyncTalk Server starting on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running. Waiting for clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
