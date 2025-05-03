
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Server extends Thread{
    private static final int PORT = 12345;
    public static final String CSV_FILE = "ps2023minmax.csv";
    private static final List<String[]> tideData = new ArrayList<>();
    
    public static void main(String[] args) {
        loadTideData();
        
        ExecutorService executor = Executors.newCachedThreadPool();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server avviato sulla porta " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Errore nel server: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
    
    private static void loadTideData() {
        tideData.clear(); // Pulisce la lista prima di caricare nuovi dati
        
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            int lineNumber = 0;
            String location = "";
    
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                // Gestione righe speciali
                if (lineNumber == 1) {
                    location = line.split(",")[0];
                    continue;
                }
                if (lineNumber == 2) continue; // Salta l'intestazione delle colonne
                
                if (line.trim().isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length >= 3) {
                    // Crea un array più grande per includere la località
                    String[] record = new String[4];
                    System.arraycopy(values, 0, record, 0, 3);
                    record[3] = location; // Aggiunge la località
                    tideData.add(record);
                }
            }
            System.out.println("Dati caricati: " + tideData.size() + " record");
        } catch (IOException e) {
            System.err.println("Errore caricamento file: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true)) {
                
                out.println("SERVER_TIDE_READY"); // Notifica che il server è pronto
                
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equalsIgnoreCase("EXIT")) {
                        break;
                    }
                    
                    String response = processRequest(inputLine);
                    out.println(response);
                }
            } catch (IOException e) {
                System.err.println("Errore nella comunicazione con il client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Errore nella chiusura del socket: " + e.getMessage());
                }
            }
        }
        
        private String processRequest(String request) {
            String[] parts = request.split(" ", 2);
            String command = parts[0].toUpperCase();
            
            try {
                switch (command) {
                    case "GET_ROW":
                        if (parts.length < 2) return "ERROR: Specificare numero riga";
                        int row = Integer.parseInt(parts[1]);
                        if (row < 0 || row >= tideData.size()) {
                            return "ERROR: Riga deve essere tra 0 e " + (tideData.size() - 1);
                        }
                        return formatRecord(tideData.get(row));

                    case "GET_DATE":
                        if (parts.length < 2) return "ERROR: Specificare data (YYYY-MM-DD)";
                        return getByDate(parts[1]);

                    default:
                        return "ERROR: Comando non valido";
                }
            } catch (NumberFormatException e) {
                return "ERROR: Numero di riga non valido";
            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        }
        
        private String getRowData(int row) {
            if (row < 0 || row >= tideData.size()) {
                return "ERROR: Numero di riga non valido (0-" + (tideData.size()-1) + ")";
            }
            return Arrays.toString(tideData.get(row));
        }
        
        private String getDateData(String date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            
            try {
                sdf.parse(date); // Verifica che la data sia valida
            } catch (Exception e) {
                return "ERROR: Formato data non valido (usare YYYY-MM-DD)";
            }
            
            StringBuilder result = new StringBuilder();
            for (String[] row : tideData) {
                if (row.length > 0 && row[0].startsWith(date)) {
                    result.append(Arrays.toString(row)).append("\n");
                }
            }
            
            return result.length() > 0 ? result.toString() : "Nessun dato trovato per la data " + date;
        }
        
        private String getDateStats(String date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            
            try {
                sdf.parse(date);
            } catch (Exception e) {
                return "ERROR: Formato data non valido (usare YYYY-MM-DD)";
            }
            
            List<Double> minTides = new ArrayList<>();
            List<Double> maxTides = new ArrayList<>();
            
            for (String[] row : tideData) {
                if (row.length >= 3 && row[0].startsWith(date)) {
                    try {
                        double value = Double.parseDouble(row[2]);
                        if (row[1].equalsIgnoreCase("min")) {
                            minTides.add(value);
                        } else if (row[1].equalsIgnoreCase("max")) {
                            maxTides.add(value);
                        }
                    } catch (NumberFormatException e) {
                        // Ignora valori non numerici
                    }
                }
            }
            
            if (minTides.isEmpty() && maxTides.isEmpty()) {
                return "Nessun dato trovato per la data " + date;
            }
            
            StringBuilder stats = new StringBuilder();
            stats.append("Statistiche per la data ").append(date).append(":\n");
            
            if (!minTides.isEmpty()) {
                double min = Collections.min(minTides);
                double max = Collections.max(minTides);
                double avg = minTides.stream().mapToDouble(d -> d).average().orElse(0);
                stats.append("Maree minime: ").append(minTides.size()).append(" rilevazioni\n");
                stats.append("  Minimo: ").append(min).append(" m\n");
                stats.append("  Massimo: ").append(max).append(" m\n");
                stats.append("  Media: ").append(String.format("%.2f", avg)).append(" m\n");
            }
            
            if (!maxTides.isEmpty()) {
                double min = Collections.min(maxTides);
                double max = Collections.max(maxTides);
                double avg = maxTides.stream().mapToDouble(d -> d).average().orElse(0);
                stats.append("Maree massime: ").append(maxTides.size()).append(" rilevazioni\n");
                stats.append("  Minimo: ").append(min).append(" m\n");
                stats.append("  Massimo: ").append(max).append(" m\n");
                stats.append("  Media: ").append(String.format("%.2f", avg)).append(" m\n");
            }
            
            return stats.toString();
        }
        
        private String getAllData() {
            StringBuilder result = new StringBuilder();
            for (String[] row : tideData) {
                result.append(Arrays.toString(row)).append("\n");
            }
            return result.toString();
        }
        private String formatRecord(String[] record) {
            try {
                return String.format("%s | %-4s | %6.2f m | %s",
                    record[0], // timestamp
                    record[1].toUpperCase(), // tipo
                    Double.parseDouble(record[2]), // valore
                    record[3] // località
                );
            } catch (Exception e) {
                return "ERROR: Formattazione fallita";
            }
        }
        
        private String getByDate(String date) {
            StringBuilder result = new StringBuilder();
            for (String[] record : tideData) {
                if (record[0].startsWith(date)) {
                    result.append(formatRecord(record)).append("\n");
                }
            }
            return result.length() > 0 ? result.toString() : "Nessun dato per " + date;
        }
        
    }
}