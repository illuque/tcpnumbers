import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;

public class Application {

    private static final String TERMINATION_SEQUENCE = "terminate";
    private static final int PORT = 4000;
    private static final int MAX_CLIENTS = 5;

    public static void main(String[] args) throws IOException {
        try (FileWriter fileWriter = new FileWriter(generateNewFile(), true)) {
            NumbersApprover numbersApprover = NumbersApprover.getInstance();

            // TODO:I hacer rebase para meter esto donde tocaba
            Supplier<LinesReader> linesReaderCreator = () -> LinesReader.create(TERMINATION_SEQUENCE, numbersApprover, fileWriter);

            TcpServer tcpServer = TcpServer.create(PORT, MAX_CLIENTS, linesReaderCreator);

            tcpServer.start();

            // TODO:I intentar q no se cierre el fileWriter hasta q de verdad el resto de procesos terminaron

            numbersApprover.shutDown();
        }
    }

    private static File generateNewFile() throws IOException {
        File outputFile = new File("numbers.log").getAbsoluteFile();

        if (outputFile.exists() && !outputFile.delete()) {
            throw new IllegalStateException("Could not create file");
        }

        if (!outputFile.createNewFile()) {
            throw new IllegalStateException("Could not create file");
        }

        return outputFile;
    }

}
