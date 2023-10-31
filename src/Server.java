import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Server {
    final static int PORT = 10001;
    static LinkedList<PlayerThread> playerList = new LinkedList<>();
    private static Socket playerSocket;
    private static ServerSocket serverSocket;
    private static LinkedList<Thread> games = new LinkedList<>();

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Start Server on " + PORT + " port");
        while(true) {
            // Waiting for new connection
            try {
                playerSocket = serverSocket.accept();

                // Creating new client
                PlayerThread clientThread = new PlayerThread(playerSocket);
                // Удаление старого клиента с тем же никнеймом
                removeClientWithSameName(clientThread.name);
                playerList.add(clientThread);

                System.out.println("Got new connection. Players in queue: " + playerList.size());
                // Creating new games until size() != 0 OR 1
                while (playerList.size() > 1){
                    Thread newGame = new Thread(new TicTacToe(playerList.get(0), playerList.get(1)));
                    games.add(newGame);
                    games.get(games.size()-1).start();
                    playerList.remove(0);
                    playerList.remove(1);
                    System.out.println("New game started. Players in queue: " + playerList.size());
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static void removeClientWithSameName(String name) {
        for (PlayerThread clientThread : playerList) {
            if (clientThread.name != null && clientThread.name.equals(name)) {
                playerList.remove(clientThread);
                return;
            }
        }
    }
}