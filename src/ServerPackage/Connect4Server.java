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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
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
        private BufferedReader in;
        private PrintStream out;
        private int requestType;
        public String name, username;
        private String game_key;
        private final Connect4Server Server;
        Socket sock;
        private Map<String, Boolean> isTurn = new HashMap<>();
        
        public ClientConnection(Socket socket, Connect4Server server) throws ClassNotFoundException, SQLException {
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
            isTurn.put(this.username, true);    
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
                    String enter = "CHAT:" + name + " (" + username + ") " + "has entered the game!";
                    Server.relayAll(game_key, enter);
                } else if (map.get("Player2") == null) {
                    out.println("PLAYER:2");
                    map.put("Player2", this);
                    Games.put(game_key, map);
                    isTurn.put(this.username, false);
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

        private void relay(String message) {
            out.println(message);
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

        private void verifyConnection() throws ClassNotFoundException, SQLException {
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
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processRequest(String request) throws ClassNotFoundException, SQLException {
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

        private void validateMove(String move) throws ClassNotFoundException, SQLException {
            int column = Integer.parseInt(move);
            int currentPlayer = moveOrder % 2 == 0 ? 1 : 2;
            if (!isTurn.get(this.username)) {
                out.println("It's not your turn!");
                return;
            }
 
            int row = -1;
            for (int i = 0; i < currentBoard.length; i++) {
                if (currentBoard[i][column] == 0) {
                    row = i;
                    break;
                }
            }
        
            if (row == -1) {
                return;
            }
            currentBoard[row][column] = currentPlayer;
            moveOrder++;

            if (checkForWin(row, column, currentPlayer)) {
                relayAll(game_key,"RESULT:WIN:" + currentPlayer);
            } else if (checkForDraw()) {
                relayAll(game_key,"RESULT:DRAW");
            } else {
                relayAll(game_key,"BOARD:" + gameBoardToString());
            }
            endTurn();
        }
        
        public void endTurn() {
            for (String username : isTurn.keySet()) {
                isTurn.put(username, !isTurn.get(username)); 
            }
        }
        
        public String getCurrentTurn() {
            for (Map.Entry<String, Boolean> entry : isTurn.entrySet()) {
                if (entry.getValue()) {  
                    return entry.getKey();
                }
            }
            return null;  
        }

        private boolean checkForWin(int row, int column, int player) throws ClassNotFoundException, SQLException {
            // Check row
            int count = 0;
            for (int j = 0; j < currentBoard[0].length; j++) {
                if (currentBoard[row][j] == player) {
                    count++;
                    if (count == 4) {
                        String winnerUsername = getWinnerUsername(player);
                        DatabaseIntegration.updateStats(winnerUsername, true, false);
                        String loserUsername = null;
                        if (player == 1) {
                            loserUsername = getWinnerUsername(2);  
                        } else if (player == 2) {
                            loserUsername = getWinnerUsername(1);  
                        }
                        if (loserUsername != null) {
                            DatabaseIntegration.updateStats(loserUsername, false, false);
                        }
                        return true;
                    }
                } else {
                    count = 0;
                }
            }
        
            // Check column
            count = 0;
            for (int i = 0; i < currentBoard.length; i++) {
                if (currentBoard[i][column] == player) {
                    count++;
                    if (count == 4) {
                        String winnerUsername = getWinnerUsername(player);
                        DatabaseIntegration.updateStats(winnerUsername, true, false);
                        String loserUsername = null;
                        if (player == 1) {
                            loserUsername = getWinnerUsername(2);  
                        } else if (player == 2) {
                            loserUsername = getWinnerUsername(1);  
                        }
                        if (loserUsername != null) {
                            DatabaseIntegration.updateStats(loserUsername, false, false);
                        }                       
                        return true;  
                    }
                } else {
                    count = 0;
                }
            }
        
            // Check diagonal (top-left to bottom-right)
            count = 0;
            for (int i = row, j = column; i < currentBoard.length && j < currentBoard[0].length; i++, j++) {
                if (currentBoard[i][j] == player) {
                    count++;
                    if (count == 4) {
                        String winnerUsername = getWinnerUsername(player);
                        DatabaseIntegration.updateStats(winnerUsername, true, false);
                        String loserUsername = null;
                        if (player == 1) {
                            loserUsername = getWinnerUsername(2);  
                        } else if (player == 2) {
                            loserUsername = getWinnerUsername(1);  
                        }
                        if (loserUsername != null) {
                            DatabaseIntegration.updateStats(loserUsername, false, false);
                        } 
                        return true;
                    }
                } else {
                    count = 0;
                }
            }
        
                // Check other diagonal (top-right to bottom-left)
            count = 0;
            for (int i = row, j = column; i < currentBoard.length && j >= 0; i++, j--) {
                if (currentBoard[i][j] == player) {
                    count++;
                    if (count == 4) {
                        String winnerUsername = getWinnerUsername(player);
                        DatabaseIntegration.updateStats(winnerUsername, true, false);
                        String loserUsername = null;
                        if (player == 1) {
                            loserUsername = getWinnerUsername(2);  
                        } else if (player == 2) {
                            loserUsername = getWinnerUsername(1); 
                        }
                        if (loserUsername != null) {
                            DatabaseIntegration.updateStats(loserUsername, false, false);
                        }       
                        return true;
                    }
                } else {
                    count = 0;
                }
            }
             return false;
        }

        private String getWinnerUsername(int player) {
            Map<String, ClientConnection> game = Games.get(game_key);
            String winnerUsername = null;
            if (player == 1 && game.get("Player1") != null) {
                winnerUsername = game.get("Player1").username;
            } else if (player == 2 && game.get("Player2") != null) {
                winnerUsername = game.get("Player2").username;
            }
            return winnerUsername;
        }

        private boolean checkForDraw() {
            for (int i = 0; i < currentBoard.length; i++) {
                for (int j = 0; j < currentBoard[0].length; j++) {
                    if (currentBoard[i][j] == 0) {
                        return false;
                    }
                }
            }
            try {
                Map<String, ClientConnection> aGame = Games.get(game_key);
                if (aGame != null) {
                    for (Map.Entry<String, ClientConnection> entry : aGame.entrySet()) {
                        String username = entry.getKey();
                        DatabaseIntegration.updateStats(username, false, true);
                    }
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
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
            try {
                Map<String, ClientConnection> aGame = Games.get(game_key);
                if (aGame != null) {
                    for (Map.Entry<String, ClientConnection> entry : aGame.entrySet()) {
                        String username = entry.getKey();
                        DatabaseIntegration.updateStats(username, false, true);
                    }
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }

        private void resignCommand() {
            String message = "CHAT:" + name + " Has Resigned!";
            Server.relayAll(game_key, message);
            String opponentName = null;
            for (Map.Entry<String, Boolean> entry : isTurn.entrySet()) {
                if (!entry.getValue()) {  
                    opponentName = entry.getKey();
                    break;
                }
            }
            if (opponentName == null) {
                System.out.println("Opponent not found");
                return;
            }
        
            try {
                DatabaseIntegration.updateStats(name, false, false);
                DatabaseIntegration.updateStats(opponentName, true, false);
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
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

