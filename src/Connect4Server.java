import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class Connect4Server extends Thread{
    private int port;
    private boolean live;
    Hashtable<Integer, ClientConnection> Games = new Hashtable<>();

    public Connect4Server(int port_config){
        port = port_config;
        live = true;
        start();
    }

    public void stopServer(){
        live = false;
    }
    @Override
    public void run(){
        try {
            System.out.println("Server IP Address: "  + InetAddress.getLocalHost().getHostAddress());
            System.out.println("Port: " + Integer.toString(port));
            ServerSocket sock = new ServerSocket(port);
            while (live){
                Socket socket = sock.accept();
                ClientConnection con = new ClientConnection();
            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    class ClientConnection {
        public ClientConnection() {
            start();
        }

        private void newGame() {
        }

        private void joinGame() {
        }

        private void spectate() {
        }

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