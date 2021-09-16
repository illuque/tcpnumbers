import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Application {

    private static final String OUTPUT_FILENAME = "numbers.log";
    private static final int REPORT_FREQUENCY_IN_SECONDS = 10;

    private static final String TERMINATION_SEQUENCE = "terminate";
    private static final int PORT = 4000;
    private static final int MAX_CLIENTS = 5;

    private static volatile boolean keepRunning;

    public static void main(String[] args) throws IOException {
        try (BufferedWriter bufferedWriter = generateBufferedFileWriter()) {

            NumbersApprover numbersApprover = NumbersApprover.getInstance();

            LinesProcessor linesProcessor = LinesProcessor.create(TERMINATION_SEQUENCE, numbersApprover);

            Server server = Server.create(PORT, MAX_CLIENTS, linesProcessor);

            keepRunning = true;

            initFileWriterThread(bufferedWriter, numbersApprover);
            initReporterThread(numbersApprover);

            server.start();

            keepRunning = false;
        }
    }

    private static void initFileWriterThread(BufferedWriter bufferedWriter, NumbersApprover numbersApprover) {
        Runnable runnable = () -> {
            long lastRead = -1;
            while (keepRunning) {
                Integer number = numbersApprover.pollNumber();
                try {
                    if (number != null) {
                        lastRead = System.currentTimeMillis();
                        writeToFile(bufferedWriter, number);
                    } else {
                        forceFlushAfterInactivity(bufferedWriter, lastRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread reporterThread = new Thread(runnable);
        reporterThread.start();
    }

    private static void writeToFile(BufferedWriter bufferedWriter, Integer number) throws IOException {
        String logLine = number + System.lineSeparator();
        bufferedWriter.append(logLine);
    }

    private static void initReporterThread(NumbersApprover numbersApprover) {
        Runnable runnable = () -> {
            while (keepRunning) {
                try {
                    TimeUnit.SECONDS.sleep(REPORT_FREQUENCY_IN_SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                NumbersApprover.Report report = numbersApprover.newReportRound();
                System.out.printf("Received %d unique numbers, %d duplicates. Unique total: %d%s", report.getUniquesInRound(), report.getDuplicatedInRound(), report.getTotalUniqueNumbers(), System.lineSeparator());
            }
        };

        Thread reporterThread = new Thread(runnable);
        reporterThread.start();
    }

    private static BufferedWriter generateBufferedFileWriter() throws IOException {
        return new BufferedWriter(new FileWriter(generateNewFile(), true));
    }

    private static File generateNewFile() throws IOException {
        File outputFile = new File(OUTPUT_FILENAME).getAbsoluteFile();

        if (outputFile.exists() && !outputFile.delete()) {
            throw new IllegalStateException("Could not create file, already exists and not possible to deelete it");
        }

        if (!outputFile.createNewFile()) {
            throw new IllegalStateException("Could not create file");
        }

        return outputFile;
    }

    private static void forceFlushAfterInactivity(BufferedWriter bufferedWriter, long lastRead) throws IOException {
        boolean forceFlush = (System.currentTimeMillis() - lastRead) > 1000;
        if (forceFlush) {
            bufferedWriter.flush();
        }
    }

}
