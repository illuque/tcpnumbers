import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TcpServer {

    private final List<Socket> allClientSockets;

    private final List<Future<Boolean>> allClientFutures;

    private final ExecutorService executor;

    private final ServerSocket serverSocket;

    private final int maxClients;

    private final LinesReader linesReader;

    public static TcpServer create(int port, int maxClients, LinesReader linesReader) {
        return new TcpServer(port, maxClients, linesReader);
    }

    private TcpServer(int port, int maxClients, LinesReader linesReader) {
        this.maxClients = maxClients;

        this.allClientFutures = new CopyOnWriteArrayList<>();
        this.allClientSockets = new ArrayList<>();

        this.serverSocket = createSocket(port);
        this.executor = Executors.newFixedThreadPool(maxClients);

        this.linesReader = linesReader;
    }

    public void start() {
        try {
            initClientStatusListenerThread();
            initClientConnectionsListenerThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServerSocket createSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return serverSocket;
    }

    private void initClientStatusListenerThread() {
        Thread statusListenerThread = new Thread(() -> {
            while (true) {
                if (anyClientForcedTermination()) return;
            }
        });

        statusListenerThread.start();
    }

    private void initClientConnectionsListenerThread() throws IOException {
        int i = 0;
        while (true) {
            i++;
            String clientName = "#" + i;

            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (SocketException se) {
                System.out.printf("Server was closed by one of the clients%s", System.lineSeparator());
                return;
            }

            if (allClientFutures.size() == maxClients) {
                rejectClient(clientSocket);
            } else {
                acceptClient(clientName, clientSocket);
            }
        }
    }

    private boolean anyClientForcedTermination() {
        for (Future<Boolean> f : allClientFutures) {
            try {
                boolean terminationReceived = f.isDone() && f.get();
                if (terminationReceived) {
                    shutDown();
                    return true;
                }
                if (f.isDone()) {
                    allClientFutures.remove(f);
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void rejectClient(Socket clientSocket) {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptClient(String clientName, Socket clientSocket) {
        ClientHandler clientHandler = ClientHandler.create(clientName, clientSocket, linesReader);

        allClientSockets.add(clientSocket);
        allClientFutures.add(executor.submit(clientHandler));
    }

    public synchronized void shutDown() throws IOException {
        executor.shutdown();

        for (Socket clientSocket : allClientSockets) {
            clientSocket.close();
        }

        serverSocket.close();
    }

}
