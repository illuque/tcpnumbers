import com.illuque.tcpnumbers.TcpNumbers;

import java.io.IOException;

public class Application {

    private static final int PORT = 4000;
    private static final int MAX_CLIENTS = 5;

    public static void main(String[] args) throws IOException {
        new TcpNumbers(PORT, MAX_CLIENTS).start();
    }

}
