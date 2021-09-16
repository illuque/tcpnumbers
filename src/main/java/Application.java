import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Application {

    private static final String OUTPUT_FILENAME = "numbers.log";

    private static final String TERMINATION_SEQUENCE = "terminate";
    private static final int PORT = 4000;
    private static final int MAX_CLIENTS = 5;

    public static void main(String[] args) throws IOException {
        try (FileWriter fileWriter = new FileWriter(generateNewFile(), true)) {
            NumbersApprover numbersApprover = NumbersApprover.getInstance();

            LinesReader linesReader = LinesReader.create(TERMINATION_SEQUENCE, numbersApprover, fileWriter);

            Server server = Server.create(PORT, MAX_CLIENTS, linesReader);

            numbersApprover.initReporter();

            server.start();

            numbersApprover.shutDownReporter();
        }
    }

    private static File generateNewFile() throws IOException {
        File outputFile = new File(OUTPUT_FILENAME).getAbsoluteFile();

        if (outputFile.exists() && !outputFile.delete()) {
            throw new IllegalStateException("Could not create file");
        }

        if (!outputFile.createNewFile()) {
            throw new IllegalStateException("Could not create file");
        }

        return outputFile;
    }

}
