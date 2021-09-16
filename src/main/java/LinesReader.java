import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class LinesReader {

    private static final int VALID_INPUT_LENGTH = 9;

    private final String terminateSequence;
    private final NumbersApprover numbersApprover;
    private final OutputStreamWriter outputStreamWriter;

    public static LinesReader create(String terminateSequence, NumbersApprover numbersApprover, OutputStreamWriter outputStreamWriter) {
        return new LinesReader(terminateSequence, numbersApprover, outputStreamWriter);
    }

    private LinesReader(String terminateSequence, NumbersApprover numbersApprover, OutputStreamWriter outputStreamWriter) {
        this.terminateSequence = terminateSequence;
        this.numbersApprover = numbersApprover;
        this.outputStreamWriter = outputStreamWriter;
    }

    public ClientResult read(Scanner inputScanner) throws IOException {
        while (inputScanner.hasNextLine()) {
            String line = inputScanner.nextLine();

            boolean isValidLength = line.length() == VALID_INPUT_LENGTH;
            if (!isValidLength) {
                return ClientResult.INVALID_INPUT;
            }

            boolean terminateSequenceReceived = terminateSequence.equals(line);
            if (terminateSequenceReceived) {
                return ClientResult.FORCED_TERMINATION;
            }

            Integer validNumber = getLineAsNumber(line);
            if (validNumber == null) {
                return ClientResult.INVALID_INPUT;
            }

            boolean accepted = numbersApprover.add(validNumber);
            if (accepted) {
                String logLine = validNumber + System.lineSeparator();
                outputStreamWriter.append(logLine);
            }
        }

        return ClientResult.READ_EXHAUSTED;
    }

    private Integer getLineAsNumber(String line) {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
