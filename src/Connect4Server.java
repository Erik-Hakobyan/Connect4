import javax.swing.*;

public class Connect4Server {





    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {


                new Connect4Server();
            }
        });

    }
}