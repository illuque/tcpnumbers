package com.illuque.tcpnumbers.client;

import com.illuque.tcpnumbers.LinesProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientTest {

    @Mock
    private Socket mockClientSocket;

    @Mock
    private LinesProcessor linesProcessor;

    private Client clientToTest;

    @BeforeEach
    void setUp() {
        clientToTest = Client.create("#1", mockClientSocket, linesProcessor);
    }

    @Test
    void call_whenInvalidInputReceived_thenReturn() throws IOException {
        PipedOutputStream oStream = new PipedOutputStream();
        PipedInputStream iStream = new PipedInputStream(oStream);
        when(mockClientSocket.getInputStream()).thenReturn(iStream);

        when(linesProcessor.read(any())).thenReturn(ClientResult.INVALID_INPUT);
        assertEquals(ClientResult.INVALID_INPUT, clientToTest.call());
    }

    @Test
    void call_whenErrorReading_thenReturn() throws IOException {
        PipedOutputStream oStream = new PipedOutputStream();
        PipedInputStream iStream = new PipedInputStream(oStream);
        when(mockClientSocket.getInputStream()).thenReturn(iStream);

        when(linesProcessor.read(any())).thenThrow(new IOException("boom"));
        assertEquals(ClientResult.ERROR_READING, clientToTest.call());
    }

    @Test
    void call_whenSocketClosed_thenClientDisconnected() {
        when(mockClientSocket.isClosed()).thenReturn(true);
        assertEquals(ClientResult.CLIENT_DISCONNECTED, clientToTest.call());
    }

    @Test
    void schedule() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        assertDoesNotThrow(() -> clientToTest.schedule(executor));
    }

    @Test
    void disconnect() throws IOException {
        clientToTest.disconnect();
        verify(mockClientSocket, times(1)).close();
    }

    @Test
    void isDone_trueWhenFinished() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        clientToTest.schedule(executor);
        executor.awaitTermination(300, TimeUnit.MILLISECONDS);
        assertTrue(clientToTest.isDone());
    }

    @Test
    void isDone_falseWhenInProgress() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        clientToTest.schedule(executor);
        assertFalse(clientToTest.isDone());
    }

    @Test
    void receivedTerminationFlag_trueWhenReceived() throws IOException, InterruptedException, ExecutionException {
        PipedOutputStream oStream = new PipedOutputStream();
        PipedInputStream iStream = new PipedInputStream(oStream);
        when(mockClientSocket.getInputStream()).thenReturn(iStream);

        when(linesProcessor.read(any())).thenReturn(ClientResult.FORCED_TERMINATION);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        clientToTest.schedule(executor);
        executor.awaitTermination(300, TimeUnit.MILLISECONDS);

        assertTrue(clientToTest.receivedTerminationFlag());
    }
}