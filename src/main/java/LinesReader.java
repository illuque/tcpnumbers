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

    public boolean read(Scanner inputScanner) throws IOException {
        boolean terminateSequenceReceived = false;

        while (inputScanner.hasNextLine()) {
            String line = inputScanner.nextLine();

            boolean isValidLength = line.length() == VALID_INPUT_LENGTH;
            if (!isValidLength) {
                break;
            }

            terminateSequenceReceived = terminateSequence.equals(line);
            if (terminateSequenceReceived) {
                break;
            }

            Integer validNumber = getLineAsNumber(line);
            if (validNumber == null) {
                break;
            }

            boolean accepted = numbersApprover.add(validNumber);
            if (accepted) {
                String logLine = validNumber + System.lineSeparator();

                // TODO:I intentar matar el outputStreamWriter de último para q si leyó uno lo pueda escribir

                outputStreamWriter.append(logLine);
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
