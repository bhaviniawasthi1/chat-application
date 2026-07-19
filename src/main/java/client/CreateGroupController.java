package client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.*;

public class CreateGroupController {
    @FXML private TextField groupNameField;
    @FXML private ListView<Map<String, Object>> userListView;
    @FXML private Button createButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private final ObservableList<Map<String, Object>> users = FXCollections.observableArrayList();
    private final Map<Integer, BooleanProperty> selectedMap = new HashMap<>();

    @FXML
    public void initialize() {
        userListView.setCellFactory(param -> {
            CheckBoxListCell<Map<String, Object>> cell = new CheckBoxListCell<>(item -> {
                if (item == null) return null;
                int id = (int) item.get("id");
                if (!selectedMap.containsKey(id)) {
                    selectedMap.put(id, new SimpleBooleanProperty(false));
                }
                return selectedMap.get(id);
            });
            cell.setConverter(new StringConverter<>() {
                @Override
                public String toString(Map<String, Object> item) {
                    if (item == null) return "";
                    return item.get("display_name") + " (" + item.get("username") + ")";
                }
                @Override
                public Map<String, Object> fromString(String string) {
                    return null;
                }
            });
            return cell;
        });

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

        JsonObject request = new JsonObject();
        request.addProperty("type", "GET_USERS");
        conn.send(request);
    }

    @FXML
    private void handleCreate() {
        String groupName = groupNameField.getText().trim();
        if (groupName.isEmpty()) {
            errorLabel.setText("Please enter a group name");
            return;
        }

        List<Integer> memberIds = new ArrayList<>();
        for (Map.Entry<Integer, BooleanProperty> entry : selectedMap.entrySet()) {
            if (entry.getValue().get()) {
                memberIds.add(entry.getKey());
            }
        }

        if (memberIds.isEmpty()) {
            errorLabel.setText("Please select at least one member");
            return;
        }

        JsonObject request = new JsonObject();
        request.addProperty("type", "CREATE_GROUP");
        request.addProperty("name", groupName);
        JsonArray idsArray = new JsonArray();
        for (int id : memberIds) {
            idsArray.add(id);
        }
        request.add("member_ids", idsArray);
        ServerConnection conn = ServerConnection.getInstance();

        conn.registerHandler("GROUP_CREATED", json -> {
            boolean success = json.get("success").getAsBoolean();
            Platform.runLater(() -> {
                if (success) {
                    Stage stage = (Stage) createButton.getScene().getWindow();
                    stage.close();
                } else {
                    errorLabel.setText("Failed to create group");
                }
            });
        });

        conn.send(request);
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
