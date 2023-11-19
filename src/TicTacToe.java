import java.io.IOException;
import java.util.Objects;
public class TicTacToe implements Runnable {
    private static char[][] field;
    private static PlayerThread player1;
    private static PlayerThread player2;
    private static boolean gameFinished;
    public TicTacToe(PlayerThread p1, PlayerThread p2){
        player1 = p1;
        player2 = p2;
        gameFinished = false;

        field = new char[10][];
        for (int i = 0; i < 10; i++) field[i] = new char[10];
        for (int i = 0; i < 10; i++) for (int j = 0; j < 10; j++) field[i][j] = ' ';

        player1.toPlayerStream.write("You are first player. You are 'X'.");
        player2.toPlayerStream.write("You are second player. You are 'Y'.");
    }

    @Override
    public void run() {
        player1.toPlayerStream.write("Game started!");
        player2.toPlayerStream.write("Game started!");

        // Поток для проверки поля на победителя
        Thread fieldChecker = new Thread(TicTacToe::checkTable);
        fieldChecker.start();

        try{
            while (!gameFinished) {
                // Попытка поймать правильное сообщение
                String[] messageFromPlayer1 = messageFromPlayer(player1);
                // Ловим ошибку
                while (!Objects.equals(messageFromPlayer1[0], "@error")) {
                    messageFromPlayer1 = messageFromPlayer(player1);
                }

                // Случай "игрок сдался"
                if (messageFromPlayer1[0].equals("@exit")) {
                    player2.toPlayerStream.write("First player left. You won!");
                    /* some code */
                }
                // Случай "игрок делает ход"
                else {
                    int x = Integer.parseInt(messageFromPlayer1[1]);
                    int y = Integer.parseInt(messageFromPlayer1[2]);

                    if (field[x][y] == ' ') field[x][y] = 'X';
                    else player1.toPlayerStream.write("Spot already used.");
                }

                printFieldToPlayers();

                ///
                /// Again for 2nd player
                ///
                // Trying catch right message
                String[] messageFromPlayer2 = messageFromPlayer(player2);
                while (!Objects.equals(messageFromPlayer2[0], "@error")) {
                    messageFromPlayer2 = messageFromPlayer(player2);
                }

                if (messageFromPlayer2[0].equals("@exit")) {
                    player1.toPlayerStream.write("Second player left. You won!");
                    /* some code */
                }
                // If "@step"
                else {
                    int x = Integer.parseInt(messageFromPlayer2[1]);
                    int y = Integer.parseInt(messageFromPlayer2[2]);

                    if (field[x][y] == ' ') field[x][y] = 'O';
                    else player2.toPlayerStream.write("Spot already used.");
                }

                printFieldToPlayers();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkTable(){
        for (int i = 0; i < field.length; i++){
            // Горизонтальная проверка
            if (field[i][0] != ' '){
                boolean checker = true;
                for (int j = 1; j < field.length; j++){
                    if (field[i][0] != field[i][j]){
                        checker = false;
                        break;
                    }
                }
                if (checker) finishGame(field[i][0]);
            }
            // Вертикальная проверка
            if (field[0][i] != ' '){
                boolean checker = true;
                for (int j = 1; j < field.length; j++){
                    if (field[0][i] != field[j][i]){
                        checker = false;
                        break;
                    }
                }
                if (checker) finishGame(field[i][0]);
            }
        }

        // Диагонали
        boolean checker = true;
        // Проверка нисходящей диагонали
        if (field[0][0] != ' '){
            for (int i = 1; i < field.length; i++){
                if (field[i][i] != field[0][0]){
                    checker = false;
                    break;
                }
            }
        }
        if (checker) finishGame(field[0][0]);
        // Проверка восходящей диагонали
        checker = true;
        if (field[0][field.length] != ' '){
            for (int i = 1; i < field.length; i++){
                if (field[i][field.length-i] != field[0][field.length]){
                    checker = false;
                    break;
                }
            }
        }
        if (checker) finishGame(field[0][field.length]);
    }
    private static void finishGame(char winner){
        gameFinished = true;

        if (winner == 'X'){
            player1.toPlayerStream.write("You won!");
            player2.toPlayerStream.write("You lose!");
        }
        if (winner == 'O'){
            player2.toPlayerStream.write("You won!");
            player1.toPlayerStream.write("You lose!");
        }
        else{
            System.out.println("TicTacToe::finishGame(): char winner == ???");
        }
    }

    private void printFieldToPlayers(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n-----------------------------------------");
        for (int i = 0; i < 10; i++){
            for (int j = 0; j < 10; j++){
                sb.append("| " + field[i][j] + " ");
            }
            sb.append("|");
            sb.append("\n-----------------------------------------");
        }
        player1.toPlayerStream.write(sb.toString());
        player2.toPlayerStream.write(sb.toString());
    }

    // Интерпретация сообщения от игрока в образцы сообщений (exit/error/step)
    private String[] messageFromPlayer(PlayerThread player) throws IOException {
        String[] message = player.fromClientStream.readLine().split(" ");

        // "@exit"
        if (Objects.equals(message[0], "@exit")){
            String[] returnArray = new String[1];
            returnArray[0] = "@exit";
            return returnArray;
        }
        // "@step"
        else if (Objects.equals(message[0], "@step")){
            // удаляем лишнее
            if (message.length > 2){
                String[] returnArray = new String[3];
                returnArray[0] = message[0];
                returnArray[1] = message[1];
                returnArray[2] = message[2];
                return returnArray;
            } else return returnError();
        } else return returnError();
    }

    // Error String[]
    private String[] returnError(){
        String[] returnArray = new String[1];
        returnArray[0] = "@error";
        return returnArray;
    }
}
