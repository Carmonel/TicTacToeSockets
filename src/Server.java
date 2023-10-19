import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Vector;

import static java.lang.Integer.parseInt;

public class Server {
    static int PORT;
    static LinkedList<ClientThread> clientsList = new LinkedList<>();
    private static Socket clientSocket;
    private static ServerSocket serverSocket;
    private Vector<TicTacToe> games;

    public static void main(String[] args) throws IOException {
        System.out.println("Print port: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PORT = parseInt(br.readLine());

        serverSocket = new ServerSocket(PORT);
        System.out.println("Start Server on " + PORT + " port" + "\nWaiting for new connections...");
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Got new connection.");

                ClientThread clientThread = new ClientThread(clientSocket);
                clientsList.add(clientThread);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class TicTacToe{
    private char[][] field;
    ClientThread firstUser;
    ClientThread secondUser;
    TicTacToe(ClientThread newConnection){
        firstUser = newConnection;
        field = new char[10][10];

        if (!firstUser.sendMessageToClient("You are first gamer. You are 'X'. \nWaiting for new connection...")){
            /* error */
        }
    }
}

class ClientThread extends Thread {
    Socket socket;
    BufferedReader fromClientStream;
    PrintWriter toClientStream;
    String name;

    ClientThread(Socket s) throws IOException {
        socket = s;
        fromClientStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        toClientStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
        start();
    }
    public boolean sendMessageToClient(String message){
        try{
            toClientStream.write(message + "\n");
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public void run() {
        try {
            // Read and save username
            name = fromClientStream.readLine();
            while(true) {
                // Continuous message
                String clientMessage = fromClientStream.readLine();
                // log to server
                System.out.println("log: " + name + ": " + clientMessage);
                String[] spittedMessage = clientMessage.split(" ");

                // if @senduser
                if(spittedMessage[0].equals("@senduser")) {
                    for (ClientThread ct : Server.clientsList) {
                        if(spittedMessage[1].equals(ct.name)) {
                            ct.toClientStream.write(name+ " [private]:");
                            for (int i = 2; i < spittedMessage.length; i++) {
                                ct.toClientStream.write(" " +spittedMessage[i]);
                            }
                            ct.toClientStream.write("\n");
                            ct.toClientStream.flush();
                        }
                    }
                }else {
                    // else write to everyone
                    for (ClientThread ct : Server.clientsList) {
                        ct.toClientStream.write(name+ ": " + clientMessage + "\n");
                        ct.toClientStream.flush();
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}