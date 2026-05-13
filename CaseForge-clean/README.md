# CaseForge

CaseForge e una base Java 17/Maven per un simulatore desktop di indagini investigative.

## Struttura

```text
src/main/java/it/university/caseforge/
  App.java
  controller/
  factory/
  model/
  observer/
  persistence/
  view/
src/test/java/it/university/caseforge/model/
```

## Pattern usati

- MVC: package `model`, `controller`, `view`.
- Factory: `CaseFactory`, `DemoCaseFactory`, `EvidenceFactory`, `SuspectFactory`.
- Strategy: `AccusationEvaluationStrategy`, `StrictAccusationEvaluationStrategy`, `DeductionEngine`.
- Observer: `InvestigationObserver`, `InvestigationEvent`, eventi emessi da `Investigation`.
- Builder: `CaseFile.Builder` per costruire casi complessi.
- State: `InvestigationStatus` enum.

## Funzionalita attuali

- Creazione caso demo.
- Caso demo narrativo ambientato nella startup tecnologica HelixNova, con piu sospetti, prove incrociate e cronologia investigativa estesa.
- Aggiunta sospetti, prove e cronologia.
- Scoperta di una prova.
- Collegamento prova-sospetto.
- Accusa finale strutturata con sospetto, prova principale, contraddizione confermata ed evento della cronologia rilevante.
- Valutazione finale tramite strategy che pesa tutti gli elementi dell'accusa, non solo il sospetto scelto.
- Pulsante `Nuova indagine` per ricreare il caso demo e ripartire senza riavviare l'applicazione.
- Notifica eventi per prova scoperta, collegamento e chiusura caso.
- Scheletri MVC per controller e view JavaFX.
- Sistema di interrogatori con domande categorizzate, risposte affidabili e contraddizioni rilevate quando una prova scoperta smentisce una risposta.
- Interrogatori interattivi: l'investigatore sceglie una domanda, ottiene la risposta e solo allora puo usarla per collegare prove.
- Dossier investigativo aggiornato dinamicamente con prove scoperte, risposte raccolte, contraddizioni ed eventi della cronologia.
- Riduzione dell'affidabilita del sospetto solo dopo il collegamento manuale di una prova scoperta a una risposta effettivamente contraddittoria.
- Pannello GUI dedicato alle domande e risposte del sospetto selezionato.
- GUI riorganizzata come flusso investigativo guidato: procedura visibile, dettagli sospetto/prova, cronologia e log eventi affiancati.
- Supporto a piu casi investigativi selezionabili dalla GUI.
- Persistence JSON manuale: i pulsanti `Salva indagine` e `Carica indagine` permettono di salvare o ripristinare una sessione investigativa senza salvataggio automatico.

## Salvataggio e caricamento

La GUI contiene i pulsanti `Salva indagine` e `Carica indagine` nella barra delle azioni.

- `Salva indagine`: crea un file JSON con caso selezionato, prove scoperte, domande poste, risposte ottenute, collegamenti prova-risposta, contraddizioni, stato dell'indagine ed eventuale accusa finale.
- `Carica indagine`: legge un file JSON salvato e ricostruisce una nuova indagine coerente nella GUI.
- Il cambio caso non salva automaticamente lo stato corrente: usa `Salva indagine` prima di cambiare caso se vuoi conservarlo.
- Se il file non e valido, l'errore viene mostrato nel log eventi senza chiudere l'applicazione.

## Comandi Windows

```powershell
.\mvnw.cmd test
.\mvnw.cmd javafx:run
```
