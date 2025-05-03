public class StartTideServer {
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        System.out.println("=== SERVER MARE VENEZIA ===");
        System.out.println("Caricamento dati da: " + Server.CSV_FILE);
        
        Server server = new Server();
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nArresto del server...");
            server.stop();
        }));
        
        System.out.println("Server avviato. Premi CTRL+C per terminare...");
        try {
            while (true) Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
