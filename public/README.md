# Webapp
La webapp è stata realizzata con HTML, CSS e JavaScript. La struttura è divisa in tre parti principali:
- **index.html**: la pagina principale, con il calendario e la lista degli eventi;
- **edit.html**: la pagina per la modifica degli eventi;
- **./common/**: la cartella con le risorse comuni, tra cui CSS, JavaScript e immagini.

## Calendario
Il calendario della webapp è stato realizzato con JavaScript e jQuery. Il suo funzionamento è controllato da `./common/js/calendar.js`.

Viene istanziato nel file **HTML** un oggetto `CalendarControl` che è il controller di tutte le azioni che si possono eseguire sul calendario, come lo spostamento tra le date e il caricamento degli eventi.
Il costruttore ha come parametro `is_editing`, che può essere `true` o `false` e serve per abilitare o disabilitare la modifica degli eventi.

CalendarControl ha 3 Date:
- <code>this.view</code>: la data visualizzata nel calendario
- <code>this.selected</code>: la data selezionata, viene usato quando si vuole muoversi nel calendario con <code>this.view</code> pure senza selezionando una data
- <code>this.localDate</code>: per salvarsi la data locale, in modo da poterla confrontare con le altre due in alcuni casi.

### Metodi più importanti

#### CalendarControl.init()
Inizializza il calendario, scrivendo il contenuto in HTML e assegnando i listener per le azioni.

#### CalendarControl.loadEvents()
Fa una richiesta POST al server per ottenere gli eventi di una data. La data viene ricavata dalla variabile <code>this.selected</code>. Se la richiesta va a buon fine, elabora i dati JSON ricevuti e scrive il contenuto nel calendario in HTML.

#### Metodi di navigazione
6 metodi per navigare nel calendario, per cambiare giorno, mese, per selezionare una data e per tornare al giorno corrente.