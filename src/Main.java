public class Main {
    public static void main(String[] args) {
        int port = 1234;
        new Connect4Server(port);
        new Connect4User();
    }
}