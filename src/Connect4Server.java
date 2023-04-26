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
import java.util.Hashtable;

public class Connect4Server extends Thread {
    private int port;
    private boolean live;
    Hashtable<String, ClientConnection[]> Games = new Hashtable<>();

    public Connect4Server(int port_config) {
        port = port_config;
        live = true;
        start();
    }

    public void stopServer() {
        live = false;
    }

    @Override
    public void run() {
        try {
            System.out.println("Server IP Address: " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("Port: " + Integer.toString(port));
            ServerSocket sock = new ServerSocket(port);
            while (live) {
                Socket socket = sock.accept();
                ClientConnection con = new ClientConnection(socket);
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
        private int game_mode = 0;
        public String name, username;
        private String game_key;
        Socket sock;

        public ClientConnection(Socket socket) {
            sock = socket;
            if (verifyConnection()) {
                initializeMode();
            }
        }

        private void newGame() {
            game_key = generateKey();
            ClientConnection[] client_array = new ClientConnection[2];
            client_array[0] = this;
            Games.put(game_key, client_array);
            out.println("GK:" + game_key);

        }

        private void joinGame() {
            if (Games.contains(game_key)) {
                ClientConnection[] array = Games.get(game_key);
                if (array[0] != null && array[1] != null) {
                    out.println("Error 100");
                } else {
                    array[1] = this;
                    Games.put(game_key, array);
                    for (ClientConnection cc : array) {
                        cc.relay(name + "(" + username + ")" + "has entered the game!");

                    }
                }
            }

        }

        private void spectate() {
            if (Games.containsKey(game_key)) {
                //continue writing code from here
                //replace array wth hashtable for users so that it's easier to remove users
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
            switch (game_mode) {
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


        private boolean verifyConnection() {
            try {
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintStream(sock.getOutputStream());
                String line = in.readLine();
                String[] parts = line.split(",");
                if (parts[0] == "Authenticate") {
                    game_mode = Integer.parseInt(parts[1]);
                    name = parts[2];
                    username = parts[3];
                    game_key = parts[4];
                    return true;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false;

        }


        class DatabaseManager {

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
