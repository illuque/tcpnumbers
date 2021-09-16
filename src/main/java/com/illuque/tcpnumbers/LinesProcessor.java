package com.illuque.tcpnumbers;

import com.illuque.tcpnumbers.client.ClientResult;

import java.io.IOException;
import java.util.Scanner;

public class LinesProcessor {

    private static final int VALID_INPUT_LENGTH = 9;

    private final String terminateSequence;
    private final NumbersCollector numbersCollector;

    public static LinesProcessor create(String terminateSequence, NumbersCollector numbersCollector) {
        return new LinesProcessor(terminateSequence, numbersCollector);
    }

    private LinesProcessor(String terminateSequence, NumbersCollector numbersCollector) {
        this.terminateSequence = terminateSequence;
        this.numbersCollector = numbersCollector;
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

            numbersCollector.add(validNumber);
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
