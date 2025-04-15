
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server extends Thread {

    public void run() {
        // SERVER

        int port = 12345; // Porta su cui il server ascolterà

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server TCP avviato sulla porta " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Connessione accettata da " + clientSocket.getInetAddress());

                    // Invia una risposta al client
                    try (OutputStream out = clientSocket.getOutputStream()) {
                        PrintWriter writer = new PrintWriter(out, true);
                        writer.println("Inserire data per controllare la marea: ");
                        writer.println("Formato: YYYY-MM-DD");
                        writer.println("Esempio: 2023-01-01 14:05:00");
                        writer.println("Per terminare il programma digitare 'exit'");
                        writer.flush();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String requestedDate = in.readLine(); // Legge la data inviata dal client
                        if (requestedDate != null && !requestedDate.equalsIgnoreCase("exit")) {
                            readFile(requestedDate); // Passa la data al metodo readFile
                        }
                        // Leggi la risposta del client
                        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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

    private void readFile(String requestedDate) {
        String filePath = "ps2023minmax.csv";
        double minTide = Double.MAX_VALUE;
        double maxTide = Double.MIN_VALUE;
        String minTideTime = "";
        String maxTideTime = "";
        boolean dateFound = false;
    
        System.out.println("File: " + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Il file non esiste.");
            return;
        } else {
            System.out.println("Il file esiste.");
        }
    
        try {
            // Rimuove spazi indesiderati da requestedDate
            requestedDate = requestedDate.trim();
            System.out.println("Requested date: '" + requestedDate + "'");
    
            // Verifica il formato della data
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            try {
                sdf.parse(requestedDate); // Controlla che la data sia valida
            } catch (Exception e) {
                System.err.println("Formato data non valido: " + requestedDate);
                return;
            }
    
            // Lettura del file
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineNumber = 0; // Contatore per le righe
    
                while ((line = br.readLine()) != null) {
                    lineNumber++;
    
                    // Salta le prime due righe
                    if (lineNumber <= 2) {
                        continue;
                    }
    
                    // Ignora righe vuote
                    if (line.trim().isEmpty()) {
                        continue;
                    }
    
                    String[] values = line.split(",");
                    if (values.length < 2 || values[0].trim().isEmpty() || values[1].trim().isEmpty()) {
                        System.err.println("Riga malformata o incompleta alla riga " + lineNumber + ": " + line);
                        continue;
                    }
    
                    String dateTime = values[0].trim(); // Prima colonna: data e orario
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dateTimeFormat.setLenient(false);
    
                    try {
                        Date parsedDate = dateTimeFormat.parse(dateTime); // Parsing della data e orario
                        String datePart = new SimpleDateFormat("yyyy-MM-dd").format(parsedDate); // Estrai solo la data
                        String timePart = new SimpleDateFormat("HH:mm:ss").format(parsedDate); // Estrai solo l'orario
    
                        // Confronta la data letta con la requestedDate
                        if (datePart.equals(requestedDate)) {
                            dateFound = true;
    
                            // Parsing del valore della marea (seconda colonna)
                            try {
                                double tide = Double.parseDouble(values[1].trim());
                                if (tide < minTide) {
                                    minTide = tide;
                                    minTideTime = timePart;
                                }
                                if (tide > maxTide) {
                                    maxTide = tide;
                                    maxTideTime = timePart;
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Valore di marea non valido alla riga " + lineNumber + ": " + values[1]);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Formato data e orario non valido alla riga " + lineNumber + ": " + dateTime);
                    }
                }
    
                // Risultati finali
                if (dateFound) {
                    System.out.println("Data: " + requestedDate);
                    System.out.println("Marea minima: " + minTide + " alle " + minTideTime);
                    System.out.println("Marea massima: " + maxTide + " alle " + maxTideTime);
                } else {
                    System.out.println("Nessun dato trovato per la data: " + requestedDate);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file: " + e.getMessage());
        }
    }
}