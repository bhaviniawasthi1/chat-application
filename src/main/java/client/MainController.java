package client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;

import java.util.*;

public class MainController {
    @FXML private Label welcomeLabel;
    @FXML private ListView<Map<String, Object>> userListView;
    @FXML private ListView<Map<String, Object>> groupListView;
    @FXML private Button refreshButton;
    @FXML private Button createGroupButton;
    @FXML private Label errorLabel;

    private final ObservableList<Map<String, Object>> users = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> groups = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + SyncTalkClient.currentDisplayName + "!");

        userListView.setCellFactory(param -> {
            ListCell<Map<String, Object>> cell = new ListCell<>() {
                @Override
                protected void updateItem(Map<String, Object> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText(item.get("display_name") + " (" + item.get("username") + ")");
                }
            };
            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Map<String, Object> item = cell.getItem();
                    if (item != null) {
                        try {
                            SyncTalkClient.showChatPage((int) item.get("id"), (String) item.get("display_name"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return cell;
        });

        groupListView.setCellFactory(param -> {
            ListCell<Map<String, Object>> cell = new ListCell<>() {
                @Override
                protected void updateItem(Map<String, Object> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText((String) item.get("name"));
                }
            };
            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Map<String, Object> item = cell.getItem();
                    if (item != null) {
                        try {
                            SyncTalkClient.showGroupChatPage((int) item.get("id"), (String) item.get("name"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return cell;
        });

        setupHandlers();
        loadData();
    }

    private void setupHandlers() {
        ServerConnection conn = ServerConnection.getInstance();

        conn.registerHandler("USERS_LIST", json -> {
            JsonArray usersArray = json.getAsJsonArray("users");
            List<Map<String, Object>> userList = new ArrayList<>();
            for (JsonElement e : usersArray) {
                JsonObject u = e.getAsJsonObject();
                Map<String, Object> map = new HashMap<>();
                map.put("id", u.get("id").getAsInt());
                map.put("username", u.get("username").getAsString());
                map.put("display_name", u.get("display_name").getAsString());
                userList.add(map);
            }
            users.setAll(userList);
            userListView.setItems(users);
        });

        conn.registerHandler("GROUPS_LIST", json -> {
            JsonArray groupsArray = json.getAsJsonArray("groups");
            List<Map<String, Object>> groupList = new ArrayList<>();
            for (JsonElement e : groupsArray) {
                JsonObject g = e.getAsJsonObject();
                Map<String, Object> map = new HashMap<>();
                map.put("id", g.get("id").getAsInt());
                map.put("name", g.get("name").getAsString());
                groupList.add(map);
            }
            groups.setAll(groupList);
            groupListView.setItems(groups);
        });

        conn.registerHandler("NEW_MESSAGE", json -> {
            String from = json.get("sender_name").getAsString();
            String content = json.get("content").getAsString();
            errorLabel.setText("Message from " + from + ": " + content);
        });

        conn.registerHandler("NEW_GROUP_MESSAGE", json -> {
            String from = json.get("sender_name").getAsString();
            String content = json.get("content").getAsString();
            errorLabel.setText("Group message from " + from + ": " + content);
        });
    }

    private void loadData() {
        JsonObject userReq = new JsonObject();
        userReq.addProperty("type", "GET_USERS");
        ServerConnection.getInstance().send(userReq);

        JsonObject groupReq = new JsonObject();
        groupReq.addProperty("type", "GET_GROUPS");
        ServerConnection.getInstance().send(groupReq);
    }

    @FXML
    private void handleRefresh() {
        loadData();
        errorLabel.setText("Refreshed!");
    }

    @FXML
    private void handleCreateGroup() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/create_group.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Create Group");
            stage.setScene(new javafx.scene.Scene(root, 400, 500));
            stage.showAndWait();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "LOGOUT");
        ServerConnection.getInstance().send(request);
        try {
            SyncTalkClient.showLoginPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
