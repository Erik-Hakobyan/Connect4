import UserPackage.Connect4User;
import UserPackage.GameGUI;
import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        int port = 1234;
        new Connect4Server(port);
        new Connect4User();
        new GameGUI();
    }
}