import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Client {
    static String clientName;
    static Socket clientSocket;
    static PrintWriter toServerStream;
    static BufferedReader fromServerStream;
    static BufferedReader fromClientStream;

    public static void main(String[] args) throws IOException {
        clientSocket = new Socket("localhost", 10001);
        toServerStream = new PrintWriter(clientSocket.getOutputStream(), true);
        fromServerStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        fromClientStream = new BufferedReader(new InputStreamReader(System.in));

        setNickname();

        // From server to client
        new Thread(() -> {
            while(true) {
                try {
                    String message = fromServerStream.readLine();
                    System.out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Send message to server
        while(true) {
            String userMessage;
            userMessage = fromClientStream.readLine();
            toServerStream.write(userMessage + "\n");
            toServerStream.flush();
        }
    }
    private static void setNickname() throws IOException {
        System.out.print("Press your nick: ");
        clientName = fromClientStream.readLine();
        toServerStream.write(clientName + "\n");
        toServerStream.flush();
    }
}