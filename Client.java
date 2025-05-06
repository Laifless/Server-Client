import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    static boolean exit= false;
        private static final int TIMEOUT = 10000000; // 10 secondi
        
        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("=== CLIENT MARE VENEZIA ===");
            boolean exit = false; // Variabile per controllare il ciclo
            do {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress("localhost", 4578), TIMEOUT);
                    socket.setSoTimeout(TIMEOUT);
                    
                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                         PrintWriter out = new PrintWriter(
                            socket.getOutputStream(), true)) {
                        
                        System.out.println("Connesso al server. " + in.readLine());
                        
                        while (!exit) { // Continua finché exit è false
                            printMenu();
                            System.out.print("> ");
                            String input = scanner.nextLine().trim();
                            
                            out.println(input);
                            if (input.equalsIgnoreCase("EXIT")) {
                                exit = true; // Esce dal ciclo
                                break;
                            }
                            
                            System.out.println("\n=== RISULTATI ===");
                            String response;
                            while ((response = in.readLine()) != null) {
                                if (response.equals("END_OF_TRANSMISSION")) break;
                                System.out.println(response);
                            }
                            System.out.println("=================\n");
                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout: server non risponde");
                } catch (ConnectException e) {
                    System.err.println("Connessione rifiutata. Verifica che il server sia avviato.");
                } catch (IOException e) {
                    System.err.println("Errore di comunicazione: " + e.getMessage());
                }
            } while (!exit); // Continua a tentare la connessione finché exit è false
            System.out.println("Client terminato.");
        }
    
    private static void printMenu() {
        System.out.println("\nComandi disponibili:");
        System.out.println("GET_ROW <n>      - Visualizza la riga n del dataset");
        System.out.println("GET_DATE <data>  - Visualizza dati per una data (YYYY-MM-DD)");
        System.out.println("GET_ALL          - Visualizza tutti i dati (usare con cautela)");
        System.out.println("EXIT             - Termina la connessione");
    } 
}