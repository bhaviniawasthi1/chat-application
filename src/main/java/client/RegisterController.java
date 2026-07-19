package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField displayNameField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink loginLink;

    @FXML
    public void initialize() {
        ServerConnection conn = ServerConnection.getInstance();

        conn.registerHandler("REGISTER_RESPONSE", json -> {
            boolean success = json.get("success").getAsBoolean();
            if (success) {
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Registration successful! Go to login.");
            } else {
                errorLabel.setText(json.get("message").getAsString());
            }
        });
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String displayName = displayNameField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        JsonObject request = new JsonObject();
        request.addProperty("type", "REGISTER");
        request.addProperty("username", username);
        request.addProperty("password", password);
        request.addProperty("display_name", displayName);

        ServerConnection.getInstance().send(request);
    }

    @FXML
    private void goToLogin() {
        try {
            SyncTalkClient.showLoginPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
