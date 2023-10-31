import java.io.*;
import java.net.Socket;
import java.util.Objects;

class PlayerThread extends Thread {
    Socket socket;
    BufferedReader fromClientStream;
    PrintWriter toPlayerStream;
    String name;
    PlayerThread(Socket s) throws IOException {
        socket = s;
        // in and out streams
        fromClientStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        toPlayerStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
        start();
    }

    public void run() {
        try {
            // Read and save username
            name = fromClientStream.readLine();
            while(true) {
                // Continuous message
                String clientMessage = fromClientStream.readLine();
                // Log to server
                System.out.println("log: " + name + ": " + clientMessage);
                String[] spittedMessage = clientMessage.split(" ");

                // if @step
                if(spittedMessage[0].equals("@step")) {
                    for (PlayerThread ct : Server.playerList) {
                        if(spittedMessage[1].equals(ct.name)) {
                            ct.toPlayerStream.write(name+ " [private]:");
                            for (int i = 2; i < spittedMessage.length; i++) {
                                ct.toPlayerStream.write(" " +spittedMessage[i]);
                            }
                            ct.toPlayerStream.write("\n");
                            ct.toPlayerStream.flush();
                        }
                    }
                } else {
                    // else write to everyone
                    for (PlayerThread ct : Server.playerList) {
                        if (!Objects.equals(ct.name, this.name)){
                            ct.toPlayerStream.write(name+ ": " + clientMessage + "\n");
                            ct.toPlayerStream.flush();
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        } finally {
            // Удаление клиента из списка при завершении соединения
            Server.playerList.remove(this);
            // Закрытие сокета
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}