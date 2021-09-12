import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.Set;

public class LinesReader {

    public static final int VALID_INPUT_LENGTH = 9;
    private final String terminateSequence;
    private final Set<Integer> uniqueNumbersSet;
    private final OutputStreamWriter outputStreamWriter;

    public static LinesReader create(String terminateSequence, Set<Integer> uniqueNumbersSet, OutputStreamWriter outputStreamWriter) {
        return new LinesReader(terminateSequence, uniqueNumbersSet, outputStreamWriter);
    }

    private LinesReader(String terminateSequence, Set<Integer> uniqueNumbersSet, OutputStreamWriter outputStreamWriter) {
        this.terminateSequence = terminateSequence;
        this.uniqueNumbersSet = uniqueNumbersSet;
        this.outputStreamWriter = outputStreamWriter;
    }

    public boolean read(Scanner inputScanner) throws IOException {
        boolean terminateSequenceReceived = false;

        while (inputScanner.hasNextLine()) {
            String line = inputScanner.nextLine();

            boolean isValidLength = line.length() == VALID_INPUT_LENGTH;
            if (!isValidLength) {
                break;
            }

            // TODO:I terminate all clients on this situation!
            terminateSequenceReceived = terminateSequence.equals(line);
            if (terminateSequenceReceived) {
                break;
            }

            Integer validNumber = getLineAsNumber(line);
            if (validNumber == null) {
                break;
            }

            boolean isNew = uniqueNumbersSet.add(validNumber);
            if (isNew) {
                outputStreamWriter.append(validNumber.toString()).append(System.lineSeparator());
            }
        }

        return terminateSequenceReceived;
    }

    private Integer getLineAsNumber(String line) {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
