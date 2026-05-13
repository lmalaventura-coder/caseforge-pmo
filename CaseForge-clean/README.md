# CaseForge

CaseForge e un'applicazione desktop Java per la gestione simulata di indagini investigative. Il progetto e stato sviluppato per il corso di Programmazione e Modellazione a Oggetti, con attenzione alla modellazione del dominio, alla separazione delle responsabilita e all'uso ragionato dei principali design pattern.

L'applicazione non e pensata come videogioco realtime: l'utente interpreta un investigatore che consulta dossier, scopre prove, interroga sospetti, collega informazioni e formula un'accusa finale valutata dal sistema.

## Obiettivo

L'obiettivo del progetto e realizzare una base software estendibile per gestire casi investigativi diversi, mantenendo una struttura a oggetti chiara e testabile.

In particolare, CaseForge permette di:

- caricare piu casi investigativi;
- condurre una nuova indagine pulita per ciascun caso;
- consultare sospetti, prove, timeline e interrogatori;
- scoprire prove e collegarle manualmente alle risposte ottenute;
- rilevare contraddizioni solo quando una prova scoperta smentisce una risposta;
- formulare un'accusa finale strutturata;
- salvare e caricare lo stato dell'indagine tramite JSON.

## Funzionalita principali

- Selezione del caso investigativo dalla GUI.
- Dossier con titolo, descrizione, sospetti, prove, timeline e log eventi.
- Sistema di interrogatori interattivi:
  - scelta della domanda da porre;
  - risposta visibile solo dopo la domanda;
  - risposte ottenute selezionabili per collegare prove.
- Scoperta manuale delle prove.
- Collegamento prova-risposta con rilevamento di eventuali contraddizioni.
- Riduzione dell'affidabilita del sospetto solo dopo una contraddizione confermata.
- Accusa finale basata su:
  - sospetto;
  - prova principale;
  - contraddizione principale;
  - evento rilevante della timeline.
- Reset della nuova indagine sul caso attualmente selezionato.
- Salvataggio e caricamento manuale dello stato in formato JSON.

## Architettura generale

Il progetto segue una struttura MVC.

- `model`: contiene le classi di dominio, come `CaseFile`, `Investigation`, `Suspect`, `Evidence`, `Interrogation`, `Accusation` e `EvaluationResult`.
- `view`: contiene la GUI JavaFX. La view visualizza i dati e inoltra le azioni dell'utente al controller, senza gestire la logica di dominio.
- `controller`: coordina le azioni tra view, model, factory e repository.
- `factory`: centralizza la creazione dei casi, dei sospetti e delle prove.
- `persistence`: contiene i repository in memoria e la persistence JSON delle indagini.
- `observer`: contiene gli eventi e gli observer usati per notificare cambiamenti rilevanti dell'indagine.

## Design pattern usati

- MVC: separazione tra model, controller e view JavaFX.
- Factory: `CaseFactory`, `DemoCaseFactory`, `EvidenceFactory`, `SuspectFactory`.
- Builder: `CaseFile.Builder`, usato per costruire casi investigativi complessi.
- Strategy: `AccusationEvaluationStrategy` e `StrictAccusationEvaluationStrategy`, usate per valutare l'accusa finale.
- Observer: `InvestigationObserver` e `InvestigationEvent`, usati per notificare scoperta prove, contraddizioni e chiusura caso.
- State: `InvestigationStatus`, enum che rappresenta lo stato dell'indagine.

I pattern sono usati per supportare l'architettura del progetto, non come esercizio isolato.

## Tecnologie usate

- Java 17
- Maven
- JavaFX
- JUnit 5
- Jackson per la persistence JSON
- Maven Wrapper per l'esecuzione senza Maven installato globalmente

## Requisiti per l'esecuzione

- JDK 17 o superiore.
- Sistema operativo con supporto JavaFX.
- Su Windows e possibile usare direttamente `mvnw.cmd`, incluso nel progetto.

Non e necessario installare Maven globalmente se si usa il Maven Wrapper.

## Comandi principali

Da Windows, nella cartella del progetto:

```powershell
.\mvnw.cmd test
```

per eseguire i test.

```powershell
.\mvnw.cmd javafx:run
```

per avviare l'applicazione JavaFX.

## Struttura principale

```text
CaseForge-clean/
  pom.xml
  README.md
  mvnw
  mvnw.cmd
  src/
    main/
      java/
        it/university/caseforge/
          App.java
          controller/
          factory/
          model/
          observer/
          persistence/
          view/
      resources/
        it/university/caseforge/
          caseforge-dark.css
    test/
      java/
        it/university/caseforge/
          controller/
          model/
          persistence/
```

## Casi investigativi inclusi

### Violazione di mezzanotte in HelixNova

Caso principale ambientato in una startup tecnologica. Il fondatore viene trovato ferito dopo la preparazione di un dossier di audit, mentre dai sistemi spariscono file finanziari sensibili. Il caso include piu sospetti credibili, prove digitali e fisiche, timeline estesa, interrogatori e contraddizioni.

### Il furto del prototipo

Caso piu breve ambientato in un laboratorio universitario/startup. Dopo una dimostrazione privata, un prototipo scompare dal locker tecnico. Il caso contiene tre sospetti, prove essenziali, timeline ridotta e una contraddizione rilevabile.

## Salvataggio e caricamento JSON

La GUI contiene due pulsanti nella barra delle azioni:

- `Salva indagine`
- `Carica indagine`

Il salvataggio e manuale: CaseForge non salva automaticamente lo stato quando si cambia caso.

Il file JSON contiene lo stato dell'indagine, tra cui:

- caso selezionato;
- prove scoperte;
- domande poste;
- risposte ottenute;
- collegamenti tra prove e risposte;
- contraddizioni rilevate;
- sospetto selezionato, se disponibile;
- stato dell'indagine;
- eventuale accusa finale e risultato della valutazione.

Se il file caricato non e valido, l'errore viene mostrato nel log della GUI senza chiudere l'applicazione.

## Screenshot

Placeholder per screenshot dell'applicazione:

```text
docs/screenshots/home.png
docs/screenshots/interrogatorio.png
docs/screenshots/accusa-finale.png
```

## Team e contributi

Progetto realizzato da:

- Nome Cognome - matricola
- Nome Cognome - matricola
- Nome Cognome - matricola

Contributi principali:

- Modellazione dominio: da completare
- GUI JavaFX: da completare
- Persistence JSON: da completare
- Test JUnit: da completare

## Note finali

Il progetto privilegia chiarezza, modularita e testabilita rispetto alla completezza di un prodotto reale. Alcune scelte, come la persistence JSON manuale e i casi demo hardcoded nella factory, sono intenzionali e adatte al contesto universitario.
