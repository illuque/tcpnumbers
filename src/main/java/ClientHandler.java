import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class ClientHandler implements Callable<Boolean> {
    private final String name;
    private final Socket clientSocket;
    private final LinesReader linesReader;

    // TODO:I builder
    ClientHandler(String name, Socket clientSocket, LinesReader linesReader) {
        this.name = name;
        this.clientSocket = clientSocket;
        this.linesReader = linesReader;
    }

    @Override
    public Boolean call() {
        if (clientSocket.isClosed()) {
            System.out.printf("Socket is closed, cannot run client %s%s", this.name, System.lineSeparator());
            return false;
        }

        try (Scanner inputScanner = new Scanner(clientSocket.getInputStream(), StandardCharsets.UTF_8)) {
            System.out.printf("Client %s closed%s", this.name, System.lineSeparator());
            return linesReader.read(inputScanner);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}