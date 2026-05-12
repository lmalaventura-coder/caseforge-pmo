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
- Caso demo narrativo ambientato nella startup tecnologica HelixNova, con piu sospetti, prove incrociate e timeline investigativa estesa.
- Aggiunta sospetti, prove e timeline.
- Scoperta di una prova.
- Collegamento prova-sospetto.
- Accusa finale strutturata con sospetto, prova principale, contraddizione confermata ed evento timeline rilevante.
- Valutazione finale tramite strategy che pesa tutti gli elementi dell'accusa, non solo il sospetto scelto.
- Notifica eventi per prova scoperta, collegamento e chiusura caso.
- Scheletri MVC per controller e view JavaFX.
- Sistema di interrogatori con domande categorizzate, risposte affidabili e contraddizioni rilevate quando una prova scoperta smentisce una risposta.
- Riduzione dell'affidabilita del sospetto solo dopo il collegamento manuale di una prova scoperta a una risposta effettivamente contraddittoria.
- Pannello GUI dedicato alle domande e risposte del sospetto selezionato.
- GUI riorganizzata come flusso investigativo guidato: procedura visibile, dettagli sospetto/prova, timeline e log eventi affiancati.

## Comandi Windows

```powershell
.\mvnw.cmd test
.\mvnw.cmd javafx:run
```
