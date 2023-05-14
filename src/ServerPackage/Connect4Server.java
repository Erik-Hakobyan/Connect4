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
        private int moveOrder;
        private int playerNumber;
        private BufferedReader in;
        private PrintStream out;
        private int requestType;
        public String name, username;
        private String game_key;
        private final Connect4Server Server;
        Socket sock;

        public ClientConnection(Socket socket, Connect4Server server) {
            sock = socket;
            Server = server;
            verifyConnection();
            this.moveOrder = 0;
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
   
        }

        private void joinGame() {
            if (Games.containsKey(game_key)) {
                Map<String, ClientConnection> map_unsync = Games.get(game_key);
                Map<String, ClientConnection> map = Collections.synchronizedMap(map_unsync);
                if (map.get("Player1") != null && map.get("Player2") != null) {
                    relay("Error 100"); // Send error message for two users already in the game
                } else if (map.get("Player1") == null) {
                    map.put("Player1", this);
                    out.println("PLAYER:1");
                    Games.put(game_key, map);
                    playerNumber = 1;
                    String enter = "CHAT:" + name + " (" + username + ") " + "has entered the game!";
                    Server.relayAll(game_key, enter);
                } else if (map.get("Player2") == null) {
                    out.println("PLAYER:2");
                    map.put("Player2", this);
                    Games.put(game_key, map);
                    playerNumber = 2;
                    String enter = "CHAT:" + name + " (" + username + ") " + "has entered the game!";
                    Server.relayAll(game_key, enter);
                }
                
            } else {
                relay("Error 200"); 
            }
        }
        
        private void spectate() {
            if (Games.containsKey(game_key)) {
                out.println("SUCCESS");
            } else {
                out.println("Error 300");
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
            // Parse the move string to get the column
            int column = Integer.parseInt(move);
        
            // Determine the player based on the move order
            int currentPlayer = moveOrder % 2 == 0 ? 1 : 2;
        
            // Check if it's this client's turn
            if (currentPlayer != playerNumber) {
                out.println("It's not your turn!");
                return;
            }
        
            // Find the first empty slot in the column
            int row = -1;
            for (int i = 0; i < currentBoard.length; i++) {
                if (currentBoard[i][column] == 0) {
                    row = i;
                    break;
                }
            }
        
            // If the column is full, the move is invalid
            if (row == -1) {
                return;
            }
        
            // Update the game board
            currentBoard[row][column] = currentPlayer;
        
            // Increment the move order
            moveOrder++;
        
            // Check for a win
            if (checkForWin(row, column, currentPlayer)) {
                // If win, send message to all clients
                relayAll(game_key,"RESULT:WIN:" + currentPlayer);
            } else if (checkForDraw()) {
                // If draw, send message to all clients
                relayAll(game_key,"RESULT:DRAW");
            } else {
                // If ongoing, send updated game board to all clients
                relayAll(game_key,"BOARD:" + gameBoardToString());
            }
        }
        

        private boolean checkForWin(int row, int column, int player) {
            // Check for a win in the row
            int count = 0;
            for (int j = 0; j < currentBoard[0].length; j++) {
                if (currentBoard[row][j] == player) {
                    count++;
                    if (count == 4) {
                        return true;
                    }
                } else {
                    count = 0;
                }
            }
        
            // Check for a win in the column
            count = 0;
            for (int i = 0; i < currentBoard.length; i++) {
                if (currentBoard[i][column] == player) {
                    count++;
                    if (count == 4) {
                        return true;
                    }
                } else {
                    count = 0;
                }
            }
        
            // Check for a win in the diagonal (top-left to bottom-right)
            count = 0;
            for (int i = row, j = column; i < currentBoard.length && j < currentBoard[0].length; i++, j++) {
                if (currentBoard[i][j] == player) {
                    count++;
                    if (count == 4) {
                        return true;
                    }
                } else {
                    count = 0;
                }
            }
        
            
                // Check for a win in the diagonal (top-right to bottom-left)
            count = 0;
            for (int i = row, j = column; i < currentBoard.length && j >= 0; i++, j--) {
                if (currentBoard[i][j] == player) {
                    count++;
                    if (count == 4) {
                        return true;
                    }
                } else {
                    count = 0;
                }
            }

             // No win found
             return false;
        }

        private boolean checkForDraw() {
            // The game is a draw if all cells are filled
            for (int i = 0; i < currentBoard.length; i++) {
                for (int j = 0; j < currentBoard[0].length; j++) {
                    if (currentBoard[i][j] == 0) {
                        return false;
                    }
                }
            }
            return true;
        }
        private String gameBoardToString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentBoard.length; i++) {
                for (int j = 0; j < currentBoard[0].length; j++) {
                    sb.append(currentBoard[i][j] == 0 ? " " : currentBoard[i][j]);
                    if (j < currentBoard[0].length - 1) {
                        sb.append(",");
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
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
                DatabaseIntegration.getStats(username);
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void newCommand() {
            String message = "CHAT:" + name + " has started new game!" ;
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

