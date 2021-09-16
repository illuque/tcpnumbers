import com.illuque.tcpnumbers.LinesProcessor;
import com.illuque.tcpnumbers.NumbersCollector;
import com.illuque.tcpnumbers.client.ClientResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinesProcessorTest {

    public static final String TERMINATE_SEQUENCE = "terminate";

    private static final String[] INPUT_WITHOUT_TERMINATION = new String[]{"111111111", "222222222", "666666666", "777777777"};

    private static final String[] INPUT_WITH_FAKE_TERMINATION = new String[]{"111111111", "222222222", "fake" + TERMINATE_SEQUENCE, "666666666", "777777777"};

    private static final String[] INPUT_WITH_TERMINATION = new String[]{"111111111", "222222222", TERMINATE_SEQUENCE, "666666666", "777777777"};

    private static final String[] INPUT_INVALID = new String[]{"111111111", "222222222", "333", "666666666", "777777777"};

    private LinesProcessor linesProcessorToTest;

    @Mock
    private NumbersCollector numbersCollector;

    @BeforeEach
    private void init() {
        linesProcessorToTest = LinesProcessor.create(TERMINATE_SEQUENCE, numbersCollector);
    }

    @Test
    void read_whenNoLinesReceived_thenNothingReturned() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(new String[]{}));
        ClientResult clientResult = linesProcessorToTest.read(scanner);
        verify(numbersCollector, never()).add(anyInt());
        Assertions.assertEquals(ClientResult.READ_EXHAUSTED, clientResult);
    }

    @Test
    void read_whenTerminateReceived_thenLinesBeforeItAdded() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_WITH_TERMINATION));
        ClientResult clientResult = linesProcessorToTest.read(scanner);
        verify(numbersCollector, times(2)).add(anyInt());
        Assertions.assertEquals(ClientResult.FORCED_TERMINATION, clientResult);
    }

    @Test
    void read_whenFakeTerminateReceived_thenLinesBeforeItAdded() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_WITH_FAKE_TERMINATION));
        ClientResult clientResult = linesProcessorToTest.read(scanner);
        verify(numbersCollector, times(2)).add(anyInt());
        Assertions.assertEquals(ClientResult.INVALID_INPUT, clientResult);
    }

    @Test
    void read_whenInvalidInputReceived_thenLinesBeforeItAdded() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_INVALID));
        ClientResult clientResult = linesProcessorToTest.read(scanner);
        verify(numbersCollector, times(2)).add(anyInt());
        Assertions.assertEquals(ClientResult.INVALID_INPUT, clientResult);
    }

    @Test
    void read_whenNoTerminateReceived_thenAllLinesAdded() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_WITHOUT_TERMINATION));
        ClientResult clientResult = linesProcessorToTest.read(scanner);
        verify(numbersCollector, times(INPUT_WITHOUT_TERMINATION.length)).add(anyInt());
        Assertions.assertEquals(ClientResult.READ_EXHAUSTED, clientResult);
    }

    private ByteArrayInputStream getAsByteArray(String[] input) {
        String lines = String.join(System.lineSeparator(), input);
        byte[] linesByteArray = lines.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(linesByteArray);
    }
}