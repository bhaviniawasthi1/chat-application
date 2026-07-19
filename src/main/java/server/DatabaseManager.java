package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/synctalk";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public boolean registerUser(String username, String password, String displayName) {
        String sql = "INSERT INTO users (username, password, display_name) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, displayName);
            stmt.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int authenticateUser(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getDisplayName(int userId) {
        String sql = "SELECT display_name FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("display_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUsername(int userId) {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, Object>> getAllUsers(int excludeUserId) {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT id, username, display_name FROM users WHERE id != ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, excludeUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("display_name", rs.getString("display_name"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean saveMessage(int senderId, Integer receiverId, Integer groupId, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, group_id, content) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            if (receiverId != null) {
                stmt.setInt(2, receiverId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            if (groupId != null) {
                stmt.setInt(3, groupId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, content);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getChatHistory(int userId1, int userId2) {
        List<Map<String, Object>> messages = new ArrayList<>();
        String sql = "SELECT m.id, m.sender_id, m.content, m.timestamp, u.display_name AS sender_name " +
                     "FROM messages m JOIN users u ON m.sender_id = u.id " +
                     "WHERE ((m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?)) " +
                     "AND m.group_id IS NULL ORDER BY m.timestamp ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("id", rs.getInt("id"));
                msg.put("sender_id", rs.getInt("sender_id"));
                msg.put("content", rs.getString("content"));
                msg.put("timestamp", rs.getString("timestamp"));
                msg.put("sender_name", rs.getString("sender_name"));
                messages.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<Map<String, Object>> getGroupMessages(int groupId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        String sql = "SELECT m.id, m.sender_id, m.content, m.timestamp, u.display_name AS sender_name " +
                     "FROM messages m JOIN users u ON m.sender_id = u.id " +
                     "WHERE m.group_id = ? ORDER BY m.timestamp ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("id", rs.getInt("id"));
                msg.put("sender_id", rs.getInt("sender_id"));
                msg.put("content", rs.getString("content"));
                msg.put("timestamp", rs.getString("timestamp"));
                msg.put("sender_name", rs.getString("sender_name"));
                messages.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public int createGroup(String name, int createdBy, List<Integer> memberIds) {
        String sql = "INSERT INTO `groups` (name, created_by) VALUES (?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setInt(2, createdBy);
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int groupId = keys.getInt(1);
                    String memberSql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
                    try (PreparedStatement memberStmt = connection.prepareStatement(memberSql)) {
                        memberStmt.setInt(1, groupId);
                        memberStmt.setInt(2, createdBy);
                        memberStmt.addBatch();
                        for (int memberId : memberIds) {
                            memberStmt.setInt(1, groupId);
                            memberStmt.setInt(2, memberId);
                            memberStmt.addBatch();
                        }
                        memberStmt.executeBatch();
                    }
                    connection.commit();
                    return groupId;
                }
            }
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public List<Map<String, Object>> getUserGroups(int userId) {
        List<Map<String, Object>> groups = new ArrayList<>();
        String sql = "SELECT g.id, g.name FROM `groups` g " +
                     "JOIN group_members gm ON g.id = gm.group_id WHERE gm.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> group = new HashMap<>();
                group.put("id", rs.getInt("id"));
                group.put("name", rs.getString("name"));
                groups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public List<Map<String, Object>> getGroupMembers(int groupId) {
        List<Map<String, Object>> members = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.display_name FROM group_members gm " +
                     "JOIN users u ON gm.user_id = u.id WHERE gm.group_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> member = new HashMap<>();
                member.put("id", rs.getInt("id"));
                member.put("username", rs.getString("username"));
                member.put("display_name", rs.getString("display_name"));
                members.add(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }
}
