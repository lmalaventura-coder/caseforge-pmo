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
- Aggiunta sospetti, prove e timeline.
- Scoperta di una prova.
- Collegamento prova-sospetto.
- Accusa finale con valutazione tramite strategy.
- Notifica eventi per prova scoperta, collegamento e chiusura caso.
- Scheletri MVC per controller e view JavaFX.
- Sistema di interrogatori con domande categorizzate, risposte affidabili e contraddizioni rilevate quando una prova scoperta smentisce una risposta.
- Riduzione dell'affidabilita del sospetto quando emerge una contraddizione.
- Pannello GUI dedicato alle domande e risposte del sospetto selezionato.

## Comandi Windows

```powershell
.\mvnw.cmd test
.\mvnw.cmd javafx:run
```
