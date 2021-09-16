import java.io.IOException;
import java.util.Scanner;

public class LinesProcessor {

    private static final int VALID_INPUT_LENGTH = 9;

    private final String terminateSequence;
    private final NumbersApprover numbersApprover;

    public static LinesProcessor create(String terminateSequence, NumbersApprover numbersApprover) {
        return new LinesProcessor(terminateSequence, numbersApprover);
    }

    private LinesProcessor(String terminateSequence, NumbersApprover numbersApprover) {
        this.terminateSequence = terminateSequence;
        this.numbersApprover = numbersApprover;
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

            numbersApprover.add(validNumber);
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
