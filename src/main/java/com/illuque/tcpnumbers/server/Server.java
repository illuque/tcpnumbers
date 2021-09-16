package com.illuque.tcpnumbers.server;

import com.illuque.tcpnumbers.LinesProcessor;
import com.illuque.tcpnumbers.client.Client;

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

    private final LinesProcessor linesProcessor;

    public static Server create(int port, int maxClients, LinesProcessor linesProcessor) {
        return new Server(port, maxClients, linesProcessor);
    }

    private Server(int port, int maxClients, LinesProcessor linesProcessor) {
        this.maxClients = maxClients;

        this.serverSocket = createSocket(port);
        this.clientExecutorService = Executors.newFixedThreadPool(maxClients);

        this.linesProcessor = linesProcessor;

        this.activeClients = new CopyOnWriteArrayList<>();
    }

    public void start() {
        try {
            initClientStatusListenerThread();

            listenClientConnections();

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

    private void listenClientConnections() throws IOException {
        int i = 0;
        while (true) {
            i++;
            String clientName = "#" + i;

            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (SocketException se) {
                System.out.printf("Client requested termination%s", System.lineSeparator());
                return;
            }

            boolean maxClientsReached = activeClients.size() == maxClients;
            if (maxClientsReached) {
                rejectClient(clientSocket);
            } else {
                acceptAndStartClient(clientName, clientSocket);
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

    private void acceptAndStartClient(String clientName, Socket clientSocket) {
        Client client = Client.create(clientName, clientSocket, linesProcessor);
        client.schedule(clientExecutorService);
        activeClients.add(client);
    }

    public synchronized void shutDown() {
        try {
            // stop receiving new tasks/clients
            clientExecutorService.shutdown();

            for (Client client : activeClients) {
                client.disconnect();
            }

            boolean gracefulShutDown = clientExecutorService.awaitTermination(TIMEOUT_FOR_SHUTDOWN, TimeUnit.SECONDS);
            if (!gracefulShutDown) {
                System.err.println("Not all Clients finished gracefully");
            }

            serverSocket.close();
        } catch (InterruptedException e) {
            System.err.println("Error awaiting client termination: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

}
