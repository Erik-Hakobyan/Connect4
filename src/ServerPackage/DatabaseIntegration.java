package ServerPackage;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;


public class DatabaseIntegration {
    // Only works for my code
        private static final String URL = "jdbc:mysql://localhost:3306/chat";
        private static final String USERNAME = "root";
        private static final String PASSWORD = "abc123!@#";
    
        public static Connection getConnection() throws SQLException, ClassNotFoundException {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
    
        public static void registerUser(String username) throws SQLException, ClassNotFoundException {
            String sql = "INSERT INTO PLAYERS (username) VALUES (?)";
    
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Could not connect to database");
            } catch (ClassNotFoundException e) {
                System.out.println("Could not find JDBC driver");
            }
        }
    
        public static void updateStats(String username, boolean won, boolean draw) throws SQLException, ClassNotFoundException {
            String sql = "UPDATE PLAYERS SET games_played = games_played + 1, " +
                         "games_won = games_won + ?, " +
                         "games_lost = games_lost + ?, " +
                         "games_drawn = games_drawn + ? " +
                         "WHERE username = ?";
    
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setInt(1, won ? 1 : 0);
                stmt.setInt(2, (!won && !draw) ? 1 : 0);
                stmt.setInt(3, draw ? 1 : 0);
                stmt.setString(4, username);
                stmt.executeUpdate();
            }
        }
     
        public static void getStats(String username) throws SQLException, ClassNotFoundException {
            String sql = "SELECT * FROM PLAYERS WHERE username = ?";
    
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
    
                if (rs.next()) {
                    System.out.println("Username: " + rs.getString("username"));
                    System.out.println("Games Played: " + rs.getInt("games_played"));
                    System.out.println("Games Won: " + rs.getInt("games_won"));
                    System.out.println("Games Lost: " + rs.getInt("games_lost"));
                    System.out.println("Games Drawn: " + rs.getInt("games_drawn"));
                } else {
                    System.out.println("No user found with username: " + username);
                }
            }
        }
    }
