import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// Server class
class Application {

    public static final int PORT = 4000;

    public static final int MAX_THREADS = 5;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);

        int i = 0;
        //noinspection InfiniteLoopStatement
        while (true) {
            i++;
            String clientName = "#" + i;
            Socket test = serverSocket.accept();

            ClientThread clientThread = new ClientThread(clientName, test);
            executor.execute(clientThread);
        }
    }
}
