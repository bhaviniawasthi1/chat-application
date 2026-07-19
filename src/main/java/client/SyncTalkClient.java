package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SyncTalkClient extends Application {

    private static Stage primaryStage;
    public static int currentUserId;
    public static String currentDisplayName;
    public static String currentUsername;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showLoginPage();
        primaryStage.setTitle("SyncTalk - Chat Application");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void showLoginPage() throws Exception {
        FXMLLoader loader = new FXMLLoader(SyncTalkClient.class.getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 520, 620);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void showRegisterPage() throws Exception {
        FXMLLoader loader = new FXMLLoader(SyncTalkClient.class.getResource("/fxml/register.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 520, 620);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void showMainPage() throws Exception {
        FXMLLoader loader = new FXMLLoader(SyncTalkClient.class.getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 520, 620);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void showChatPage(int otherUserId, String otherUserName) throws Exception {
        FXMLLoader loader = new FXMLLoader(SyncTalkClient.class.getResource("/fxml/chat.fxml"));
        Parent root = loader.load();
        ChatController controller = loader.getController();
        controller.initChat(otherUserId, otherUserName);
        Scene scene = new Scene(root, 520, 620);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void showGroupChatPage(int groupId, String groupName) throws Exception {
        FXMLLoader loader = new FXMLLoader(SyncTalkClient.class.getResource("/fxml/group_chat.fxml"));
        Parent root = loader.load();
        GroupChatController controller = loader.getController();
        controller.initGroupChat(groupId, groupName);
        Scene scene = new Scene(root, 520, 620);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
