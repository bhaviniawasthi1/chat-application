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

public class ChatController {
    @FXML private Label chatWithLabel;
    @FXML private VBox messageContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;

    private int otherUserId;
    private String otherUserName;

    public void initChat(int otherUserId, String otherUserName) {
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        chatWithLabel.setText(otherUserName);

        ServerConnection conn = ServerConnection.getInstance();

        conn.registerHandler("CHAT_HISTORY", json -> {
            if (json.get("other_user_id").getAsInt() == otherUserId) {
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

        conn.registerHandler("NEW_MESSAGE", json -> {
            int senderId = json.get("sender_id").getAsInt();
            if (senderId == otherUserId) {
                addMessageBubble(senderId,
                    json.get("sender_name").getAsString(),
                    json.get("content").getAsString(),
                    json.get("timestamp").getAsString());
            }
        });

        JsonObject request = new JsonObject();
        request.addProperty("type", "GET_CHAT_HISTORY");
        request.addProperty("other_user_id", otherUserId);
        conn.send(request);
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
        request.addProperty("type", "SEND_MESSAGE");
        request.addProperty("receiver_id", otherUserId);
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
