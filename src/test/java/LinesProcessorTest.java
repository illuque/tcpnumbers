import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

class LinesProcessorTest {

    public static final String TERMINATE_SEQUENCE = "terminate";

    private static final String[] INPUT_WITHOUT_TERMINATION = new String[]{"111111111", "222222222", "666666666", "777777777"};

    private static final String[] INPUT_WITH_FAKE_TERMINATION = new String[]{"111111111", "222222222", "fake" + TERMINATE_SEQUENCE, "666666666", "777777777"};

    private static final String[] INPUT_WITH_TERMINATION = new String[]{"111111111", "222222222", TERMINATE_SEQUENCE, "666666666", "777777777"};

    private static final String[] INPUT_INVALID = new String[]{"111111111", "222222222", "333", "666666666", "777777777"};

    private LinesProcessor linesProcessorToTest;

    private ByteArrayOutputStream outputStream;

    private OutputStreamWriter outputStreamWriter;

    @Mock
    private NumbersApprover numbersApprover;

    // TODO:I check for duplicates

    @BeforeEach
    private void init() {
        outputStream = new ByteArrayOutputStream();
        outputStreamWriter = new OutputStreamWriter(outputStream);
        linesProcessorToTest = LinesProcessor.create(TERMINATE_SEQUENCE, numbersApprover);
    }

    @Test
    void read_whenNoLinesReceived_thenNothingReturned() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(new String[]{}));

        ClientResult clientResult = linesProcessorToTest.read(scanner);
        outputStreamWriter.flush();

        Assertions.assertEquals(ClientResult.READ_EXHAUSTED, clientResult);
        Assertions.assertEquals("", outputStream.toString());
    }

    @Test
    void read_whenTerminateReceived_thenLinesBeforeItReturned() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_WITH_TERMINATION));

        ClientResult clientResult = linesProcessorToTest.read(scanner);
        outputStreamWriter.flush();

        List<String> validLines = Arrays.asList(Arrays.copyOfRange(INPUT_WITH_TERMINATION, 0, 2));
        String expectedNumbers = validLines.stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();

        Assertions.assertEquals(ClientResult.FORCED_TERMINATION, clientResult);
        Assertions.assertEquals(expectedNumbers, outputStream.toString());
    }

    @Test
    void read_whenFakeTerminateReceived_thenLinesBeforeItReturned() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_WITH_FAKE_TERMINATION));

        ClientResult clientResult = linesProcessorToTest.read(scanner);
        outputStreamWriter.flush();

        List<String> validLines = Arrays.asList(Arrays.copyOfRange(INPUT_WITH_TERMINATION, 0, 2));
        String expectedNumbers = validLines.stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();

        Assertions.assertEquals(ClientResult.INVALID_INPUT, clientResult);
        Assertions.assertEquals(expectedNumbers, outputStream.toString());
    }

    @Test
    void read_whenInvalidInputReceived_thenLinesBeforeItReturned() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_WITH_FAKE_TERMINATION));

        ClientResult clientResult = linesProcessorToTest.read(scanner);
        outputStreamWriter.flush();

        List<String> validLines = Arrays.asList(Arrays.copyOfRange(INPUT_INVALID, 0, 2));
        String expectedNumbers = validLines.stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();

        Assertions.assertEquals(ClientResult.INVALID_INPUT, clientResult);
        Assertions.assertEquals(expectedNumbers, outputStream.toString());
    }

    @Test
    void read_whenNoTerminateReceived_thenAllLinesReturned() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_WITHOUT_TERMINATION));

        ClientResult clientResult = linesProcessorToTest.read(scanner);
        outputStreamWriter.flush();

        List<String> validLines = Arrays.asList(INPUT_WITHOUT_TERMINATION);
        String expectedNumbers = validLines.stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();

        Assertions.assertEquals(ClientResult.READ_EXHAUSTED, clientResult);
        Assertions.assertEquals(expectedNumbers, outputStream.toString());
    }

    private ByteArrayInputStream getAsByteArray(String[] input) {
        String lines = String.join(System.lineSeparator(), input);
        byte[] linesByteArray = lines.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(linesByteArray);
    }
}