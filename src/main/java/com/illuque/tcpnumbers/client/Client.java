package com.illuque.tcpnumbers.client;

import com.illuque.tcpnumbers.LinesProcessor;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Client implements Callable<ClientResult> {

    private final String name;
    private final Socket socket;
    private final LinesProcessor linesProcessor;
    private Future<ClientResult> future;

    public static Client create(String name, Socket clientSocket, LinesProcessor linesProcessor) {
        return new Client(name, clientSocket, linesProcessor);
    }

    private Client(String name, Socket clientSocket, LinesProcessor linesProcessor) {
        this.name = name;
        this.socket = clientSocket;
        this.linesProcessor = linesProcessor;
        System.out.printf("Client %s connected%s", this.name, System.lineSeparator());
    }

    public void schedule(ExecutorService executorService) {
        future = executorService.submit(this);
    }

    @Override
    public ClientResult call() {
        if (socket.isClosed()) {
            System.out.printf("Socket is closed, cannot run client %s%s", this.name, System.lineSeparator());
            return ClientResult.CLIENT_DISCONNECTED;
        }

        try (Scanner inputScanner = new Scanner(socket.getInputStream(), StandardCharsets.UTF_8)) {
            return linesProcessor.read(inputScanner);
        } catch (IOException e) {
            e.printStackTrace();
            return ClientResult.ERROR_READING;
        }
    }

    public void disconnect() throws IOException {
        socket.close();
    }

    public boolean isDone() throws ExecutionException, InterruptedException {
        return future.isDone();
    }

    public boolean receivedTerminationFlag() throws ExecutionException, InterruptedException {
        return future.isDone() && future.get() == ClientResult.FORCED_TERMINATION;
    }

}