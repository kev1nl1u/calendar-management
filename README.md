# Calendar Manager
Calendar Manager è un progetto open-source per la gestione di un calendario utilizzabile in diversi ambiti. In questo caso è stato realizzato per la gestione degli eventi dell'IS E. Fermi di Mantova, dato che il sistema utilizzato attualmente è obsoleto, anche se minimalista e flessibile.


## Funzionamento
Calendar Manager utilizza un server multithread HTTP in Java [Server.java](https://github.com/kev1nl1u/calendar-manager/blob/main/Server.java) per la gestione delle richieste e delle risposte dalla webapp in [./public](https://github.com/kev1nl1u/calendar-manager/tree/main/public).


### Server
Il server è in ascolto sulla porta **8080** e gestisce le richieste HTTP _GET_ e _POST_ dalle diverse funzioni della Webapp.
- Le richieste _GET_ vengono utilizzate per ottenere i file per la visualizzazione della webapp;
- Le richieste _POST_ vengono utilizzate ricavare i dati degli eventi e la modifica del calendario.
Il server salva i dati in un file XML [calendar.xml](https://github.com/kev1nl1u/calendar-manager/blob/main/calendar.xml) validato con il file [calendar.xsd](https://github.com/kev1nl1u/calendar-manager/blob/main/calendar.xsd).


#### Classe Worker
La classe `Worker` estende `Thread` e si occupa di gestire ogni client che si connette al server e gestisce le sue richieste.

Dopo aver fatto partire il thread, legge la prima riga della richiesta per riconoscere il metodo e il path della richiesta. In base a queste informazioni, chiama il metodo corrispondente per gestire la richiesta.


##### Worker.get(_params_)
Il metodo `Worker.get(params)` si occupa di gestire le richieste _GET_.

In particolare, se il path non contiene `/common/` aggiunge `.html` al path, in modo da eliminare l'estensione nell'URL (es. `localhost:8080/edit` invece di `localhost:8080/edit.html`). In seguito cerca il file nella cartella `./public/` e lo invia al client. Se il file non esiste, restituisce un errore 404.


##### Worker.post(_params_)
Il metodo `Worker.post(params)` si occupa di gestire le richieste _POST_.

Legge il corpo della richiesta per individuare i dati passati con POST.

A seconda del path, esegue il codice necessario per elaborare una risposta.
<ul>
<li><code>/post/getEvents</code>
	<ol type="1">
		<li>viene passato con POST la data</li>
		<li>cerca tutti gli eventi nel file XML con <code>getXMLEventsByDate(params)</code> che hanno quella data</li>
		<li>converte i risultati in JSON con <code>nodeList2Json(params)</code> fatto su misura per <code>calendar.xml</code></li>
		<li>invia al client in formato JSON</li>
	</ol>
</li>
<li>
	<code>/post/addEvent</code> --> <code>Worker.addEvent(params)</code>
	<ol type="1">
		<li>viene passato con POST i dati dell'evento in JSON</li>
		<li>suddivide e inserisce i dati in un <code>HashMap</code></li>
		<li>crea un nuovo nodo XML in cui vengono inseriti le key come tag e i valori come testo attraverso <code>Worker.setEventParams(params)</code></li>
		<li>aggiunge l'evento al file XML</li>
	</ol>
</li>
<li>
	<code>/post/deleteEvent</code> --> <code>Worker.deleteEvent(params)</code>
	<ol type="1">
		<li>viene passato con POST l'id dell'evento</li>
		<li>cerca l'evento nel file XML</li>
		<li>elimina l'evento</li>
	</ol>
</li>
</ul>

> _params_ si riferisce ai parametri passati dei vari metodi.


## Criticità
Il server è stato realizzato per scopi didattici e non è stato testato in un ambiente di produzione.

Inoltre, XML è molto vulnerabile a attacchi di tipo _XXE_ (XML eXternal Entity). Per questo motivo, è sconsigliato l'utilizzo di XML per la memorizzazione di dati sensibili o importanti. È più consigliato utilizzare un database SQL in un ambiente di produzione.

Infine, il codice è stato scritto in maniera molto semplice a causa dei tempi stretti e non è stato ottimizzato per la velocità o per la sicurezza.


## Contatti
Per qualsiasi dubbio o domanda, contattami [qui](https://blog.davidesirico.studio).


# Licenza

[![CC BY-SA L](https://i.creativecommons.org/l/by-sa/4.0/80x15.png)](https://creativecommons.org/licenses/by-sa/4.0/deed.it)

Quest'opera è distribuita con licenza [Creative Commons Attribution-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-sa/4.0/).

[![CC BY-SA](https://i.creativecommons.org/l/by-sa/4.0/88x31.png)](https://creativecommons.org/licenses/by-sa/4.0/deed.it)
