package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink;

    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        ServerConnection conn = ServerConnection.getInstance();
        if (!conn.connect("localhost", 8080)) {
            errorLabel.setText("Cannot connect to server!");
            return;
        }

        conn.registerHandler("LOGIN_RESPONSE", json -> {
            boolean success = json.get("success").getAsBoolean();
            if (success) {
                SyncTalkClient.currentUserId = json.get("user_id").getAsInt();
                SyncTalkClient.currentDisplayName = json.get("display_name").getAsString();
                SyncTalkClient.currentUsername = usernameField.getText().trim();
                try {
                    SyncTalkClient.showMainPage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                errorLabel.setText(json.get("message").getAsString());
            }
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        JsonObject request = new JsonObject();
        request.addProperty("type", "LOGIN");
        request.addProperty("username", username);
        request.addProperty("password", password);

        ServerConnection.getInstance().send(request);
    }

    @FXML
    private void goToRegister() {
        try {
            SyncTalkClient.showRegisterPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
