package ServerPackage;

public class ConnectFourGame {

    private int rows;
    private int columns;
    private int[][] board;
    private int currentPlayer;
    private String[] playerNames = new String[3]; // holds the usernames of the players

    public ConnectFourGame(int rows, int columns, String player1, String player2) {
        this.rows = rows;
        this.columns = columns;
        this.board = new int[rows][columns];
        this.currentPlayer = 1;
        this.playerNames[1] = player1;
        this.playerNames[2] = player2;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int[][] getBoard() {
        return board;
    }

    public void resetGame() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                board[row][col] = 0;
            }
        }
        currentPlayer = 1;
    }

    public boolean isCurrentPlayer(String username) {
        return playerNames[currentPlayer].equals(username);
    }

    public boolean isValidMove(int column) {
        return board[0][column] == 0;
    }

    public synchronized boolean makeMove(int column) {
        int row = findRowForColumn(column);
        if (row != -1) {
            board[row][column] = currentPlayer;
            switchPlayer();
            return true;
        }
        return false;
    }

    public String boardToString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                sb.append(board[row][col]);
                if (col < columns - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public boolean isGameOver() {
        return checkGameState() != GameState.IN_PROGRESS;
    }

    public boolean isDraw() {
        return checkGameState() == GameState.DRAW;
    }

    private void switchPlayer() {
        currentPlayer = 3 - currentPlayer;
    }

    public int findRowForColumn(int column) {
        for (int row = rows - 1; row >= 0; row--) {
            if (board[row][column] == 0) {
                return row;
            }
        }
        return -1;
    }

    public GameState checkGameState() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (board[row][col] != 0) {
                    for (int[] direction : DIRECTIONS) {
                        if (checkWin(row, col, direction)) {
                            return board[row][col] == 1 ? GameState.PLAYER_ONE_WIN : GameState.PLAYER_TWO_WIN;
                        }
                    }
                }
            }
        }

        if (isFull()) {
            return GameState.DRAW;
        }

        return GameState.IN_PROGRESS;
    }

    private boolean checkWin(int row, int col, int[] direction) {
        int count = 0;
        int newRow = row;
        int newCol = col;
        int player = board[row][col];

        while (isValidPosition(newRow, newCol) && board[newRow][newCol] == player) {
            count++;
            newRow += direction[0];
            newCol += direction[1];
        }

        return count >= 4;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < columns;
    }

    private boolean isFull() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (board[row][col] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static final int[][] DIRECTIONS = {
            {1, 0}, {0, 1}, {1, 1}, {1, -1}
    };

    public enum GameState {
        IN_PROGRESS,
        PLAYER_ONE_WIN,
        PLAYER_TWO_WIN,
        DRAW
    }
}

