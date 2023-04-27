package UserPackage;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Connect4User {
    private String name, username, game_id, server_ip;
    private JTextField name_text_field, username_text_field, game_id_text_field, server_ip_text_field;
    private BufferedReader in_stream;
    private PrintWriter out_Stream;

    public Connect4User() {
        initGUI = new InitialGUI();
    }

    public static void ConnectServer(int RequestType, String name, String username, String game_id, String server_ip) {
        String[] parts = server_ip.split(":");
        String ip = parts[0];
        String port = parts[1];
        String connect_message = "Authenticate" + ":" + name + ":" + username + ":" + game_id;
        String response;
        try {
            Socket socket = new Socket(ip, Integer.parseInt(port));
            BufferedReader in_stream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out_stream = new PrintWriter(socket.getOutputStream(), true);
            out_stream.println(connect_message);
            response = in_stream.readLine();
            if (response != "SUCCESS") {
                if (response == "Error 100") {
                    initGUI.addError("Error 100: 2 Users Already In Game. Spectate Instead");
                } else if (response == "Error 200") {
                    initGUI.addError("Error 200: Game Key is invalid");
                }
            } else {
                gameGUI = new GameGUI();
            }

        } catch (Exception e) {
            initGUI.addError("Socket Failure. Check IP:Port.");
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
