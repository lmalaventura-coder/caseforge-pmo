package it.university.caseforge.view;

import it.university.caseforge.controller.CaseController;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.Contradiction;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.model.Evidence;
import it.university.caseforge.model.Interrogation;
import it.university.caseforge.model.Question;
import it.university.caseforge.model.Suspect;
import it.university.caseforge.model.TimelineEvent;
import it.university.caseforge.observer.InvestigationEvent;
import it.university.caseforge.observer.InvestigationEventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JavaFxCaseView implements CaseView {

    private static final DateTimeFormatter TIMELINE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BorderPane root = new BorderPane();
    private final Label titleLabel = new Label("CaseForge");
    private final Label descriptionLabel = new Label();
    private final Label statusLabel = new Label("In attesa del caso demo.");
    private final Label selectedSuspectLabel = new Label("Sospetto selezionato: nessuno");
    private final Label selectedEvidenceLabel = new Label("Prova selezionata: nessuna");

    private final ListView<SuspectItem> suspectList = new ListView<>();
    private final ListView<EvidenceItem> evidenceList = new ListView<>();
    private final ListView<String> interrogationList = new ListView<>();
    private final ComboBox<AnswerLinkItem> answerComboBox = new ComboBox<>();
    private final ListView<String> timelineList = new ListView<>();
    private final ComboBox<SuspectItem> accusedSuspectComboBox = new ComboBox<>();
    private final ComboBox<EvidenceAccusationItem> primaryEvidenceComboBox = new ComboBox<>();
    private final ComboBox<ContradictionItem> primaryContradictionComboBox = new ComboBox<>();
    private final ComboBox<TimelineEventItem> relevantTimelineComboBox = new ComboBox<>();

    private final TextArea suspectDetailsArea = readOnlyArea();
    private final TextArea evidenceDetailsArea = readOnlyArea();
    private final TextArea logArea = readOnlyArea();

    private final Button discoverEvidenceButton = new Button("Scopri prova selezionata");
    private final Button linkEvidenceButton = new Button("Collega prova selezionata alla risposta selezionata");
    private final Button formulateAccusationButton = new Button("Formula accusa");

    private CaseController controller;
    private CaseFile currentCaseFile;
    private boolean caseLoaded;
    private boolean refreshingView;

    public JavaFxCaseView() {
        configureRoot();
        configureActions();
        appendLog("Interfaccia pronta. Segui la procedura investigativa dall'alto verso il basso.");
    }

    public void bindController(CaseController controller) {
        this.controller = Objects.requireNonNull(controller);
    }

    public Parent getRoot() {
        return root;
    }

    @Override
    public void showCase(CaseFile caseFile) {
        refreshingView = true;
        try {
            currentCaseFile = caseFile;
            String selectedSuspectId = selectedSuspectId();
            String selectedEvidenceId = selectedEvidenceId();
            String accusedSuspectId = selectedAccusedSuspectId();
            String primaryEvidenceId = selectedPrimaryEvidenceId();
            String primaryContradictionId = selectedPrimaryContradictionId();
            String relevantTimelineEventId = selectedRelevantTimelineEventId();

            titleLabel.setText(caseFile.getTitle());
            descriptionLabel.setText(caseFile.getDescription());

            suspectList.getItems().setAll(caseFile.getSuspects().stream()
                    .map(suspect -> toSuspectItem(caseFile, suspect))
                    .toList());
            evidenceList.getItems().setAll(caseFile.getEvidences().stream()
                    .map(evidence -> toEvidenceItem(caseFile, evidence))
                    .toList());
            timelineList.getItems().setAll(caseFile.getTimeline().getEvents().stream()
                    .map(this::formatTimelineEvent)
                    .toList());

            accusedSuspectComboBox.getItems().setAll(suspectList.getItems());
            primaryEvidenceComboBox.getItems().setAll(caseFile.getEvidences().stream()
                    .filter(Evidence::isDiscovered)
                    .map(this::toEvidenceAccusationItem)
                    .toList());
            relevantTimelineComboBox.getItems().setAll(caseFile.getTimeline().getEvents().stream()
                    .map(this::toTimelineEventItem)
                    .toList());

            restoreSuspectSelection(selectedSuspectId);
            restoreEvidenceSelection(selectedEvidenceId);
            restoreAccusedSuspectSelection(accusedSuspectId);
            restorePrimaryEvidenceSelection(primaryEvidenceId);
            refreshPrimaryContradictionOptions(primaryContradictionId);
            restoreRelevantTimelineSelection(relevantTimelineEventId);

            if (suspectList.getSelectionModel().getSelectedItem() == null && !suspectList.getItems().isEmpty()) {
                suspectList.getSelectionModel().selectFirst();
            }

            refreshSelectedSuspectPresentation();
            refreshSelectedEvidencePresentation();
        } finally {
            refreshingView = false;
        }

        if (!caseLoaded) {
            statusLabel.setText("Caso demo caricato.");
            appendLog("Caso demo caricato. Inizia selezionando un sospetto, poi esamina prove e timeline.");
            caseLoaded = true;
        }
    }

    @Override
    public void showInvestigationEvent(InvestigationEvent event) {
        String message = formatInvestigationEvent(event);
        statusLabel.setText(message);
        appendLog(message);
    }

    @Override
    public void showEvaluationResult(EvaluationResult result) {
        String message = result.getMessage() + " Score: " + result.getScore();
        if (!result.getMissingEvidenceIds().isEmpty()) {
            message += " | Prove mancanti: " + String.join(", ", result.getMissingEvidenceIds());
        }

        statusLabel.setText(message);
        appendLog(message);
    }

    private void configureRoot() {
        root.setPadding(new Insets(18));
        root.setTop(buildHeader());
        root.setCenter(buildInvestigationWorkspace());
        root.setBottom(buildActionArea());
    }

    private VBox buildHeader() {
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 13px;");
        statusLabel.setStyle("-fx-font-weight: bold;");

        VBox header = new VBox(10,
                titleLabel,
                descriptionLabel,
                statusLabel,
                buildProcedurePanel()
        );
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private VBox buildProcedurePanel() {
        Label heading = sectionHeading("Procedura investigativa");
        HBox steps = new HBox(10,
                procedureStep("1. Seleziona un sospetto"),
                procedureStep("2. Esamina prove e timeline"),
                procedureStep("3. Interroga il sospetto"),
                procedureStep("4. Collega prove alle risposte"),
                procedureStep("5. Formula accusa")
        );
        steps.setAlignment(Pos.CENTER_LEFT);

        VBox procedure = new VBox(8, heading, steps);
        return procedure;
    }

    private Label procedureStep(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(150);
        label.setStyle("-fx-background-color: #eef3f6; "
                + "-fx-border-color: #ccd6df; "
                + "-fx-padding: 8 10 8 10;");
        return label;
    }

    private HBox buildInvestigationWorkspace() {
        VBox leftColumn = buildSuspectColumn();
        VBox centerColumn = buildCenterColumn();
        VBox rightColumn = buildRightColumn();

        leftColumn.setPrefWidth(320);
        rightColumn.setPrefWidth(360);
        HBox.setHgrow(centerColumn, Priority.ALWAYS);

        HBox workspace = new HBox(16, leftColumn, centerColumn, rightColumn);
        VBox.setVgrow(workspace, Priority.ALWAYS);
        return workspace;
    }

    private VBox buildSuspectColumn() {
        suspectList.setPrefHeight(250);
        suspectDetailsArea.setPrefRowCount(14);

        VBox column = new VBox(10,
                sectionHeading("Sospetti e affidabilita"),
                suspectList,
                sectionHeading("Profilo del sospetto"),
                suspectDetailsArea
        );
        VBox.setVgrow(suspectDetailsArea, Priority.ALWAYS);
        return column;
    }

    private VBox buildCenterColumn() {
        evidenceList.setPrefWidth(360);
        evidenceDetailsArea.setPrefRowCount(12);
        interrogationList.setPrefHeight(220);
        answerComboBox.setPromptText("Risposta dell'interrogatorio");
        answerComboBox.setPrefWidth(460);

        VBox evidenceListPanel = new VBox(8,
                sectionHeading("Prove disponibili e scoperte"),
                evidenceList
        );
        VBox.setVgrow(evidenceList, Priority.ALWAYS);

        VBox evidenceDetailsPanel = new VBox(8,
                sectionHeading("Dettagli della prova selezionata"),
                evidenceDetailsArea
        );
        HBox.setHgrow(evidenceDetailsPanel, Priority.ALWAYS);

        HBox evidenceArea = new HBox(12, evidenceListPanel, evidenceDetailsPanel);
        VBox.setVgrow(evidenceArea, Priority.ALWAYS);

        VBox interrogationPanel = new VBox(8,
                sectionHeading("Interrogatori del sospetto selezionato"),
                interrogationList,
                sectionHeading("Risposta da collegare"),
                answerComboBox
        );
        VBox.setVgrow(interrogationList, Priority.ALWAYS);

        VBox center = new VBox(14, evidenceArea, interrogationPanel);
        VBox.setVgrow(evidenceArea, Priority.ALWAYS);
        return center;
    }

    private VBox buildRightColumn() {
        timelineList.setPrefHeight(280);
        logArea.setPrefRowCount(14);

        VBox timelinePanel = new VBox(8,
                sectionHeading("Timeline investigativa"),
                timelineList
        );
        VBox.setVgrow(timelineList, Priority.ALWAYS);

        VBox logPanel = new VBox(8,
                sectionHeading("Log eventi investigativi"),
                logArea
        );
        VBox.setVgrow(logArea, Priority.ALWAYS);

        VBox right = new VBox(14, timelinePanel, logPanel);
        VBox.setVgrow(logPanel, Priority.ALWAYS);
        return right;
    }

    private VBox buildActionArea() {
        selectedSuspectLabel.setWrapText(true);
        selectedEvidenceLabel.setWrapText(true);

        HBox selectionSummary = new HBox(18, selectedSuspectLabel, selectedEvidenceLabel);
        selectionSummary.setAlignment(Pos.CENTER_LEFT);

        accusedSuspectComboBox.setPromptText("Sospetto accusato");
        accusedSuspectComboBox.setPrefWidth(240);
        primaryEvidenceComboBox.setPromptText("Prova principale");
        primaryEvidenceComboBox.setPrefWidth(250);
        primaryContradictionComboBox.setPromptText("Contraddizione principale");
        primaryContradictionComboBox.setPrefWidth(360);
        relevantTimelineComboBox.setPromptText("Evento timeline rilevante");
        relevantTimelineComboBox.setPrefWidth(300);
        discoverEvidenceButton.setPrefWidth(190);
        linkEvidenceButton.setPrefWidth(320);
        linkEvidenceButton.setWrapText(true);
        formulateAccusationButton.setPrefWidth(150);

        HBox investigativeActions = new HBox(12,
                discoverEvidenceButton,
                linkEvidenceButton
        );
        investigativeActions.setAlignment(Pos.CENTER_LEFT);

        HBox accusationSelectors = new HBox(12,
                accusationField("Sospetto", accusedSuspectComboBox),
                accusationField("Prova principale", primaryEvidenceComboBox),
                accusationField("Contraddizione", primaryContradictionComboBox),
                accusationField("Evento timeline", relevantTimelineComboBox),
                formulateAccusationButton
        );
        accusationSelectors.setAlignment(Pos.BOTTOM_LEFT);

        VBox actionArea = new VBox(10,
                sectionHeading("Azioni investigative"),
                selectionSummary,
                investigativeActions,
                sectionHeading("Accusa strutturata"),
                accusationSelectors
        );
        actionArea.setPadding(new Insets(16, 0, 0, 0));
        return actionArea;
    }

    private void configureActions() {
        suspectList.getSelectionModel().selectedItemProperty().addListener((observable, previous, current) -> {
            refreshSelectedSuspectPresentation();
            if (!refreshingView && current != null) {
                appendLog("Sospetto selezionato: " + current.name()
                        + ". Consulta movente, alibi, prove collegate e risposte disponibili.");
            }
        });

        evidenceList.getSelectionModel().selectedItemProperty().addListener((observable, previous, current) -> {
            refreshSelectedEvidencePresentation();
            if (!refreshingView && current != null) {
                appendLog("Prova selezionata: " + current.title()
                        + ". Valuta se scoprirla o collegarla alla risposta selezionata.");
            }
        });

        accusedSuspectComboBox.valueProperty().addListener((observable, previous, current) -> {
            if (!refreshingView) {
                refreshPrimaryContradictionOptions(null);
                if (current != null) {
                    appendLog("Sospetto scelto per l'accusa: " + current.name()
                            + ". Ora seleziona prova, contraddizione ed evento timeline.");
                }
            }
        });

        discoverEvidenceButton.setOnAction(event -> withController(() -> {
            EvidenceItem evidence = evidenceList.getSelectionModel().getSelectedItem();
            if (evidence == null) {
                appendLog("Per scoprire una prova, selezionala prima dal pannello centrale.");
                return;
            }
            appendLog("Richiesta scoperta prova: " + evidence.title() + ".");
            controller.discoverEvidence(evidence.id());
        }));

        linkEvidenceButton.setOnAction(event -> withController(() -> {
            EvidenceItem evidence = evidenceList.getSelectionModel().getSelectedItem();
            AnswerLinkItem answer = answerComboBox.getValue();

            if (evidence == null && answer == null) {
                appendLog("Per collegare una prova a una risposta, seleziona prima entrambe.");
                return;
            }
            if (evidence == null) {
                appendLog("Per collegare una risposta, seleziona anche la prova da analizzare.");
                return;
            }
            if (answer == null) {
                appendLog("Per collegare la prova, scegli una risposta dell'interrogatorio del sospetto selezionato.");
                return;
            }

            appendLog("Richiesta collegamento prova-risposta: " + evidence.title()
                    + " -> " + answer.answerPreview() + ".");
            controller.linkEvidenceToAnswer(
                    evidence.id(),
                    answer.suspectId(),
                    answer.interrogationId(),
                    answer.questionId()
            );
        }));

        formulateAccusationButton.setOnAction(event -> withController(() -> {
            SuspectItem accusedSuspect = accusedSuspectComboBox.getValue();
            if (accusedSuspect == null) {
                appendLog("Prima di formulare l'accusa, scegli un sospetto dalla combo dedicata.");
                return;
            }
            EvidenceAccusationItem primaryEvidence = primaryEvidenceComboBox.getValue();
            if (primaryEvidence == null) {
                appendLog("Per formulare l'accusa, seleziona una prova principale gia scoperta.");
                return;
            }
            ContradictionItem primaryContradiction = primaryContradictionComboBox.getValue();
            if (primaryContradiction == null) {
                appendLog("Per formulare l'accusa, seleziona una contraddizione principale gia rilevata.");
                return;
            }
            TimelineEventItem relevantTimelineEvent = relevantTimelineComboBox.getValue();
            if (relevantTimelineEvent == null) {
                appendLog("Per formulare l'accusa, seleziona un evento timeline rilevante.");
                return;
            }

            appendLog("Accusa strutturata formulata contro " + accusedSuspect.name()
                    + " con prova, contraddizione ed evento timeline selezionati.");
            controller.submitAccusation(
                    accusedSuspect.id(),
                    primaryEvidence.id(),
                    primaryContradiction.id(),
                    relevantTimelineEvent.id(),
                    "Accusa formulata dalla GUI con dossier strutturato."
            );
        }));
    }

    private SuspectItem toSuspectItem(CaseFile caseFile, Suspect suspect) {
        List<String> linkedEvidenceTitles = caseFile.getEvidences().stream()
                .filter(evidence -> evidence.isLinkedTo(suspect.getId()))
                .map(Evidence::getTitle)
                .toList();
        List<String> contradictionLines = suspect.getInterrogations().stream()
                .flatMap(interrogation -> interrogation.getContradictions().stream())
                .map(this::formatContradictionLine)
                .toList();
        List<String> interrogationLines = buildInterrogationLines(suspect);
        List<AnswerLinkItem> answerLinkItems = buildAnswerLinkItems(suspect);

        String detailText = String.join(System.lineSeparator(),
                "Movente: " + suspect.getMotive(),
                "",
                "Alibi: " + suspect.getAlibi(),
                "",
                "Affidabilita: " + reliabilityCategory(suspect.getReliabilityScore()),
                "",
                "Prove gia collegate: " + joinedOrFallback(linkedEvidenceTitles, "nessuna"),
                "",
                "Interrogatori disponibili: " + suspect.getInterrogations().size(),
                "",
                "Contraddizioni trovate: " + joinedOrFallback(contradictionLines, "nessuna")
        );

        String label = suspect.getName()
                + " | affidabilita "
                + reliabilityCategory(suspect.getReliabilityScore());
        return new SuspectItem(
                suspect.getId(),
                suspect.getName(),
                label,
                detailText,
                interrogationLines,
                answerLinkItems
        );
    }

    private EvidenceItem toEvidenceItem(CaseFile caseFile, Evidence evidence) {
        List<String> linkedSuspects = evidence.getLinkedSuspectIds().stream()
                .map(suspectId -> caseFile.findSuspectById(suspectId)
                        .map(Suspect::getName)
                        .orElse(suspectId))
                .toList();

        String discoveryStatus = evidence.isDiscovered() ? "scoperta" : "non scoperta";
        String label = evidence.getTitle() + " | " + evidence.getType() + " | " + discoveryStatus;
        String detailText = String.join(System.lineSeparator(),
                "Titolo: " + evidence.getTitle(),
                "Tipo: " + evidence.getType(),
                "Stato: " + discoveryStatus,
                "",
                "Descrizione: " + evidence.getDescription(),
                "",
                "Sospetti collegati: " + joinedOrFallback(linkedSuspects, "nessuno"),
                "",
                "Perche potrebbe essere rilevante: " + evidenceRelevance(linkedSuspects, evidence.isDiscovered())
        );

        return new EvidenceItem(evidence.getId(), evidence.getTitle(), label, detailText);
    }

    private EvidenceAccusationItem toEvidenceAccusationItem(Evidence evidence) {
        String label = evidence.getTitle() + " | " + evidence.getType();
        return new EvidenceAccusationItem(evidence.getId(), evidence.getTitle(), label);
    }

    private TimelineEventItem toTimelineEventItem(TimelineEvent event) {
        return new TimelineEventItem(event.getId(), formatTimelineEvent(event));
    }

    private List<String> buildInterrogationLines(Suspect suspect) {
        if (suspect.getInterrogations().isEmpty()) {
            return List.of("Nessun interrogatorio disponibile per il sospetto selezionato.");
        }

        List<String> lines = new ArrayList<>();
        for (Interrogation interrogation : suspect.getInterrogations()) {
            lines.add("Interrogatorio " + interrogation.getId()
                    + " | " + TIMELINE_FORMATTER.format(interrogation.getStartedAt()));
            for (Question question : interrogation.getQuestions()) {
                lines.add("Q [" + question.getCategory() + "]: " + question.getText());
                if (question.getAnswer() != null) {
                    lines.add("A [" + question.getAnswer().getReliabilityLevel() + "]: "
                            + question.getAnswer().getText());
                }
            }
            for (Contradiction contradiction : interrogation.getContradictions()) {
                lines.add("Contraddizione: " + formatContradictionLine(contradiction));
            }
        }
        return lines;
    }

    private List<AnswerLinkItem> buildAnswerLinkItems(Suspect suspect) {
        List<AnswerLinkItem> items = new ArrayList<>();
        for (Interrogation interrogation : suspect.getInterrogations()) {
            for (Question question : interrogation.getQuestions()) {
                if (question.getAnswer() != null) {
                    items.add(new AnswerLinkItem(
                            suspect.getId(),
                            interrogation.getId(),
                            question.getId(),
                            question.getText(),
                            question.getAnswer().getText()
                    ));
                }
            }
        }
        return items;
    }

    private String formatTimelineEvent(TimelineEvent event) {
        return TIMELINE_FORMATTER.format(event.getOccurredAt())
                + " | " + event.getTitle()
                + " | " + event.getDescription();
    }

    private String formatInvestigationEvent(InvestigationEvent event) {
        if (event.getType() == InvestigationEventType.CONTRADICTION_DETECTED) {
            return event.getContradiction()
                    .map(contradiction -> "Contraddizione rilevata: "
                            + formatContradictionLine(contradiction))
                    .orElse(event.getMessage());
        }

        if (event.getType() == InvestigationEventType.EVIDENCE_LINKED_TO_ANSWER) {
            return event.getEvidence()
                    .map(evidence -> "Prova collegata alla risposta: " + evidence.getTitle() + ".")
                    .orElse("Prova collegata alla risposta.");
        }

        if (event.getType() == InvestigationEventType.NO_CONTRADICTION_DETECTED) {
            return event.getEvidence()
                    .map(evidence -> "Nessuna contraddizione rilevata per la prova: " + evidence.getTitle() + ".")
                    .orElse("Nessuna contraddizione rilevata.");
        }

        if (event.getType() == InvestigationEventType.EVIDENCE_LINKED_TO_SUSPECT) {
            return event.getEvidence()
                    .map(evidence -> "Collegamento registrato per la prova: " + evidence.getTitle() + ".")
                    .orElse(event.getMessage());
        }

        if (event.getType() == InvestigationEventType.EVIDENCE_DISCOVERED) {
            return event.getEvidence()
                    .map(evidence -> "Prova scoperta: " + evidence.getTitle() + ".")
                    .orElse("Prova scoperta.");
        }

        return event.getEvidence()
                .map(evidence -> event.getMessage() + " " + evidence.getTitle())
                .orElse(event.getMessage());
    }

    private String formatContradictionLine(Contradiction contradiction) {
        return contradiction.getEvidence().getTitle()
                + " smentisce la risposta: " + contradiction.getAnswer().getText();
    }

    private String evidenceRelevance(List<String> linkedSuspects, boolean discovered) {
        if (!linkedSuspects.isEmpty()) {
            return "aiuta a confrontare " + String.join(", ", linkedSuspects)
                    + " con timeline, accessi e dichiarazioni.";
        }
        if (discovered) {
            return "e una prova gia emersa da valutare insieme alla timeline e agli interrogatori.";
        }
        return "scoprirla puo chiarire accessi, movimenti o alibi ancora aperti.";
    }

    private void refreshSelectedSuspectPresentation() {
        SuspectItem suspect = suspectList.getSelectionModel().getSelectedItem();
        if (suspect == null) {
            suspectDetailsArea.setText("Seleziona un sospetto per visualizzare movente, alibi, affidabilita e prove collegate.");
            interrogationList.getItems().setAll("Gli interrogatori compariranno dopo la selezione di un sospetto.");
            answerComboBox.getItems().clear();
            answerComboBox.setValue(null);
            selectedSuspectLabel.setText("Sospetto selezionato: nessuno");
            return;
        }

        suspectDetailsArea.setText(suspect.detailText());
        interrogationList.getItems().setAll(suspect.interrogationLines());
        answerComboBox.getItems().setAll(suspect.answerLinkItems());
        answerComboBox.setValue(null);
        if (!answerComboBox.getItems().isEmpty()) {
            answerComboBox.getSelectionModel().selectFirst();
        }
        selectedSuspectLabel.setText("Sospetto selezionato: " + suspect.name());
    }

    private void refreshSelectedEvidencePresentation() {
        EvidenceItem evidence = evidenceList.getSelectionModel().getSelectedItem();
        if (evidence == null) {
            evidenceDetailsArea.setText("Seleziona una prova per vedere descrizione, stato, collegamenti e rilevanza.");
            selectedEvidenceLabel.setText("Prova selezionata: nessuna");
            return;
        }

        evidenceDetailsArea.setText(evidence.detailText());
        selectedEvidenceLabel.setText("Prova selezionata: " + evidence.title());
    }

    private void restoreSuspectSelection(String suspectId) {
        selectById(suspectList, suspectId);
    }

    private void restoreEvidenceSelection(String evidenceId) {
        selectById(evidenceList, evidenceId);
    }

    private void restoreAccusedSuspectSelection(String suspectId) {
        accusedSuspectComboBox.setValue(null);
        if (suspectId != null) {
            accusedSuspectComboBox.getItems().stream()
                    .filter(item -> item.id().equals(suspectId))
                    .findFirst()
                    .ifPresent(accusedSuspectComboBox::setValue);
        }
    }

    private void restorePrimaryEvidenceSelection(String evidenceId) {
        selectComboItemById(primaryEvidenceComboBox, evidenceId);
    }

    private void restoreRelevantTimelineSelection(String timelineEventId) {
        selectComboItemById(relevantTimelineComboBox, timelineEventId);
    }

    private void refreshPrimaryContradictionOptions(String preferredContradictionId) {
        primaryContradictionComboBox.setValue(null);
        if (currentCaseFile == null || accusedSuspectComboBox.getValue() == null) {
            primaryContradictionComboBox.getItems().clear();
            return;
        }

        String accusedSuspectId = accusedSuspectComboBox.getValue().id();
        List<ContradictionItem> contradictionItems = currentCaseFile.findSuspectById(accusedSuspectId)
                .map(suspect -> suspect.getInterrogations().stream()
                        .flatMap(interrogation -> interrogation.getContradictions().stream())
                        .map(this::toContradictionItem)
                        .toList())
                .orElseGet(List::of);

        primaryContradictionComboBox.getItems().setAll(contradictionItems);
        selectComboItemById(primaryContradictionComboBox, preferredContradictionId);
    }

    private ContradictionItem toContradictionItem(Contradiction contradiction) {
        String label = contradiction.getEvidence().getTitle()
                + " smentisce: "
                + contradiction.getAnswer().getText();
        return new ContradictionItem(contradiction.getId(), contradiction.getSuspectId(), label);
    }

    private <T extends IdentifiedItem> void selectById(ListView<T> listView, String id) {
        if (id == null) {
            return;
        }
        listView.getItems().stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .ifPresent(item -> listView.getSelectionModel().select(item));
    }

    private <T extends IdentifiedItem> void selectComboItemById(ComboBox<T> comboBox, String id) {
        comboBox.setValue(null);
        if (id == null) {
            return;
        }
        comboBox.getItems().stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .ifPresent(comboBox::setValue);
    }

    private String selectedSuspectId() {
        return Optional.ofNullable(suspectList.getSelectionModel().getSelectedItem())
                .map(SuspectItem::id)
                .orElse(null);
    }

    private String selectedEvidenceId() {
        return Optional.ofNullable(evidenceList.getSelectionModel().getSelectedItem())
                .map(EvidenceItem::id)
                .orElse(null);
    }

    private String selectedAccusedSuspectId() {
        return Optional.ofNullable(accusedSuspectComboBox.getValue())
                .map(SuspectItem::id)
                .orElse(null);
    }

    private String selectedPrimaryEvidenceId() {
        return Optional.ofNullable(primaryEvidenceComboBox.getValue())
                .map(EvidenceAccusationItem::id)
                .orElse(null);
    }

    private String selectedPrimaryContradictionId() {
        return Optional.ofNullable(primaryContradictionComboBox.getValue())
                .map(ContradictionItem::id)
                .orElse(null);
    }

    private String selectedRelevantTimelineEventId() {
        return Optional.ofNullable(relevantTimelineComboBox.getValue())
                .map(TimelineEventItem::id)
                .orElse(null);
    }

    private String joinedOrFallback(List<String> values, String fallback) {
        return values.isEmpty() ? fallback : String.join(", ", values);
    }

    private String reliabilityCategory(int reliabilityScore) {
        if (reliabilityScore >= 80) {
            return "ALTA";
        }
        if (reliabilityScore >= 60) {
            return "MEDIA";
        }
        if (reliabilityScore >= 40) {
            return "BASSA";
        }
        return "COMPROMESSA";
    }

    private Label sectionHeading(String text) {
        Label heading = new Label(text);
        heading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        return heading;
    }

    private VBox accusationField(String label, ComboBox<?> comboBox) {
        Label fieldLabel = new Label(label);
        fieldLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        return new VBox(6, fieldLabel, comboBox);
    }

    private static TextArea readOnlyArea() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        return area;
    }

    private void appendLog(String message) {
        logArea.appendText(message + System.lineSeparator());
    }

    private void withController(Runnable action) {
        if (controller == null) {
            appendLog("Controller non disponibile.");
            return;
        }

        try {
            action.run();
        } catch (RuntimeException exception) {
            String message = "Operazione non completata: " + exception.getMessage();
            statusLabel.setText(message);
            appendLog(message);
        }
    }

    private interface IdentifiedItem {
        String id();
    }

    private record SuspectItem(
            String id,
            String name,
            String label,
            String detailText,
            List<String> interrogationLines,
            List<AnswerLinkItem> answerLinkItems
    ) implements IdentifiedItem {
        @Override
        public String toString() {
            return label;
        }
    }

    private record EvidenceItem(
            String id,
            String title,
            String label,
            String detailText
    ) implements IdentifiedItem {
        @Override
        public String toString() {
            return label;
        }
    }

    private record EvidenceAccusationItem(
            String id,
            String title,
            String label
    ) implements IdentifiedItem {
        @Override
        public String toString() {
            return label;
        }
    }

    private record ContradictionItem(
            String id,
            String suspectId,
            String label
    ) implements IdentifiedItem {
        @Override
        public String toString() {
            return label;
        }
    }

    private record TimelineEventItem(
            String id,
            String label
    ) implements IdentifiedItem {
        @Override
        public String toString() {
            return label;
        }
    }

    private record AnswerLinkItem(
            String suspectId,
            String interrogationId,
            String questionId,
            String questionText,
            String answerPreview
    ) {
        @Override
        public String toString() {
            return questionText + " -> " + answerPreview;
        }
    }
}
