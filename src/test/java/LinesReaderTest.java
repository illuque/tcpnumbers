import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

class LinesReaderTest {

    public static final String TERMINATE_SEQUENCE = "terminate";

    private static final String[] INPUT_WITHOUT_TERMINATION = new String[]{"111111111", "222222222", "333333333", "444444444", "555555555", "666666666", "777777777", "888888888", "999999999"};

    private static final String[] INPUT_WITH_TERMINATION = new String[]{"111111111", "222222222", "333333333", "444444444", "555555555", TERMINATE_SEQUENCE, "666666666", "777777777", "888888888", "999999999"};

    private LinesReader linesReaderToTest;

    private Set<Integer> uniqueNumberSet;

    @BeforeEach
    private void init() {
        uniqueNumberSet = new HashSet<>();
        linesReaderToTest = new LinesReader(TERMINATE_SEQUENCE, uniqueNumberSet, null);
    }

    @Test
    void read_whenNoLinesReceived_thenNothingReturned() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(new String[]{}));

        linesReaderToTest.read(scanner);

        Set<Integer> expectedNumbers = Set.of();
        Assertions.assertEquals(expectedNumbers, uniqueNumberSet);
    }

    @Test
    void read_whenTerminateReceived_thenLinesBeforeItReturned() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_WITH_TERMINATION));
        linesReaderToTest.read(scanner);

        List<String> validLines = Arrays.asList(Arrays.copyOfRange(INPUT_WITH_TERMINATION, 0, 5));
        Set<Integer> expectedNumbers = validLines.stream().map(Integer::parseInt).collect(Collectors.toSet());

        Assertions.assertEquals(expectedNumbers, uniqueNumberSet);
    }

    @Test
    void read_whenNoTerminateReceived_thenAllLinesReturned() throws IOException {
        Scanner scanner = new Scanner(getAsByteArray(INPUT_WITHOUT_TERMINATION));

        linesReaderToTest.read(scanner);

        List<String> validLines = Arrays.asList(INPUT_WITHOUT_TERMINATION);
        Set<Integer> expectedNumbers = validLines.stream().map(Integer::parseInt).collect(Collectors.toSet());

        Assertions.assertEquals(expectedNumbers, uniqueNumberSet);
    }

    private ByteArrayInputStream getAsByteArray(String[] input) {
        String lines = String.join(System.lineSeparator(), input);
        byte[] linesByteArray = lines.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(linesByteArray);
    }
}