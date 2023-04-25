import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        InitialGUI();
    }

    private void InitialGUI() {
        int rightPadding = 20;
        JFrame frame = new JFrame("Welcome to Connect4");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(4, 5));
        JLabel name_label = new JLabel("Name:");

        name_label.setVerticalAlignment(SwingConstants.CENTER);
        name_label.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(name_label);
        name_text_field = new JTextField();
        name_text_field.setHorizontalAlignment(SwingConstants.CENTER);
        name_text_field.setBorder(BorderFactory.createCompoundBorder(
                name_text_field.getBorder(),
                BorderFactory.createEmptyBorder(0, 0, 0, rightPadding)));
        inputPanel.add(name_text_field);
        JLabel username_label = new JLabel("Username: ");
        username_label.setVerticalAlignment(SwingConstants.CENTER);
        username_label.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(username_label);
        username_text_field = new JTextField();
        username_text_field.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(username_text_field);
        JLabel game_id_label = new JLabel("Game ID:");
        game_id_label.setVerticalAlignment(SwingConstants.CENTER);
        game_id_label.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(game_id_label);
        game_id_text_field = new JTextField();
        game_id_text_field.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(game_id_text_field);
        JButton newGameButton = new JButton("New Game");
        JButton joinGameButton = new JButton("Join Game");
        JButton spectateGameButton = new JButton("Spectate Game");
        JLabel server_ip_label = new JLabel("Server IP:Port");
        server_ip_label.setVerticalAlignment(SwingConstants.CENTER);
        server_ip_label.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(server_ip_label);
        server_ip_text_field = new JTextField();
        server_ip_text_field.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(server_ip_text_field);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        buttonPanel.add(newGameButton);
        buttonPanel.add(joinGameButton);
        buttonPanel.add(spectateGameButton);


        name_text_field.setBorder(BorderFactory.createCompoundBorder(
                name_text_field.getBorder(),
                BorderFactory.createEmptyBorder(0, 0, 0, rightPadding)));
        username_text_field.setBorder(BorderFactory.createCompoundBorder(
                username_text_field.getBorder(),
                BorderFactory.createEmptyBorder(0, 0, 0, rightPadding)));
        game_id_text_field.setBorder(BorderFactory.createCompoundBorder(
                game_id_text_field.getBorder(),
                BorderFactory.createEmptyBorder(0, 0, 0, rightPadding)));
        server_ip_text_field.setBorder(BorderFactory.createCompoundBorder(
                server_ip_text_field.getBorder(),
                BorderFactory.createEmptyBorder(0, 0, 0, rightPadding)));


        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        frame.add(inputPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        newGameButton.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initialCapture();
            }
        }));
        joinGameButton.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initialCapture();
            }
        }));
        spectateGameButton.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initialCapture();
            }
        }));
        // Display the window
        frame.setLocationRelativeTo(null); // Center the window on the screen
        frame.setVisible(true);

    }

    public void initialCapture() {
        name = name_text_field.getText();
        username = username_text_field.getText();
        game_id = game_id_text_field.getText();
        server_ip = server_ip_text_field.getText();
        ConnectServer(name, username, game_id, server_ip);
    }

    public static void ConnectServer(String name, String username, String game_id, String server_ip) {
        String[] parts = server_ip.split(":");
        String ip = parts[0];
        String port = parts[1];
        try {
            Socket socket = new Socket(ip, Integer.parseInt(port));
            BufferedReader in_stream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out_stream = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
