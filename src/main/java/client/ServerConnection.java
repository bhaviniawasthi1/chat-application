package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ServerConnection {
    private static ServerConnection instance;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final Gson gson = new Gson();
    private final Map<String, List<Consumer<JsonObject>>> handlers = new ConcurrentHashMap<>();
    private boolean listening = false;

    private ServerConnection() {}

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            startListening();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void send(JsonObject json) {
        writer.println(gson.toJson(json));
    }

    public void registerHandler(String type, Consumer<JsonObject> handler) {
        handlers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public void removeHandler(String type) {
        handlers.remove(type);
    }

    private void startListening() {
        if (listening) return;
        listening = true;
        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    JsonObject json = gson.fromJson(line, JsonObject.class);
                    String type = json.get("type").getAsString();
                    List<Consumer<JsonObject>> list = handlers.get(type);
                    if (list != null) {
                        for (Consumer<JsonObject> handler : list) {
                            Platform.runLater(() -> handler.accept(json));
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection closed");
            }
        }).start();
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void disconnect() {
        handlers.clear();
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
