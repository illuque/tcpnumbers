package com.illuque.tcpnumbers;

import com.illuque.tcpnumbers.client.ClientResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinesProcessorTest {

    public static final String TERMINATION_SEQUENCE = "terminate";

    private static final String[] INPUT_WITH_TERMINATION = new String[]{"111111111", "222222222", "333333333", "444444444", "555555555", TERMINATION_SEQUENCE, "6666666666"};
    private static final String[] INPUT_WITH_SHORT_NUMBER = new String[]{"111111111", "222", "333333333"};
    private static final String[] INPUT_WITH_SHORT_WORD = new String[]{"111111111", "surprise", "333333333"};
    private static final String[] INPUT_OK = new String[]{"111111111", "222222222", "333333333", "444444444", "555555555", "666666666"};

    @Mock
    private ServerSocket mockServerSocket;

    @Mock
    private Socket mockClientSocket;

    @Mock
    private NumbersCollector numbersCollector;

    private LinesProcessor linesProcessorToTest;

    @BeforeEach
    void setUp() throws IOException {
        PipedOutputStream oStream = new PipedOutputStream();
        when(mockClientSocket.getOutputStream()).thenReturn(oStream);

        PipedInputStream iStream = new PipedInputStream(oStream);
        when(mockClientSocket.getInputStream()).thenReturn(iStream);

        linesProcessorToTest = LinesProcessor.create(TERMINATION_SEQUENCE, numbersCollector);
    }

    @Test
    void run_whenLessThan9DigitsReceived_thenReadingStops() throws IOException {
        ClientResult test = setup(INPUT_WITH_SHORT_NUMBER);

        assertEquals(ClientResult.INVALID_INPUT, test);
        verify(numbersCollector, times(1)).add(anyInt());
    }

    @Test
    void run_whenTerminationSequenceReceived_thenReadingStops() throws IOException {
        ClientResult test = setup(INPUT_WITH_TERMINATION);

        assertEquals(ClientResult.FORCED_TERMINATION, test);
        verify(numbersCollector, times(5)).add(anyInt());
    }

    @Test
    void run_whenWordReceived_thenReadingStops() throws IOException {
        ClientResult test = setup(INPUT_WITH_SHORT_WORD);

        assertEquals(ClientResult.INVALID_INPUT, test);
        verify(numbersCollector, times(1)).add(anyInt());
    }

    @Test
    void run_whenAllInputGood_thenReadingFinishes() throws IOException {
        ClientResult test = setup(INPUT_OK);

        assertEquals(ClientResult.READ_EXHAUSTED, test);
        verify(numbersCollector, times(6)).add(anyInt());
    }

    private ClientResult setup(String[] inputOk) throws IOException {
        OutputStreamWriter clientOutputStream = new OutputStreamWriter(mockClientSocket.getOutputStream(), StandardCharsets.UTF_8);
        PrintWriter clientOutputPrinter = new PrintWriter(clientOutputStream, true);

        int messageThroughputInMillis = 1;
        senderMessagesInThread(inputOk, clientOutputPrinter, messageThroughputInMillis);

        return linesProcessorToTest.read(new Scanner(mockClientSocket.getInputStream(), StandardCharsets.UTF_8));
    }

    private void senderMessagesInThread(String[] data, PrintWriter out, int messageThroughputInMillis) {
        Thread sendMessageThread = new Thread(() -> {
            for (String s : data) {
                out.println(s);
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