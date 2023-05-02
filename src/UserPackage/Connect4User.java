package UserPackage;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Connect4User extends Thread {

    private static InitialGUI initGUI;
    private static GameGUI gameGUI;
    private static PrintWriter out_stream;
    private static BufferedReader in_stream;

    public Connect4User() {
        start();

    }

    public boolean relay(String message) {
        if (out_stream == null) {
            return false;
        } else {
            out_stream.println(message);
            return true;
        }
    }

    public void ConnectServer(String RequestType, String name, String username, String game_id, String server_ip) {
        String[] parts = server_ip.split(":");
        String ip = parts[0];
        String port = parts[1];
        String connect_message = "Authenticate" + ":" + RequestType + ":" + name + ":" + username + ":" + game_id;
        String response;
        try {
            Socket socket = new Socket(ip, Integer.parseInt(port));
            in_stream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out_stream = new PrintWriter(socket.getOutputStream(), true);
            response = in_stream.readLine();
            if (!response.contains("SUCCESS")) {
                if (response == "Error 100") {
                    initGUI.addError("Error 100: 2 Users Already In Game. Spectate Instead");
                } else if (response == "Error 200") {
                    initGUI.addError("Error 200: Game Key is invalid");
                }
            } else {
                gameGUI = new GameGUI(this);
                initGUI.changeVisibility();
                initGUI = null;

            }

        } catch (Exception e) {
            initGUI.addError("Socket Failure. Check IP:Port.");
        }

    }

    @Override
    public void run() {
        initGUI = new InitialGUI(this);
        String response;
        while (initGUI != null) {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        while (true) {
            try {
                if ((response = in_stream.readLine()) != null) {
                    processRequest(response);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }


    private void processRequest(String response) throws InterruptedException {
        if (response != null && !response.isEmpty()) {
            String[] parts = response.split(":", 2);
            if (parts.length >= 2) {
                String command = parts[0];
                String content = parts[1];

                switch (command) {
                    case "CHAT":
                        gameGUI.addChat(content);
                        break;
                    case "GK":
                        gameGUI.addChat("Your Game Key: " + content);
                        break;
                    case "PLAYER":
                        gameGUI.setPlayer(content == "1");
                        break;
                    case "STATUS":
                        gameGUI.updateStatus(content);
                        break;
                    case "MOVE":
                        gameGUI.newMove(content);
                        break;
                    case "TURN":
                        gameGUI.turn();
                        break;
                    case "RESULT":
                        gameGUI.gameResult(content);
                        break;
                    default:
                        System.out.println("Unknown command received: " + command);
                        break;
                }
            } else {
                System.out.println("Invalid response format: " + response);
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                } catch (UnsupportedLookAndFeelException e) {
                    throw new RuntimeException(e);
                }

                new Connect4User();
            }
        });
    }
}
