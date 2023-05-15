package UserPackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class InitialGUI extends Thread {
    private String name, username, game_id, server_ip;
    private CustomJTextField name_text_field, username_text_field, game_id_text_field, server_ip_text_field;
    private JTextArea statusUpdatesTextArea;
    private Connect4User Connect4UserRef;
    private JFrame frame;

    public InitialGUI(Connect4User user) {
        Connect4UserRef = user;
        start();
    }

    @Override
    public void run() {
        initialize();
    }

    public void initialize() {
        frame = new JFrame("Welcome to Connect4");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        JLabel name_label = new JLabel("Name:");

        name_label.setVerticalAlignment(SwingConstants.CENTER);
        name_label.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(name_label);

        name_text_field = new CustomJTextField(1, 50);
        name_text_field.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(name_text_field);

        JLabel username_label = new JLabel("Username: ");
        username_label.setVerticalAlignment(SwingConstants.CENTER);
        username_label.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(username_label);

        username_text_field = new CustomJTextField(1, 50);
        username_text_field.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(username_text_field);

        JLabel game_id_label = new JLabel("Game ID:");
        game_id_label.setVerticalAlignment(SwingConstants.CENTER);
        game_id_label.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(game_id_label);

        game_id_text_field = new CustomJTextField(1, 50);
        game_id_text_field.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(game_id_text_field);

        JButton newGameButton = new JButton("New Game");
        JButton joinGameButton = new JButton("Join Game");
        JButton spectateGameButton = new JButton("Spectate Game");
        JLabel server_ip_label = new JLabel("Server IP:Port");

        server_ip_label.setVerticalAlignment(SwingConstants.CENTER);
        server_ip_label.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(server_ip_label);

        server_ip_text_field = new CustomJTextField(1, 50);
        server_ip_text_field.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(server_ip_text_field);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        buttonPanel.add(newGameButton);
        buttonPanel.add(joinGameButton);
        buttonPanel.add(spectateGameButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 10));

        JPanel statusUpdatesPanel = new JPanel(new BorderLayout());
        JLabel statusUpdatesLabel = new JLabel("Status Updates:");
        statusUpdatesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusUpdatesPanel.add(statusUpdatesLabel, BorderLayout.NORTH);

        statusUpdatesTextArea = new JTextArea();
        JScrollPane statusUpdatesScrollPane = new JScrollPane(statusUpdatesTextArea);
        statusUpdatesPanel.add(statusUpdatesScrollPane, BorderLayout.CENTER);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(statusUpdatesPanel, BorderLayout.SOUTH);

        newGameButton.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initialCapture("1");    
            }
        }));
        joinGameButton.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initialCapture("2");
            }
        }));
        spectateGameButton.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initialCapture("3");
            }
        }));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void changeVisibility() {
        frame.setVisible(false);
    }

    public class CustomJTextField extends JTextField {
        private int customHeight;

        public CustomJTextField(int columns, int customHeight) {
            super(columns);
            this.customHeight = customHeight;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.height = customHeight;
            return size;
        }
    }

    public void initialCapture(String RequestType) {
        name = name_text_field.getText();
        username = username_text_field.getText();
        game_id = game_id_text_field.getText();
        server_ip = server_ip_text_field.getText();
        if (name.isEmpty()) {
            addError("Name is Missing");
        } else if (username.isEmpty()) {
            addError("Username is Missing");
        } else if (server_ip.isEmpty()) {
            addError("Server IP is Missing");
        } else {
            if (game_id.isEmpty()) {
                game_id = "NULL";
            }
            Connect4UserRef.ConnectServer(RequestType, name, username, game_id, server_ip);
        }
    }
    void addError(String error) {
        statusUpdatesTextArea.setText(error);
    }
}