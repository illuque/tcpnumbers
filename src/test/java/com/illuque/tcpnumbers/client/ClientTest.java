package com.illuque.tcpnumbers.client;

import com.illuque.tcpnumbers.LinesProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

// TODO:I properly test
class ClientTest {

    private static final String[] INPUT = new String[]{"1", "2", "3", "4", "5", "terminate", "6", "7", "8", "9"};

    public static final String TERMINATION_SEQUENCE = "terminate";

    @Mock
    private ServerSocket mockServerSocket;

    @Mock
    private Socket mockClientSocket;

    @Mock
    private LinesProcessor linesProcessor;

    private Client clientToTest;

    @BeforeEach
    void setUp() throws IOException {
        when(mockServerSocket.accept()).thenReturn(mockClientSocket);

        PipedOutputStream oStream = new PipedOutputStream();
        when(mockClientSocket.getOutputStream()).thenReturn(oStream);

        PipedInputStream iStream = new PipedInputStream(oStream);
        when(mockClientSocket.getInputStream()).thenReturn(iStream);

        when(mockClientSocket.isClosed()).thenReturn(false);

        clientToTest = Client.create("#1", mockClientSocket, linesProcessor);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockClientSocket.close();
        mockServerSocket.close();
    }

    @Test
    void run_whenTerminationSequenceReceived_thenThreadFinishes() throws IOException, InterruptedException, ExecutionException {
        OutputStreamWriter clientOutputStream = new OutputStreamWriter(mockClientSocket.getOutputStream(), StandardCharsets.UTF_8);
        PrintWriter clientOutputPrinter = new PrintWriter(clientOutputStream, true);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(new Thread((Runnable) clientToTest));

        int messageThroughputInMillis = 1;
        senderMessagesInThread(clientOutputPrinter, messageThroughputInMillis);

        try {
            long enoughTimeForCommunicationToFinish = INPUT.length * messageThroughputInMillis * 2L;
            future.get(enoughTimeForCommunicationToFinish, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            fail("future should be already finished");
        }
        executor.shutdownNow();
    }

    private void senderMessagesInThread(PrintWriter out, int messageThroughputInMillis) {
        Thread sendMessageThread = new Thread(() -> {
            boolean terminationReached = false;
            for (String s : INPUT) {
                out.println(s);
                if (terminationReached) {
                    fail("This message should not be sent");
                }
                if (s.equals(TERMINATION_SEQUENCE)) {
                    terminationReached = true;
                }
                try {
                    Thread.sleep(messageThroughputInMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        sendMessageThread.start();
    }

}