package UserPackage;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

class GameGUI {
    private static final int ROWS = 6;
    private static final int COLUMNS = 7;
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel boardPanel;
    private JPanel controlPanel;
    private JPanel chatPanel;
    private CircleButton[][] board;
    private JButton drawButton;
    private JButton resignButton;
    private JButton newGameButton;
    private JButton statsButton;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton sendButton;

    public GameGUI() {
        create();
    }

    private void create() {

        frame = new JFrame("Connect 4");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        mainPanel = new JPanel(new BorderLayout());

        createBoardPanel();
        createControlPanel();
        createChatPanel();

        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(chatPanel, BorderLayout.EAST);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameGUI());
    }

    private void createBoardPanel() {
        boardPanel = new JPanel(new GridLayout(ROWS, COLUMNS));
        board = new CircleButton[ROWS][COLUMNS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                board[row][col] = createCircleButton();
                board[row][col].addActionListener(new CircleButtonListener(row, col));
                boardPanel.add(board[row][col]);
            }
        }
    }

    private void createControlPanel() {
        int hGap = 10; // Horizontal gap between components
        int vGap = 10; // Vertical gap between components
        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, hGap, vGap));

        Dimension buttonSize = new Dimension(120, 30); // Custom button size

        drawButton = new JButton("Offer Draw");
        drawButton.setPreferredSize(buttonSize);
        drawButton.addActionListener(e -> {
        });

        resignButton = new JButton("Resign");
        resignButton.setPreferredSize(buttonSize);
        resignButton.addActionListener(e -> {
        });

        statsButton = new JButton("My Stats");
        statsButton.setPreferredSize(buttonSize);

        newGameButton = new JButton("New Game");
        newGameButton.setPreferredSize(buttonSize);

        controlPanel.add(statsButton);
        controlPanel.add(drawButton);
        controlPanel.add(resignButton);
        controlPanel.add(newGameButton);
        controlPanel.add(new JLabel("Game Status:"));

        JTextArea statusUpdatesTextArea = new JTextArea();
        statusUpdatesTextArea.append("Waiting");
        controlPanel.add(statusUpdatesTextArea);
    }

    private void createChatPanel() {
        chatPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setPreferredSize(new Dimension(300, 450));

        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String message = chatInput.getText().trim();
            if (!message.isEmpty()) {
                // Implement logic for sending the chat message
                chatInput.setText("");
            }
        });

        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
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
        return button;
    }

    private class CircleButton extends JButton {
        public CircleButton() {
            super();
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            int maxSize = Math.max(size.width, size.height);
            return new Dimension(maxSize, maxSize);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isArmed()) {
                g.setColor(Color.LIGHT_GRAY);
            } else {
                g.setColor(getBackground());
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
            return new Ellipse2D.Float(0, 0, getWidth(), getHeight()).contains(x, y);
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

        }
    }
}

