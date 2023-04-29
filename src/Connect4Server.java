import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Connect4Server extends Thread {
    private final int port;
    private boolean live;
    private Map<String, Map<String, ClientConnection>> Games_Map = new HashMap<>();
    private Map<String, Map<String, ClientConnection>> Games = Collections.synchronizedMap(Games_Map);

    public Connect4Server(int port_config) {
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
            ServerSocket sock = new ServerSocket(port);
            while (live) {
                Socket socket = sock.accept();
                new ClientConnection(socket, this);
            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    class ClientConnection extends Thread {
        private static final String SYMBOLS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
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
        }

        private void newGame() {
            game_key = generateKey();
            Map<String, ClientConnection> map_unsy = new HashMap<>();
            Map<String, ClientConnection> map = Collections.synchronizedMap(map_unsy);
            map.put("Player1", this);
            map.put("Player2", null);
            Games.put(game_key, map);
            out.println("GK:" + game_key);
        }

        private void joinGame() {
            if (Games.containsKey(game_key)) {
                Map<String, ClientConnection> map_unsync = Games.get(game_key);
                Map<String, ClientConnection> map = Collections.synchronizedMap(map_unsync);
                if (map.get("Player1") != null && map.get("Player2") != null) {
                    out.println("Error 100");
                } else if (map.get("Player1") == null) {
                    map.put("Player1", this);
                } else if (map.get("Player2") == null) {
                    map.put("Player2", this);
                }
                Games.put(game_key, map);
                String enter = "CHAT:" + name + " (" + username + ") " + "has entered the game!";
                Server.relayAll(game_key, enter);
            }
            }


        private void spectate() {
            if (Games.containsKey(game_key)) {
                out.println("SUCCESS");
            } else {
                out.println("ERROR 200");
            }
        }

        private void relay(String message) {
            out.println(message);
        }

        private static String generateKey() {
            SecureRandom random = new SecureRandom();
            StringBuilder sb = new StringBuilder(8);

            for (int i = 0; i < 8; i++) {
                int randomIndex = random.nextInt(SYMBOLS.length());
                char randomChar = SYMBOLS.charAt(randomIndex);
                sb.append(randomChar);
            }
            return sb.toString();

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
            while (true) {
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
            String message = "CHAT:" + name + "Has Offered A Draw!";
            relayAll(game_key, message);
        }

        private void resignCommand() {
            String message = "CHAT:" + name + "Has Resigned!";
            relayAll(game_key, message);
        }

        private void statsCommand() {
            //PULL THESE STATS FROM DATA BASE
            String message = "CHAT:" + "My Stats";
            relayAll(game_key, message);

        }

        private void newCommand() {
            String message = "CHAT:" + name + "has left the game!";
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
