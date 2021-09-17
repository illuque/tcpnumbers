import com.illuque.tcpnumbers.TcpNumbersApp;

import java.io.IOException;

public class Application {

    private static final int PORT = 4000;
    private static final int MAX_CLIENTS = 5;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println();
        System.out.println("Starting application...");
        System.out.println();

        TcpNumbersApp.create(PORT, MAX_CLIENTS).start();

        System.out.println();
        System.out.println("Application finished...");
        System.out.println();
    }

}
