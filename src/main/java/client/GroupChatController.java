package client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.*;

public class GroupChatController {
    @FXML private Label groupNameLabel;
    @FXML private VBox messageContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private Label membersLabel;

    private int groupId;
    private boolean historyLoaded = false;

    public void initGroupChat(int groupId, String groupName) {
        this.groupId = groupId;
        groupNameLabel.setText(groupName);

        ServerConnection conn = ServerConnection.getInstance();

        conn.registerHandler("GROUP_MEMBERS", json -> {
            if (json.get("group_id").getAsInt() == groupId) {
                JsonArray membersArr = json.getAsJsonArray("members");
                StringBuilder sb = new StringBuilder("Members: ");
                for (JsonElement e : membersArr) {
                    JsonObject m = e.getAsJsonObject();
                    sb.append(m.get("display_name").getAsString()).append(", ");
                }
                String text = sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
                membersLabel.setText(text);
            }
        });

        conn.registerHandler("GROUP_MESSAGES", json -> {
            if (json.get("group_id").getAsInt() == groupId && !historyLoaded) {
                historyLoaded = true;
                JsonArray messages = json.getAsJsonArray("messages");
                messageContainer.getChildren().clear();
                for (JsonElement elem : messages) {
                    JsonObject msg = elem.getAsJsonObject();
                    addMessageBubble(
                        msg.get("sender_id").getAsInt(),
                        msg.get("sender_name").getAsString(),
                        msg.get("content").getAsString(),
                        msg.get("timestamp").getAsString()
                    );
                }
                scrollPane.setVvalue(1.0);
            }
        });

        conn.registerHandler("NEW_GROUP_MESSAGE", json -> {
            if (json.get("group_id").getAsInt() == groupId) {
                addMessageBubble(
                    json.get("sender_id").getAsInt(),
                    json.get("sender_name").getAsString(),
                    json.get("content").getAsString(),
                    json.get("timestamp").getAsString());
            }
        });

        JsonObject membersReq = new JsonObject();
        membersReq.addProperty("type", "GET_GROUP_MEMBERS");
        membersReq.addProperty("group_id", groupId);
        conn.send(membersReq);

        JsonObject historyReq = new JsonObject();
        historyReq.addProperty("type", "GET_GROUP_MESSAGES");
        historyReq.addProperty("group_id", groupId);
        conn.send(historyReq);
    }

    private void addMessageBubble(int senderId, String senderName, String content, String timestamp) {
        boolean isMine = senderId == SyncTalkClient.currentUserId;
        String time = timestamp.length() > 19 ? timestamp.substring(0, 19) : timestamp;

        VBox bubble = new VBox(3);
        bubble.setMaxWidth(380);

        if (!isMine) {
            Label nameLabel = new Label(senderName);
            nameLabel.getStyleClass().add("bubble-name");
            bubble.getChildren().add(nameLabel);
        }

        Text text = new Text(content);
        text.getStyleClass().add("bubble-text");
        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(8, 14, 8, 14));
        textFlow.getStyleClass().add(isMine ? "bubble-sent" : "bubble-received");
        if (isMine) text.setFill(javafx.scene.paint.Color.WHITE);

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("bubble-time");

        bubble.getChildren().addAll(textFlow, timeLabel);

        HBox wrapper = new HBox(bubble);
        wrapper.setPadding(new Insets(2, 10, 2, 10));
        wrapper.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Platform.runLater(() -> {
            messageContainer.getChildren().add(wrapper);
            scrollPane.setVvalue(1.0);
        });
    }

    @FXML
    private void handleSend() {
        String content = messageField.getText().trim();
        if (content.isEmpty()) return;

        JsonObject request = new JsonObject();
        request.addProperty("type", "SEND_GROUP_MESSAGE");
        request.addProperty("group_id", groupId);
        request.addProperty("content", content);
        ServerConnection.getInstance().send(request);

        addMessageBubble(SyncTalkClient.currentUserId, SyncTalkClient.currentDisplayName, content, new Date().toString());
        messageField.clear();
    }

    @FXML
    private void handleBack() {
        try {
            SyncTalkClient.showMainPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
