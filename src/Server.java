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

        // Поток для удаления завершенных игр
        Thread deleteGamesAfterEnding = new Thread(Server::deleteGamesAfterEnding);
        deleteGamesAfterEnding.setDaemon(true);
        deleteGamesAfterEnding.start();

        while(true) {
            // Waiting for new connection
            try {
                // Новый клиент
                playerSocket = serverSocket.accept();
                PlayerThread clientThread = new PlayerThread(playerSocket);
                removeClientWithSameName(clientThread.name);
                playerList.add(clientThread);
                System.out.println("Got new connection. Players in queue: " + playerList.size());

                // Создание игр, пока кол-во игроков больше 1
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
    private static void deleteGamesAfterEnding(){
        while (true){
            for (Thread thread : games) {
                if (!thread.isAlive()) {
                    games.remove(thread);
                    System.out.println("Game has finished.");
                }
            }
        }
    }
}