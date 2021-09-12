import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Application {

    private static final String TERMINATION_SEQUENCE = "terminate";
    public static final int PORT = 4000;
    public static final int MAX_CLIENTS = 5;

    public static void main(String[] args) throws IOException {
        Set<Integer> uniqueNumbersSet = Collections.synchronizedSet(new HashSet<>());
        FileWriter fileWriter = new FileWriter(generateNewFile(), true);
        LinesReader linesReader = LinesReader.create(TERMINATION_SEQUENCE, uniqueNumbersSet, fileWriter);

        TcpServer tcpServer = TcpServer.create(PORT, MAX_CLIENTS, linesReader);

        tcpServer.start();
    }

    private static File generateNewFile() throws IOException {
        File outputFile = new File("numbers.log").getAbsoluteFile();
        if (outputFile.exists()) {
            outputFile.delete();
        }

        outputFile.createNewFile();

        return outputFile;
    }

}
