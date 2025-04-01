import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
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

          /*    String[] values = line.split(",");
              
              System.out.println(values[0] + " " + values[1]);
          */ 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //SERVER

         int port = 12345; // Porta su cui il server ascolterà

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server TCP avviato sulla porta " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Connessione accettata da " + clientSocket.getInetAddress());

                    // Leggi i dati inviati dal client
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Ricevuto: " + inputLine);

                        // Invia una risposta al client
                        OutputStream out = clientSocket.getOutputStream();
                        out.write(("Echo: " + inputLine + "\n").getBytes());
                        out.flush();
                    }
                } catch (IOException e) {
                    System.err.println("Errore nella connessione con il client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nell'avvio del server: " + e.getMessage());
        }
    }
}
