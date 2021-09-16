package com.illuque.tcpnumbers;

import com.illuque.tcpnumbers.server.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TcpNumbers {

    protected static final String OUTPUT_FILENAME = "numbers.log";
    private static final int REPORT_FREQUENCY_IN_SECONDS = 10;

    private static final String TERMINATION_SEQUENCE = "terminate";
    private static final int INACTIVITY_TO_FLUSH_MILLIS = 1000;

    private volatile boolean keepRunning;

    private final int port;

    private final int maxClients;

    public static TcpNumbers create(int port, int maxClients) {
        return new TcpNumbers(port, maxClients);
    }

    private TcpNumbers(int port, int maxClients) {
        this.port = port;
        this.maxClients = maxClients;
    }

    public void start() throws IOException {
        try (BufferedWriter bufferedFileWriter = generateBufferedFileWriter()) {
            NumbersCollector numbersCollector = NumbersCollector.create();

            LinesProcessor linesProcessor = LinesProcessor.create(TERMINATION_SEQUENCE, numbersCollector);

            Server server = Server.create(port, maxClients, linesProcessor);

            keepRunning = true;

            initFileWriterConsumerThread(bufferedFileWriter, numbersCollector);
            initReporterConsumerThread(numbersCollector);

            server.start();

            keepRunning = false;
        }
    }

    private void initFileWriterConsumerThread(BufferedWriter bufferedWriter, NumbersCollector numbersCollector) {
        Runnable runnable = () -> {
            long lastReadTimestamp = -1;
            while (keepRunning) {
                Integer number = numbersCollector.pollNumber();
                try {
                    if (number != null) {
                        lastReadTimestamp = System.currentTimeMillis();
                        writeToFile(bufferedWriter, number);
                    } else {
                        forceFlushAfterInactivity(bufferedWriter, lastReadTimestamp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread reporterThread = new Thread(runnable);
        reporterThread.start();
    }

    private void writeToFile(BufferedWriter bufferedWriter, Integer number) throws IOException {
        String logLine = number + System.lineSeparator();
        bufferedWriter.append(logLine);
    }

    private void initReporterConsumerThread(NumbersCollector numbersCollector) {
        Runnable runnable = () -> {
            while (keepRunning) {
                try {
                    TimeUnit.SECONDS.sleep(REPORT_FREQUENCY_IN_SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                NumbersCollector.Report report = numbersCollector.newReportRound();
                System.out.printf("Received %d unique numbers, %d duplicates. Unique total: %d%s", report.getUniquesInRound(), report.getDuplicatedInRound(), report.getTotalUniqueNumbers(), System.lineSeparator());
            }
        };

        Thread reporterThread = new Thread(runnable);
        reporterThread.start();
    }

    private BufferedWriter generateBufferedFileWriter() throws IOException {
        return new BufferedWriter(new FileWriter(generateNewFile(), true));
    }

    private File generateNewFile() throws IOException {
        File outputFile = new File(OUTPUT_FILENAME).getAbsoluteFile();

        if (outputFile.exists() && !outputFile.delete()) {
            throw new IllegalStateException("Could not create file, already exists and not possible to deelete it");
        }

        if (!outputFile.createNewFile()) {
            throw new IllegalStateException("Could not create file");
        }

        return outputFile;
    }

    private void forceFlushAfterInactivity(BufferedWriter bufferedWriter, long lastRead) throws IOException {
        boolean forceFlush = (System.currentTimeMillis() - lastRead) > INACTIVITY_TO_FLUSH_MILLIS;
        if (forceFlush) {
            bufferedWriter.flush();
        }
    }

}
