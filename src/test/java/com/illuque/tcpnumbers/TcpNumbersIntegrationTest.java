package com.illuque.tcpnumbers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TcpNumbersIntegrationTest {

    private static final int PORT = 4001;
    private static final int MAX_CLIENTS = 2;

    @Test
    void IT_fileCreatedCorrectly() throws IOException, InterruptedException {
        TcpNumbers tcpNumbers = TcpNumbers.create(PORT, MAX_CLIENTS);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Runnable serverRunnable = () -> assertDoesNotThrow(tcpNumbers::start);

        executorService.submit(serverRunnable);

        // give time for the server to setup
        Thread.sleep(500);

        Socket socket = new Socket("127.0.0.1", PORT);

        Runnable client1 = () -> {
            PrintWriter outToServer = null;
            try {
                outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                fail();
            }
            outToServer.print("111111111\n");
            outToServer.print("222222222\n");
            outToServer.print("333333333\n");
            outToServer.print("222222222\n");
            outToServer.flush();
        };

        Runnable client2 = () -> {
            PrintWriter outToServer = null;
            try {
                outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                fail();
            }
            outToServer.print("111111111\n");
            outToServer.print("222222222\n");
            outToServer.print("333333333\n");
            outToServer.print("444444444\n");
            outToServer.print("terminate\n");
            outToServer.print("555555555\n");
            outToServer.flush();
        };

        client1.run();
        client2.run();

        assertDoesNotThrow(() -> executorService.awaitTermination(500, TimeUnit.MILLISECONDS));

        String lines = Files.lines(Paths.get(TcpNumbers.OUTPUT_FILENAME), StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        assertEquals("111111111\n222222222\n333333333\n444444444", lines);
    }

}