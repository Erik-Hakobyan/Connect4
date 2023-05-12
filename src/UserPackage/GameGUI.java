package UserPackage;

import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

public class GameGUI {
    private static final int ROWS = 6;
    private static final int COLUMNS = 7;
    private int move_counter;
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel boardPanel;
    private JPanel controlPanel;
    private JPanel statusPanel;
    private JPanel chatPanel;
    private CircleButton[][] buttonBoard;
    private JButton drawButton;
    private JButton resignButton;
    private JButton newGameButton;
    private JButton statsButton;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton sendButton;
    private JTextArea statusUpdatesTextArea;
    private String status;
    private Connect4User user;
    private boolean isPlayerOne = true;
    private boolean isMyTurn = false;

    public GameGUI(Connect4User u) {
        user = u;
        try {
            UIManager.setLookAndFeel(new FlatCyanLightIJTheme());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        create();
    }

    public void setPlayer(Boolean player) {
        isPlayerOne = player;
    }

    private void create() {
        status = "Waiting";
        frame = new JFrame("Connect 4");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        mainPanel = new JPanel(new BorderLayout());

        createBoardPanel();
        createControlPanel();
        createChatPanel();

        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(chatPanel, BorderLayout.EAST);

        frame.add(mainPanel);
        frame.setVisible(true);
    }


    private void createBoardPanel() {
        boardPanel = new JPanel(new GridLayout(ROWS, COLUMNS));
        buttonBoard = new CircleButton[ROWS][COLUMNS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                buttonBoard[row][col] = createCircleButton();
                buttonBoard[row][col].addActionListener(new CircleButtonListener(row, col));
                boardPanel.add(buttonBoard[row][col]);
            }
        }
    }

    private void resetBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if(buttonBoard[row][col] == null){
                    buttonBoard[row][col] = createCircleButton();
                }else{
                    buttonBoard[row][col].setBackground(Color.WHITE);
                }
            }
        }
        isMyTurn = isPlayerOne;
    }

    public void gameResult(String result) {
        //PROCESS THIS
        //WINNER/LOSER - Create a gui or something or post the results. freeze the board, etc.
    }

    public void turn() {
        isMyTurn = true;
    }

    public void updateStatus(String newStatus) {
        status = newStatus;
    }

    public void newMove(String new_move) {
        System.out.println("newMove called with move: " + new_move);
        if (new_move == null || new_move.length() != 2) {
            addChat("SYSTEM: AN ERROR OCCURRED");
        }

        int row = Character.getNumericValue(new_move.charAt(0));
        int column = Character.getNumericValue(new_move.charAt(1));

        if (row >= 0 && row < buttonBoard.length && column >= 0 && column < buttonBoard[0].length) {
            Color playerColor = !isPlayerOne ? Color.RED : Color.YELLOW;
            buttonBoard[row][column].setBackground(playerColor);
        } else {
            addChat("SYSTEM: AN ERROR OCCURRED");
        }
    }


    private void createControlPanel() {
        int hGap = 10;
        int vGap = 10;
        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, hGap, vGap));

        Dimension buttonSize = new Dimension(120, 30);

        drawButton = new JButton("Offer Draw");
        drawButton.setPreferredSize(buttonSize);
        drawButton.addActionListener(e -> {
            relayMessage("COMMAND:DRAW");
        });

        resignButton = new JButton("Resign");
        resignButton.setPreferredSize(buttonSize);
        resignButton.addActionListener(e -> {
            relayMessage("COMMAND:RESIGN");
        });

        statsButton = new JButton("My Stats");
        statsButton.setPreferredSize(buttonSize);
        statsButton.addActionListener(e ->
                relayMessage("COMMAND:STATS"));

        newGameButton = new JButton("New Game");
        newGameButton.setPreferredSize(buttonSize);
        newGameButton.addActionListener(e -> {
            relayMessage("COMMAND:NEW");
            resetBoard();
        });

        controlPanel.add(statsButton);
        controlPanel.add(drawButton);
        controlPanel.add(resignButton);
        controlPanel.add(newGameButton);
        controlPanel.add(new JLabel("Game Status:"));

        statusUpdatesTextArea = new JTextArea();
        statusUpdatesTextArea.append(status);
        controlPanel.add(statusUpdatesTextArea);
    }

    private void relayMessage(String message) {
        if (!user.relay(message)) {
            statusUpdatesTextArea.append(("Unable to Relay Message"));
        }
    }

    private void createChatPanel() {
        chatPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setPreferredSize(new Dimension(300, frame.getHeight()));

        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String message = chatInput.getText().trim();
            if (!message.isEmpty()) {
                user.relay("CHAT" + ":" + message);
                chatInput.setText("");
            }
        });

        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
    }

    public void addChat(String message) {
        chatArea.append(message + "\n");
    }

    private CircleButton createCircleButton() {
        CircleButton button = new CircleButton();
        button.setBackground(Color.WHITE);
        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(button.getBackground());
                g2.fillOval(0, 0, c.getWidth() - 1, c.getHeight() - 1);
                g2.setColor(Color.BLACK);
                g2.drawOval(0, 0, c.getWidth() - 1, c.getHeight() - 1);
                g2.dispose();
            }
        });
        button.setRolloverEnabled(true);
        return button;
    }

    public class CircleButton extends JButton {
        private boolean isDisabled;

        public CircleButton() {
            super();
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(true);
            setOpaque(false);
            isDisabled = false;
            setEnabled(true);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            isDisabled = !enabled;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            int maxSize = Math.max(size.width, size.height);
            return new Dimension(maxSize, maxSize);
        }

        @Override
        protected void paintComponent(Graphics g) {
            ButtonModel model = getModel();

            if (model.isPressed()) {
                g.setColor(Color.DARK_GRAY);
            } else if (model.isRollover()) {
                g.setColor(Color.LIGHT_GRAY);
            } else {
                g.setColor(isDisabled ? Color.GRAY : getBackground());
            }
            g.fillOval(0, 0, getSize().width - 1, getSize().height - 1);

            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            g.setColor(getForeground());
            g.drawOval(0, 0, getSize().width - 1, getSize().height - 1);
        }

        @Override
        public boolean contains(int x, int y) {
            return !isDisabled && new Ellipse2D.Float(0, 0, getWidth(), getHeight()).contains(x, y);
        }
    }

    private class CircleButtonListener implements ActionListener {
        private int row;
        private int column;

        public CircleButtonListener(int row, int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isMyTurn) {
                isMyTurn = false;
                int targetRow = findAvailableRow(column);
                if (targetRow != -1) {
                    Color playerColor = isPlayerOne ? Color.RED : Color.YELLOW;
                    buttonBoard[targetRow][column].setBackground(playerColor);
                    relayMessage("MOVE:" + Integer.toString(targetRow) + Integer.toString(column));
                    
                }
            } else {
                chatArea.append("SYSTEM: It's not your turn! \n");
            }
        }

        private int findAvailableRow(int column) {
            for (int row = ROWS - 1; row >= 0; row--) {
                if (buttonBoard[row][column].getBackground() == Color.WHITE) {
                    return row;
                }
            }
            return -1;
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameGUI(null));
    }
}