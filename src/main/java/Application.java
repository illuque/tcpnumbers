class Application {

    public static void main(String[] args) {
        TcpServer.create(4000, 5).start();
    }

}
