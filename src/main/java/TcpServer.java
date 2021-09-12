import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;

public class TcpServer {

    private static final String TERMINATION_SEQUENCE = "terminate";

    private final List<Socket> allClientSockets;

    private final List<Future<Boolean>> allClientFutures;

    private final Set<Integer> uniqueNumbersSet;

    private final ExecutorService executor;

    private final ServerSocket serverSocket;

    private final int maxClients;

    public static TcpServer create(int port, int maxClients) {
        return new TcpServer(port, maxClients);
    }

    private TcpServer(int port, int maxClients) {
        this.maxClients = maxClients;

        this.uniqueNumbersSet = Collections.synchronizedSet(new HashSet<>());

        this.allClientFutures = new CopyOnWriteArrayList<>();
        this.allClientSockets = new ArrayList<>();

        this.serverSocket = createSocket(port);
        this.executor = Executors.newFixedThreadPool(maxClients);
    }

    public void start() {
        try {
            initClientStatusListenerThread();
            initClientConnectionsListenerThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initClientStatusListenerThread() {
        Thread statusListenerThread = new Thread(() -> {
            while (true) {
                if (anyClientForcedTermination()) return;
            }
        });

        statusListenerThread.start();
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

    private void initClientConnectionsListenerThread() throws IOException {
        try (FileWriter fileWriter = new FileWriter(generateNewFile(), true)) {
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
                    acceptClient(fileWriter, clientName, clientSocket);
                }
            }
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

    private static File generateNewFile() throws IOException {
        File outputFile = new File("numbers.log").getAbsoluteFile();
        if (outputFile.exists()) {
            outputFile.delete();
        }

        outputFile.createNewFile();

        return outputFile;
    }

    private void rejectClient(Socket clientSocket) {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptClient(FileWriter fileWriter, String clientName, Socket clientSocket) {
        LinesReader linesReader = LinesReader.create(TERMINATION_SEQUENCE, uniqueNumbersSet, fileWriter);
        ClientHandler clientHandler = new ClientHandler(clientName, clientSocket, linesReader);

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
