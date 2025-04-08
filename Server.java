
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

    public void run() {
        //SERVER

        int port = 12345; // Porta su cui il server ascolterà

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server TCP avviato sulla porta " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Connessione accettata da " + clientSocket.getInetAddress());

                    // Leggi i dati inviati dal client
                    readFile();
                       
                        // Invia una risposta al client
                         try (OutputStream out = clientSocket.getOutputStream()) {
                        PrintWriter writer = new PrintWriter(out, true);
                        writer.println("Inserire data per controllare la marea: ");
                        writer.println("Formato: YYYY-MM-DD");
                        writer.println("Esempio: 2023-10-01");
                        writer.println("Per terminare il programma digitare 'exit'");
                        writer.flush();
                        // Leggi la risposta del client
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            if (inputLine.equalsIgnoreCase("exit")) {
                                System.out.println("Chiusura del server.");
                                break;
                            }
                            // elaborare l'input del client
                            System.out.println("Ricevuto dal client: " + inputLine);
                            // risposta al client
                            writer.println("Hai inserito: " + inputLine);
                        }
                        // Chiudi il flusso di output
                        writer.close();
                        // Chiudi il flusso di input
                        in.close();
                        // Chiudi il socket del client
                        clientSocket.close();
                        System.out.println("Connessione chiusa con " + clientSocket.getInetAddress());
                    } catch (IOException ex) {
                        System.err.println("Errore nella lettura/scrittura con il client: " + ex.getMessage());
                    } finally {
                        // Chiudi il socket del client
                        if (!clientSocket.isClosed()) {
                            clientSocket.close();
                        }
                        System.out.println("Socket del client chiuso.");
                    }
                    

                } catch (IOException ex) {
                    System.err.println("Errore nella connessione con il client: " + ex.getMessage());
                }
                }
            } catch (IOException ex) {
        }
         
    }

    private void readFile() {
        //Leggi file 

        String filePath = new File("ps2023minmax.csv").getAbsolutePath();
        boolean isFirstLine = true;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                  String[] values = line.split(",");
              
              System.out.println(values[0] + " " + values[1]);
                 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
