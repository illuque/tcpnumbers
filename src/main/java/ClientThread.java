import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientThread implements Runnable {

    private final String name;
    private final Socket test;

    // TODO:I builder
    ClientThread(String name, Socket test) {
        this.name = name;
        this.test = test;
    }

    public void run() {
        System.out.printf("New client %s set up\n", name);
        processInput(test);
    }

    private void processInput(Socket clientSocket) {
        try (Scanner inputScanner = new Scanner(clientSocket.getInputStream(), StandardCharsets.UTF_8);
             PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true)) {

            boolean terminateReceived = false;
            while (!terminateReceived && inputScanner.hasNextLine()) {
                String line = inputScanner.nextLine();
                System.out.printf("\tReceived from client %s input: %s\n", this.name, line);
                outputWriter.println(line);
            }

            System.out.printf("Client %s closed\n", this.name);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}