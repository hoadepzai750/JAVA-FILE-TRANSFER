package network;

import java.sql.*;

public class DatabaseHelper {
    // The SQLite database file will be automatically created if it does not exist
    private static final String DB_URL = "jdbc:sqlite:file_sharing.db";

   
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    
    public static void initializeDatabase() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

       
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS Users (" +
                            "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "username TEXT NOT NULL UNIQUE," +
                            "password TEXT NOT NULL)"
            );

        
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS Files (" +
                            "file_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user_id INTEGER NOT NULL," +
                            "file_name TEXT NOT NULL," +
                            "file_path TEXT NOT NULL," +
                            "file_size INTEGER NOT NULL," +
                            "file_type TEXT," +
                            "FOREIGN KEY (user_id) REFERENCES Users (user_id))"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean saveUserAccount(String name, String password) {
        String query = "INSERT INTO Users (username, password) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, password);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean validateUserAccount(String name, String password) {
        String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // Returns true if a record is found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void saveFileRecord(int userId, String fileName, String filePath, long fileSize, String fileType) {
        String query = "INSERT INTO Files (user_id, file_name, file_path, file_size, file_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setString(2, fileName);
            statement.setString(3, filePath);
            statement.setLong(4, fileSize);
            statement.setString(5, fileType);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
