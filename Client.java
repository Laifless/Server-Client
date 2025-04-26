import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 12345;
        
        try (Socket socket = new Socket(hostname, port);
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                 socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connesso al server delle maree di Venezia");
            System.out.println(in.readLine()); // Legge il messaggio di benvenuto
            
            while (true) {
                printMenu();
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("exit")) {
                    out.println("EXIT");
                    break;
                }
                
                out.println(input);
                String response = in.readLine();
                while (response != null && !response.isEmpty()) {
                    System.out.println(response);
                    response = in.readLine();
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Server non trovato: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Errore di I/O: " + e.getMessage());
        }
    }
    
    private static void printMenu() {
        System.out.println("\nComandi disponibili:");
        System.out.println("GET_ROW n - Ottieni la riga n del dataset");
        System.out.println("GET_DATE YYYY-MM-DD - Ottieni i dati per una specifica data");
        System.out.println("GET_ALL - Ottieni tutti i dati");
        System.out.println("EXIT - Termina la connessione");
        System.out.print("Inserisci comando: ");
    }
}

