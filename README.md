Progetto: App per la marea nell'anno 2023 dell'area di Punta Salute nel Canal Grande a Venenzia

Scopo: Creare un server Java utilizzando i Socket che permetta di leggere dati da un file csv

Report: 

Usiamo un protocollo TCP con porta 12345

- Server:

  Legge i dati del CSV.

  Permette di avere più Client online grazie all'estensione "Thread",
  
  Risponde ai comandi GET_ROW, GET_DATE e GET_ALL: la prima funzione restituisce una riga del codice CSV in base a quello che vuole l'utente, mentre GET_DATE in base all'inserimento di una data in formato: yyyy-mm-dd;
  mentre l'ultima funzione GET_ALL restituisce tutti i dati.

  L'interruzione del server è pulita.
  
- Client:

  Si connette al server in base ad una porta preimpostata.

  Multithreaded.

  Supporta il comando EXIT per terminare la connesione.

 - StartTideServer:

 Avvia il server in un thread separato.

 Permette di chiudere il server premendo INVIO.


GESTIONE DEGLI ERRORI: Errori di connessione: Se il server non è raggiungibile, il client mostra un messaggio di errore.

Errori nei comandi: Se un comando è malformato, il server risponde con ERROR: "descrizione".

Timeout: Non gestiti esplicitamente, ma il client può riconnettersi manualmente.
 


SCELTE PROGETTUALI:
Server multithread: Ogni client viene gestito in un thread separato (ClientHandler), permettendo connessioni multiple.

Client sincrono: Il client attende una risposta prima di inviare un nuovo comando (modello richiesta-risposta).

Gestione dei Dati
Caricamento in memoria: I dati del CSV (ps2023minmax.csv) vengono caricati all'avvio in una List<String[]>.


  
  
