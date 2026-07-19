package client.models;

public class User {
    private int id;
    private String username;
    private String displayName;

    public User(int id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }

    @Override
    public String toString() {
        return displayName + " (" + username + ")";
    }
}
