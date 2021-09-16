import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.*;

// TODO:I Test
public class Server {

    private static final int TIMEOUT_FOR_SHUTDOWN = 5;

    private final ExecutorService clientExecutorService;

    private final ServerSocket serverSocket;

    private final int maxClients;

    private final List<Client> activeClients;

    private final LinesReader linesReader;

    public static Server create(int port, int maxClients, LinesReader linesReader) {
        return new Server(port, maxClients, linesReader);
    }

    private Server(int port, int maxClients, LinesReader linesReader) {
        this.maxClients = maxClients;

        this.serverSocket = createSocket(port);
        this.clientExecutorService = Executors.newFixedThreadPool(maxClients);

        this.linesReader = linesReader;

        this.activeClients = new CopyOnWriteArrayList<>();
    }

    public void start() {
        try {
            initClientStatusListenerThread();
            initClientConnectionsListenerThread();

            boolean allFinished = clientExecutorService.awaitTermination(TIMEOUT_FOR_SHUTDOWN, TimeUnit.SECONDS);
            if (!allFinished) {
                System.err.println("Not all Clients finished gracefully");
            }
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
                for (Client client : activeClients) {
                    if (clientIsDone(client)) {
                        activeClients.remove(client);
                    }
                    if (clientForcedTermination(client)) {
                        shutDown();
                        return;
                    }
                }
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

            boolean maxClientsReached = activeClients.size() == maxClients;
            if (maxClientsReached) {
                rejectClient(clientSocket);
            } else {
                acceptClient(clientName, clientSocket);
            }
        }
    }

    private boolean clientIsDone(Client client) {
        try {
            return client.isDone();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean clientForcedTermination(Client client) {
        try {
            if (client.receivedTerminationFlag()) {
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
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
        Client client = Client.create(clientName, clientSocket, linesReader);
        client.schedule(clientExecutorService);
        activeClients.add(client);
    }

    public synchronized void shutDown() {
        try {
            // stop receiving new tasks
            clientExecutorService.shutdown();

            // disconnect server => will stop receiving messages
            serverSocket.close();

            for (Client client : activeClients) {
                client.disconnect();
            }

            activeClients.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
