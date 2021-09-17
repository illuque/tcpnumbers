package com.illuque.tcpnumbers;

import com.illuque.tcpnumbers.server.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TcpNumbers {

    protected static final String OUTPUT_FILENAME = "numbers.log";
    private static final int REPORT_FREQUENCY = 10 * 1000;

    private static final String TERMINATION_SEQUENCE = "terminate";
    private static final int INACTIVITY_TO_FLUSH = 1000;

    private static final int TIMEOUT_FOR_SHUTDOWN = REPORT_FREQUENCY + 100;

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

    public void start() throws IOException, InterruptedException {
        try (BufferedWriter bufferedFileWriter = generateBufferedFileWriter()) {
            NumbersCollector numbersCollector = NumbersCollector.create();

            LinesProcessor linesProcessor = LinesProcessor.create(TERMINATION_SEQUENCE, numbersCollector);

            Server server = Server.create(port, maxClients, linesProcessor);

            keepRunning = true;

            ExecutorService consumersExecutor = Executors.newFixedThreadPool(2);

            consumersExecutor.submit(buildFileWriterConsumer(bufferedFileWriter, numbersCollector));
            consumersExecutor.submit(buildReporterConsumerThread(numbersCollector));

            server.start();

            keepRunning = false;
            consumersExecutor.shutdown();

            System.out.println();
            System.out.println("Shutting down...");
            System.out.println();

            boolean gracefulShutDown = consumersExecutor.awaitTermination(TIMEOUT_FOR_SHUTDOWN, TimeUnit.MILLISECONDS);
            if (!gracefulShutDown) {
                System.err.println("Not all consumers finished gracefully");
            }
        }
    }

    private Runnable buildFileWriterConsumer(BufferedWriter bufferedWriter, NumbersCollector numbersCollector) {
        return () -> {
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
                    System.err.println("Error writing to file: " + e.getMessage());
                }
            }
        };
    }

    private void writeToFile(BufferedWriter bufferedWriter, Integer number) throws IOException {
        String logLine = number + System.lineSeparator();
        bufferedWriter.append(logLine);
    }

    private Runnable buildReporterConsumerThread(NumbersCollector numbersCollector) {
        return () -> {
            while (keepRunning) {
                try {
                    TimeUnit.MILLISECONDS.sleep(REPORT_FREQUENCY);
                } catch (InterruptedException e) {
                    System.err.println("Report thread could not sleep: " + e.getMessage());
                }

                NumbersCollector.Report report = numbersCollector.newReportRound();
                System.out.printf("Received %d unique numbers, %d duplicates. Unique total: %d%s", report.getUniquesInRound(), report.getDuplicatedInRound(), report.getTotalUniqueNumbers(), System.lineSeparator());
            }
        };
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
        boolean forceFlush = (System.currentTimeMillis() - lastRead) > INACTIVITY_TO_FLUSH;
        if (forceFlush) {
            bufferedWriter.flush();
        }
    }

}
