import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Application {

    private static final String TERMINATION_SEQUENCE = "terminate";
    private static final int PORT = 4000;
    private static final int MAX_CLIENTS = 5;

    public static void main(String[] args) throws IOException {
        try (FileWriter fileWriter = new FileWriter(generateNewFile(), true)) {
            Set<Integer> uniqueNumbersSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

            LinesReader linesReader = LinesReader.create(TERMINATION_SEQUENCE, uniqueNumbersSet, fileWriter);

            TcpServer tcpServer = TcpServer.create(PORT, MAX_CLIENTS, linesReader);

            tcpServer.start();
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
