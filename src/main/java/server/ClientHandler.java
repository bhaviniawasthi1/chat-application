package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private static final ConcurrentHashMap<Integer, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private int userId;
    private String username;
    private String displayName;
    private DatabaseManager db;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.db = DatabaseManager.getInstance();
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonObject request = gson.fromJson(line, JsonObject.class);
                String type = request.get("type").getAsString();
                handleRequest(type, request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void handleRequest(String type, JsonObject request) {
        switch (type) {
            case "REGISTER":
                handleRegister(request);
                break;
            case "LOGIN":
                handleLogin(request);
                break;
            case "GET_USERS":
                handleGetUsers();
                break;
            case "SEND_MESSAGE":
                handleSendMessage(request);
                break;
            case "GET_CHAT_HISTORY":
                handleGetChatHistory(request);
                break;
            case "CREATE_GROUP":
                handleCreateGroup(request);
                break;
            case "GET_GROUPS":
                handleGetGroups();
                break;
            case "GET_GROUP_MESSAGES":
                handleGetGroupMessages(request);
                break;
            case "SEND_GROUP_MESSAGE":
                handleSendGroupMessage(request);
                break;
            case "GET_GROUP_MEMBERS":
                handleGetGroupMembers(request);
                break;
            case "LOGOUT":
                handleLogout();
                break;
        }
    }

    private void handleRegister(JsonObject request) {
        String username = request.get("username").getAsString();
        String password = request.get("password").getAsString();
        String displayName = request.get("display_name").getAsString();

        boolean success = db.registerUser(username, password, displayName);
        JsonObject response = new JsonObject();
        response.addProperty("type", "REGISTER_RESPONSE");
        response.addProperty("success", success);
        response.addProperty("message", success ? "Registration successful" : "Username already exists");
        writer.println(gson.toJson(response));
    }

    private void handleLogin(JsonObject request) {
        String username = request.get("username").getAsString();
        String password = request.get("password").getAsString();

        int id = db.authenticateUser(username, password);
        JsonObject response = new JsonObject();
        response.addProperty("type", "LOGIN_RESPONSE");
        if (id != -1) {
            this.userId = id;
            this.username = username;
            this.displayName = db.getDisplayName(id);
            onlineUsers.put(id, this);
            response.addProperty("success", true);
            response.addProperty("user_id", id);
            response.addProperty("display_name", displayName);
            response.addProperty("message", "Login successful");
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid username or password");
        }
        writer.println(gson.toJson(response));
    }

    private void handleGetUsers() {
        List<Map<String, Object>> users = db.getAllUsers(userId);
        JsonObject response = new JsonObject();
        response.addProperty("type", "USERS_LIST");
        response.add("users", gson.toJsonTree(users));
        writer.println(gson.toJson(response));
    }

    private void handleSendMessage(JsonObject request) {
        int receiverId = request.get("receiver_id").getAsInt();
        String content = request.get("content").getAsString();

        boolean saved = db.saveMessage(userId, receiverId, null, content);
        JsonObject response = new JsonObject();
        response.addProperty("type", "MESSAGE_SENT");
        response.addProperty("success", saved);
        writer.println(gson.toJson(response));

        ClientHandler receiver = onlineUsers.get(receiverId);
        if (receiver != null) {
            JsonObject notify = new JsonObject();
            notify.addProperty("type", "NEW_MESSAGE");
            notify.addProperty("sender_id", userId);
            notify.addProperty("sender_name", displayName);
            notify.addProperty("content", content);
            notify.addProperty("timestamp", new Date().toString());
            receiver.writer.println(gson.toJson(notify));
        }
    }

    private void handleGetChatHistory(JsonObject request) {
        int otherUserId = request.get("other_user_id").getAsInt();
        List<Map<String, Object>> messages = db.getChatHistory(userId, otherUserId);
        JsonObject response = new JsonObject();
        response.addProperty("type", "CHAT_HISTORY");
        response.addProperty("other_user_id", otherUserId);
        response.add("messages", gson.toJsonTree(messages));
        writer.println(gson.toJson(response));
    }

    private void handleCreateGroup(JsonObject request) {
        String groupName = request.get("name").getAsString();
        List<Integer> memberIds = new ArrayList<>();
        request.get("member_ids").getAsJsonArray().forEach(e -> memberIds.add(e.getAsInt()));

        int groupId = db.createGroup(groupName, userId, memberIds);
        JsonObject response = new JsonObject();
        response.addProperty("type", "GROUP_CREATED");
        response.addProperty("success", groupId != -1);
        response.addProperty("group_id", groupId);
        response.addProperty("group_name", groupName);
        writer.println(gson.toJson(response));
    }

    private void handleGetGroups() {
        List<Map<String, Object>> groups = db.getUserGroups(userId);
        JsonObject response = new JsonObject();
        response.addProperty("type", "GROUPS_LIST");
        response.add("groups", gson.toJsonTree(groups));
        writer.println(gson.toJson(response));
    }

    private void handleGetGroupMessages(JsonObject request) {
        int groupId = request.get("group_id").getAsInt();
        List<Map<String, Object>> messages = db.getGroupMessages(groupId);
        JsonObject response = new JsonObject();
        response.addProperty("type", "GROUP_MESSAGES");
        response.addProperty("group_id", groupId);
        response.add("messages", gson.toJsonTree(messages));
        writer.println(gson.toJson(response));
    }

    private void handleSendGroupMessage(JsonObject request) {
        int groupId = request.get("group_id").getAsInt();
        String content = request.get("content").getAsString();

        boolean saved = db.saveMessage(userId, null, groupId, content);
        JsonObject response = new JsonObject();
        response.addProperty("type", "GROUP_MESSAGE_SENT");
        response.addProperty("success", saved);
        writer.println(gson.toJson(response));

        List<Map<String, Object>> members = db.getGroupMembers(groupId);
        for (Map<String, Object> member : members) {
            int memberId = ((Number) member.get("id")).intValue();
            ClientHandler handler = onlineUsers.get(memberId);
            if (handler != null && memberId != userId) {
                JsonObject notify = new JsonObject();
                notify.addProperty("type", "NEW_GROUP_MESSAGE");
                notify.addProperty("group_id", groupId);
                notify.addProperty("sender_id", userId);
                notify.addProperty("sender_name", displayName);
                notify.addProperty("content", content);
                notify.addProperty("timestamp", new Date().toString());
                handler.writer.println(gson.toJson(notify));
            }
        }
    }

    private void handleGetGroupMembers(JsonObject request) {
        int groupId = request.get("group_id").getAsInt();
        List<Map<String, Object>> members = db.getGroupMembers(groupId);
        JsonObject response = new JsonObject();
        response.addProperty("type", "GROUP_MEMBERS");
        response.addProperty("group_id", groupId);
        response.add("members", gson.toJsonTree(members));
        writer.println(gson.toJson(response));
    }

    private void handleLogout() {
        onlineUsers.remove(userId);
    }

    private void cleanup() {
        handleLogout();
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isUserOnline(int userId) {
        return onlineUsers.containsKey(userId);
    }
}
