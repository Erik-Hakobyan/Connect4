package ServerPackage;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.sql.*;


public class Connect4Server extends Thread {
    private final int port;
    private boolean live;
    private Map<String, Map<String, ClientConnection>> Games_Map = new HashMap<>();
    private Map<String, Map<String, ClientConnection>> Games = Collections.synchronizedMap(Games_Map);

    public Connect4Server(int port_config) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        port = port_config;
        live = true;
        start();
    }
    
    

    public void stopServer() {
        live = false;
    }

    private void relayAll(String key, String message) {
        Map<String, ClientConnection> aGame = Games.get(key);
        if (aGame != null) {
            for (ClientConnection connection : aGame.values()) {
                if (connection != null) {
                    connection.relay(message);
                }
            }
        }
    }


    @Override
    public void run() {
        try {
            System.out.println("Server IP Address: " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("Port: " + port);
            try (ServerSocket sock = new ServerSocket(port)) {
                while (live) {
                    Socket socket = sock.accept();
                    new ClientConnection(socket, this);
                }
            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    class ClientConnection extends Thread {
        private static final int[][] default_board = {
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0}
        };
        private int[][] currentBoard;
        private BufferedReader in;
        private PrintStream out;
        private int requestType;
        public String name, username;
        private String game_key;
        private final Connect4Server Server;
        Socket sock;
        private boolean isMyTurn;
        private ConnectFourGame game;

        public ClientConnection(Socket socket, Connect4Server server) {
            sock = socket;
            Server = server;
            verifyConnection();
        }

        private void newGame() {
            game_key = generateKey();
            Map<String, ClientConnection> map_unsy = new HashMap<>();
            Map<String, ClientConnection> map = Collections.synchronizedMap(map_unsy);
            map.put("Player1", this);
            map.put("Player2", null);
            Games.put(game_key, map);
            out.println("GK:" + game_key);
            currentBoard = default_board;
            game = new ConnectFourGame(6, 7, name, null); // Create a new game instance
            boardToDB();
            isMyTurn = true; 
            
        }

        private void joinGame() {
            if (Games.containsKey(game_key)) {
                isMyTurn = false;
                DBToBoard();
                Map<String, ClientConnection> map_unsync = Games.get(game_key);
                Map<String, ClientConnection> map = Collections.synchronizedMap(map_unsync);
                if (map.get("Player1") != null && map.get("Player2") != null) {
                    out.println("Error 100");
                } else if (map.get("Player1") == null) {
                    map.put("Player1", this);
                    out.println("PLAYER:1");
                } else if (map.get("Player2") == null) {
                    out.println("PLAYER:2");
                    map.put("Player2", this);
                }
                Games.put(game_key, map);
                String enter = "CHAT:" + name + " (" + username + ") " + "has entered the game!";
                Server.relayAll(game_key, enter);
            }
        }


        private void spectate() {
            if (Games.containsKey(game_key)) {
                String enter = "CHAT:" + name + " (" + username + ") " + "is spectating the game!";
                Server.relayAll(game_key, enter);
                out.println("SUCCESS");
            } else {
                out.println("ERROR 200");
            }
        }

        private void relay(String message) {
            out.println(message);
        }

        private static String generateKey() {
            Random random = new Random();
            StringBuilder randomStringBuilder = new StringBuilder(8);

            for (int i = 0; i < 4; i++) {
                char randomLetter = (char) ('A' + random.nextInt(26));
                randomStringBuilder.append(randomLetter);
            }

            for (int i = 0; i < 4; i++) {
                int randomNumber = random.nextInt(10);
                randomStringBuilder.append(randomNumber);
            }

            return randomStringBuilder.toString();
        }


        private void initializeMode() {
            switch (requestType) {
                case 1:
                    newGame();
                    break;
                case 2:
                    joinGame();
                    break;
                case 3:
                    spectate();
                    break;
                default:
                    break;
            }
        }


        private void verifyConnection() {
            try {
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintStream(sock.getOutputStream());
                String line = in.readLine();
                processRequest(line);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

        @Override
        public void run() {
            initializeMode();
            String userRequest;
            for (;;) {
                try {
                    if ((userRequest = in.readLine()) != null) {
                        processRequest(userRequest);
                        Thread.sleep(25);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        private void processRequest(String request) {
            String[] parts;
            if (!request.isEmpty()) {
                parts = request.split(":");
                System.out.println("REQUEST REC: " + request);
                switch (parts[0]) {
                    case "Authenticate":
                        authenticate(parts);
                        break;
                    case "CHAT":
                        String mess = "CHAT:" + name + "(" + username + "): " + parts[1];
                        Server.relayAll(game_key, mess);
                        break;
                    case "MOVE":
                        validateMove(parts[1]);
                    case "COMMAND":
                        switch (parts[1]) {
                            case "STATS":
                                statsCommand();
                                break;
                            case "NEW":
                                newCommand();
                                break;
                            case "RESIGN":
                                resignCommand();
                                break;
                            case "DRAW":
                                drawCommand();
                                break;
                        }
                        break;
                }
            }
        }

        private void validateMove(String move) {
            if (isMyTurn) {
                int column = Integer.parseInt(move);
        
                if (game.isValidMove(column)) {
                    // Make the move
                    boolean isValidMove = game.makeMove(column);
        
                    if (isValidMove) {
                        // Update the game state or any relevant variables
        
                        // Check for a game over condition
                        ConnectFourGame.GameState gameState = game.checkGameState();
                        if (gameState != ConnectFourGame.GameState.IN_PROGRESS) {
                            if (gameState == ConnectFourGame.GameState.PLAYER_ONE_WIN) {
                                // Player 1 wins
                                relayAll(game_key, "CHAT: Game over! " + Games.get(game_key).get("Player1").name + " wins!");
                            } else if (gameState == ConnectFourGame.GameState.PLAYER_TWO_WIN) {
                                // Player 2 wins
                                relayAll(game_key, "CHAT: Game over! " + Games.get(game_key).get("Player2").name + " wins!");
                            } else if (gameState == ConnectFourGame.GameState.DRAW) {
                                // Draw
                                relayAll(game_key, "CHAT: Game over! It's a draw!");
                            }
        
                            // Reset the game state
                            game.resetGame();
        
                            // Perform any other necessary cleanup or actions
        
                           // return;
                        }
        
                        // Switch the turn to the next player
                        boardToDB();
                        switchTurn();
                        return;
                    } else {
                        // Invalid move
                        relayAll(game_key, "CHAT: Invalid move!");
                    }
                } else {
                    // Invalid column
                    relayAll(game_key, "CHAT: Invalid column!");
                }
            } else {
                // It's not the player's turn
                relayAll(game_key, "CHAT: It's not your turn!");
            }
        }
        
        
        
    
        private void switchTurn() {
            // Get the game players
            Map<String, ClientConnection> game = Games.get(game_key);
            ClientConnection player1 = game.get("Player1");
            ClientConnection player2 = game.get("Player2");
        
            // Switch the turn
            if (player1 != null && player2 != null) {
                if (this.equals(player1)) {
                    player1.isMyTurn = false;
                    player2.isMyTurn = true;
                    player1.out.println("TURN:2");
                    player2.out.println("TURN:1");
                } else {
                    player1.isMyTurn = true;
                    player2.isMyTurn = false;
                    player1.out.println("TURN:1");
                    player2.out.println("TURN:2");
                }
            }
        }
        

        private void boardToDB() {
            String boardString = Arrays.stream(currentBoard)
                .map(row -> Arrays.stream(row)
                    .mapToObj(Integer::toString)
                    .collect(Collectors.joining(",")))
                .collect(Collectors.joining(";"));
        
            try {
                Connection connection = DatabaseIntegration.getConnection();
                PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO game_states (game_key, board_state) VALUES (?, ?) ON DUPLICATE KEY UPDATE board_state = VALUES(board_state)");
                stmt.setString(1, game_key);
                stmt.setString(2, boardString);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        
        
        
        private void DBToBoard() {
            try {
                Connection connection = DatabaseIntegration.getConnection();
                PreparedStatement stmt = connection.prepareStatement(
                    "SELECT board_state FROM game_states WHERE game_key = ?");
                stmt.setString(1, game_key);
                ResultSet rs = stmt.executeQuery();
        
                if (rs.next()) {
                    String boardString = rs.getString("board_state");
                    String[] rowStrings = boardString.split(";");
                    for (int i = 0; i < rowStrings.length; i++) {
                        String[] cellStrings = rowStrings[i].split(",");
                        for (int j = 0; j < cellStrings.length; j++) {
                            currentBoard[i][j] = Integer.parseInt(cellStrings[j]);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        


        private void authenticate(String[] parts) {
            System.out.println("Authenticating");
            if (parts[0].equals("Authenticate")) {
                requestType = Integer.parseInt(parts[1]);
                name = parts[2];
                username = parts[3];
                game_key = parts[4];
                out.println("SUCCESS");
                start();
            }
        }

        private void drawCommand() {
            String message = "CHAT:" + name + " Has Offered A Draw!";
            relayAll(game_key, message);
        }

        private void resignCommand() {
            String message = "CHAT:" + name + " Has Resigned!";
            relayAll(game_key, message);
        }

        private void statsCommand() {
            try {
                Connection connection = DatabaseIntegration.getConnection();
                String sql = "SELECT * FROM users WHERE username = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
        
                if (rs.next()) {
                    String message = "CHAT: " + name + " Stats - " +
                        "Games Played: " + rs.getInt("games_played") + ", " +
                        "Games Won: " + rs.getInt("games_won") + ", " +
                        "Games Lost: " + rs.getInt("games_lost") + ", " +
                        "Games Drawn: " + rs.getInt("games_drawn");
                    relayAll(game_key, message);
                } else {
                    String message = "CHAT: No stats available for user " + username;
                    relayAll(game_key, message);
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void newCommand() {
            String message = "CHAT:" + name + " has started new game!";
            relayAll(game_key, message);
        }

        public static void main(String[] args) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int port = 1234;
                    new Connect4Server(port);
                }
            });
        }
    }

}



