package com.illuque.tcpnumbers.server;

import com.illuque.tcpnumbers.LinesProcessor;
import com.illuque.tcpnumbers.client.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

public class Server {

    private static final int TIMEOUT_FOR_SHUTDOWN = 5000;

    private final ExecutorService clientExecutorService;

    private final ServerSocket serverSocket;

    private final int maxClients;

    private final List<Client> activeClients;

    private final LinesProcessor linesProcessor;

    private volatile boolean terminationFlagReceived;

    public static Server create(int port, int maxClients, LinesProcessor linesProcessor) {
        return new Server(port, maxClients, linesProcessor);
    }

    private Server(int port, int maxClients, LinesProcessor linesProcessor) {
        this.maxClients = maxClients;

        this.serverSocket = createSocket(port);
        this.clientExecutorService = Executors.newFixedThreadPool(maxClients);

        this.linesProcessor = linesProcessor;

        this.activeClients = new CopyOnWriteArrayList<>();

        terminationFlagReceived = false;
    }

    public void start() {
        initClientStatusListenerThread();
        listenClientConnections();
    }

    private ServerSocket createSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
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
                        terminationFlagReceived = true;
                        shutDown();
                        return;
                    }
                }
            }
        });

        statusListenerThread.start();
    }

    private void listenClientConnections() {
        int i = 0;
        while (true) {
            i++;
            String clientName = "#" + i;

            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException se) {
                if (terminationFlagReceived) {
                    // shutDown() was called and the serverSocket.close() caused this exception
                    System.out.println("Client requested termination, server will reject new clients...");
                } else {
                    System.err.println("Server was unexpectedly closed: " + se.getMessage());
                }
                return;
            }

            boolean maxClientsReached = activeClients.size() == maxClients;
            if (maxClientsReached) {
                rejectClient(clientName, clientSocket);
            } else {
                acceptClient(clientName, clientSocket);
            }
        }
    }

    private boolean clientIsDone(Client client) {
        try {
            return client.isDone();
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Could not check client completion: " + e.getMessage());
        }
        return false;
    }

    private boolean clientForcedTermination(Client client) {
        try {
            if (client.receivedTerminationFlag()) {
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error checking client termination: " + e.getMessage());
        }
        return false;
    }

    private void rejectClient(String clientName, Socket clientSocket) {
        try {
            clientSocket.close();
            System.out.printf("Client %s rejected%s", clientName, System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }

    private void acceptClient(String clientName, Socket clientSocket) {
        Client client = Client.create(clientName, clientSocket, linesProcessor);
        client.schedule(clientExecutorService);
        activeClients.add(client);
        System.out.printf("Client %s connected%s", clientName, System.lineSeparator());
    }

    public synchronized void shutDown() {
        try {
            // stop receiving new tasks/clients
            clientExecutorService.shutdown();

            for (Client client : activeClients) {
                client.disconnect();
            }

            boolean gracefulShutDown = clientExecutorService.awaitTermination(TIMEOUT_FOR_SHUTDOWN, TimeUnit.MILLISECONDS);
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
