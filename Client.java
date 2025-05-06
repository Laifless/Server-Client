import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final int TIMEOUT = 10000; // 10 secondi
    private static volatile boolean exit = false; // Variabile condivisa per controllare l'uscita

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== CLIENT MARE VENEZIA ===");

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 4578), TIMEOUT);
            socket.setSoTimeout(TIMEOUT);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                System.out.println("Connesso al server. " + in.readLine());

                // Thread per leggere le risposte dal server
                Thread readerThread = new Thread(() -> {
                    try {
                        String response;
                        while (!exit && (response = in.readLine()) != null) {
                            System.out.println("\n=== RISPOSTA DAL SERVER ===");
                            System.out.println(response);
                        }
                    } catch (IOException e) {
                        if (!exit) {
                            System.err.println("Errore durante la lettura dal server: " + e.getMessage());
                        }
                    }
                });

                readerThread.start(); // Avvia il thread per la lettura

                // Ciclo principale per inviare comandi
                while (!exit) {
                    printMenu();
                    System.out.print("> ");
                    String input = scanner.nextLine().trim();

                    out.println(input); // Invia il comando al server
                    if (input.equalsIgnoreCase("EXIT")) {
                        exit = true; // Segnala l'uscita
                        break;
                    }
                }

                // Aspetta che il thread di lettura termini
                readerThread.join();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore: " + e.getMessage());
        } finally {
            System.out.println("Client terminato.");
        }
    }

    private static void printMenu() {
        System.out.println("\n=== MENU ===");
        System.out.println("1. GET_ROW <n>");
        System.out.println("2. GET_DATE <date>");
        System.out.println("3. GET_ALL      -non consigliato");
        System.out.println("3. EXIT");
    }
}