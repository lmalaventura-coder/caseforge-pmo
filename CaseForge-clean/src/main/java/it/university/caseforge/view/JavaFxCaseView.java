package it.university.caseforge.view;

import it.university.caseforge.controller.CaseController;
import it.university.caseforge.model.Answer;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
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
    private final ListView<QuestionPromptItem> questionList = new ListView<>();
    private final ListView<String> interrogationList = new ListView<>();
    private final ComboBox<AnswerLinkItem> answerComboBox = new ComboBox<>();
    private final ListView<String> timelineList = new ListView<>();
    private final ComboBox<SuspectItem> accusedSuspectComboBox = new ComboBox<>();
    private final ComboBox<EvidenceAccusationItem> primaryEvidenceComboBox = new ComboBox<>();
    private final ComboBox<ContradictionItem> primaryContradictionComboBox = new ComboBox<>();
    private final ComboBox<TimelineEventItem> relevantTimelineComboBox = new ComboBox<>();

    private final TextArea suspectDetailsArea = readOnlyArea();
    private final TextArea evidenceDetailsArea = readOnlyArea();
    private final TextArea dossierArea = readOnlyArea();
    private final TextArea logArea = readOnlyArea();

    private final Button discoverEvidenceButton = new Button("Scopri prova selezionata");
    private final Button askQuestionButton = new Button("Fai domanda selezionata");
    private final Button linkEvidenceButton = new Button("Collega prova selezionata alla risposta selezionata");
    private final Button formulateAccusationButton = new Button("Formula accusa");
    private final Button resetInvestigationButton = new Button("Nuova indagine");

    private CaseController controller;
    private CaseFile currentCaseFile;
    private boolean caseLoaded;
    private boolean refreshingView;

    public JavaFxCaseView() {
        configureVisualStyles();
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
            dossierArea.setText(buildInvestigationDossier(caseFile));

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
            appendLog("Caso demo caricato. Inizia selezionando un sospetto, poi esamina prove e cronologia.");
            caseLoaded = true;
        }
    }

    @Override
    public void resetInvestigation(CaseFile caseFile) {
        refreshingView = true;
        try {
            clearInvestigationSelections();
            logArea.clear();
            selectedSuspectLabel.setText("Sospetto selezionato: nessuno");
            selectedEvidenceLabel.setText("Prova selezionata: nessuna");
        } finally {
            refreshingView = false;
        }

        showCase(caseFile);
        statusLabel.setText("Nuova indagine avviata.");
        appendLog("Nuova indagine avviata. Il caso demo e stato ripristinato.");
        appendLog("Seleziona un sospetto, poi esamina prove e cronologia.");
    }

    @Override
    public void showInvestigationEvent(InvestigationEvent event) {
        String message = formatInvestigationEvent(event);
        statusLabel.setText(message);
        appendLog(message);
    }

    @Override
    public void showEvaluationResult(EvaluationResult result) {
        String message = result.getMessage() + " Punteggio: " + result.getScore();
        if (!result.getMissingEvidenceIds().isEmpty()) {
            message += " | Prove mancanti: " + String.join(", ", result.getMissingEvidenceIds());
        }

        statusLabel.setText(message);
        appendLog(message);
    }

    private void configureRoot() {
        root.setPadding(new Insets(18));
        root.setTop(buildHeader());
        root.setCenter(buildScrollableWorkspace());
        root.setBottom(buildActionArea());
    }

    private VBox buildHeader() {
        descriptionLabel.setWrapText(true);

        VBox header = new VBox(10,
                titleLabel,
                descriptionLabel,
                statusLabel,
                buildProcedurePanel()
        );
        header.getStyleClass().add("header-panel");
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private VBox buildProcedurePanel() {
        Label heading = sectionHeading("Procedura investigativa");
        HBox steps = new HBox(10,
                procedureStep("1. Seleziona un sospetto"),
                procedureStep("2. Esamina prove e cronologia"),
                procedureStep("3. Interroga il sospetto"),
                procedureStep("4. Collega prove alle risposte"),
                procedureStep("5. Formula accusa")
        );
        steps.setAlignment(Pos.CENTER_LEFT);

        VBox procedure = new VBox(8, heading, steps);
        procedure.getStyleClass().add("procedure-panel");
        return procedure;
    }

    private Label procedureStep(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(150);
        label.getStyleClass().add("procedure-step");
        return label;
    }

    private ScrollPane buildScrollableWorkspace() {
        ScrollPane scrollPane = new ScrollPane(buildInvestigationWorkspace());
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add("workspace-scroll");
        return scrollPane;
    }

    private HBox buildInvestigationWorkspace() {
        VBox leftColumn = buildSuspectColumn();
        VBox centerColumn = buildCenterColumn();
        VBox rightColumn = buildRightColumn();

        leftColumn.setPrefWidth(300);
        rightColumn.setPrefWidth(410);
        rightColumn.setMinWidth(360);
        HBox.setHgrow(centerColumn, Priority.ALWAYS);

        HBox workspace = new HBox(16, leftColumn, centerColumn, rightColumn);
        workspace.getStyleClass().add("workspace");
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
        column.getStyleClass().addAll("workspace-panel", "suspect-panel");
        VBox.setVgrow(suspectDetailsArea, Priority.ALWAYS);
        return column;
    }

    private VBox buildCenterColumn() {
        evidenceList.setPrefWidth(320);
        evidenceDetailsArea.setPrefRowCount(12);
        questionList.setPrefHeight(150);
        questionList.setPlaceholder(new Label("Nessuna domanda disponibile"));
        interrogationList.setPrefHeight(170);
        interrogationList.setPlaceholder(new Label("Nessuna domanda posta"));
        answerComboBox.setPromptText("Risposta ottenuta da collegare");
        answerComboBox.setPrefWidth(460);
        askQuestionButton.setPrefWidth(210);

        VBox evidenceListPanel = new VBox(8,
                sectionHeading("Prove disponibili e scoperte"),
                evidenceList
        );
        evidenceListPanel.getStyleClass().addAll("workspace-panel", "evidence-list-panel");
        VBox.setVgrow(evidenceList, Priority.ALWAYS);

        VBox evidenceDetailsPanel = new VBox(8,
                sectionHeading("Dettagli della prova selezionata"),
                evidenceDetailsArea
        );
        evidenceDetailsPanel.getStyleClass().addAll("workspace-panel", "evidence-detail-panel");
        HBox.setHgrow(evidenceDetailsPanel, Priority.ALWAYS);

        HBox evidenceArea = new HBox(12, evidenceListPanel, evidenceDetailsPanel);
        evidenceArea.getStyleClass().add("split-evidence-area");
        VBox.setVgrow(evidenceArea, Priority.ALWAYS);

        HBox questionActions = new HBox(10, askQuestionButton);
        questionActions.setAlignment(Pos.CENTER_LEFT);

        VBox interrogationPanel = new VBox(8,
                sectionHeading("Domande disponibili"),
                questionList,
                questionActions,
                sectionHeading("Dossier interrogatorio"),
                interrogationList,
                sectionHeading("Risposta da collegare"),
                answerComboBox
        );
        interrogationPanel.getStyleClass().addAll("workspace-panel", "interrogation-panel");
        VBox.setVgrow(interrogationList, Priority.ALWAYS);

        VBox center = new VBox(14, evidenceArea, interrogationPanel);
        center.getStyleClass().add("center-column");
        VBox.setVgrow(evidenceArea, Priority.ALWAYS);
        return center;
    }

    private VBox buildRightColumn() {
        timelineList.setPrefHeight(320);
        timelineList.setMinHeight(220);
        dossierArea.setPrefRowCount(15);
        dossierArea.setMinHeight(210);
        logArea.setPrefRowCount(15);
        logArea.setMinHeight(230);

        VBox timelinePanel = new VBox(8,
                sectionHeading("Cronologia investigativa"),
                timelineList
        );
        timelinePanel.getStyleClass().addAll("workspace-panel", "timeline-panel");
        timelinePanel.setMinHeight(250);
        VBox.setVgrow(timelineList, Priority.ALWAYS);

        VBox logPanel = new VBox(8,
                sectionHeading("Log eventi investigativi"),
                logArea
        );
        logPanel.getStyleClass().addAll("workspace-panel", "log-panel");
        logPanel.setMinHeight(260);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        VBox dossierPanel = new VBox(8,
                sectionHeading("Dossier investigativo"),
                dossierArea
        );
        dossierPanel.getStyleClass().addAll("workspace-panel", "investigation-dossier-panel");
        dossierPanel.setMinHeight(240);
        VBox.setVgrow(dossierArea, Priority.ALWAYS);

        VBox right = new VBox(14, timelinePanel, dossierPanel, logPanel);
        right.getStyleClass().add("right-column");
        VBox.setVgrow(timelinePanel, Priority.ALWAYS);
        VBox.setVgrow(dossierPanel, Priority.ALWAYS);
        VBox.setVgrow(logPanel, Priority.ALWAYS);
        return right;
    }

    private VBox buildActionArea() {
        selectedSuspectLabel.setWrapText(true);
        selectedEvidenceLabel.setWrapText(true);

        HBox selectionSummary = new HBox(18, selectedSuspectLabel, selectedEvidenceLabel);
        selectionSummary.setAlignment(Pos.CENTER_LEFT);
        selectionSummary.getStyleClass().add("selection-summary");

        accusedSuspectComboBox.setPromptText("Sospetto accusato");
        accusedSuspectComboBox.setPrefWidth(155);
        primaryEvidenceComboBox.setPromptText("Prova principale");
        primaryEvidenceComboBox.setPrefWidth(180);
        primaryContradictionComboBox.setPromptText("Contraddizione principale");
        primaryContradictionComboBox.setPrefWidth(240);
        relevantTimelineComboBox.setPromptText("Evento della cronologia rilevante");
        relevantTimelineComboBox.setPrefWidth(200);
        discoverEvidenceButton.setPrefWidth(190);
        linkEvidenceButton.setPrefWidth(310);
        linkEvidenceButton.setWrapText(true);
        formulateAccusationButton.setPrefWidth(150);
        resetInvestigationButton.setPrefWidth(160);

        HBox investigativeActions = new HBox(12,
                discoverEvidenceButton,
                linkEvidenceButton,
                formulateAccusationButton,
                resetInvestigationButton
        );
        investigativeActions.setAlignment(Pos.CENTER_LEFT);
        investigativeActions.getStyleClass().add("action-strip");

        HBox titleRow = new HBox(12,
                sectionHeading("Azioni investigative"),
                selectionSummary
        );
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(selectionSummary, Priority.ALWAYS);

        HBox accusationFields = new HBox(10,
                accusationHeading(),
                accusationField("Sospetto", accusedSuspectComboBox),
                accusationField("Prova principale", primaryEvidenceComboBox),
                accusationField("Contraddizione", primaryContradictionComboBox),
                accusationField("Evento cronologia", relevantTimelineComboBox)
        );
        accusationFields.setAlignment(Pos.CENTER_LEFT);
        accusationFields.getStyleClass().addAll("accusation-fields", "accusation-block");

        VBox actionArea = new VBox(8,
                titleRow,
                investigativeActions,
                accusationFields
        );
        actionArea.getStyleClass().add("action-panel");
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
                            + ". Ora seleziona prova, contraddizione ed evento della cronologia.");
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

        askQuestionButton.setOnAction(event -> withController(() -> {
            QuestionPromptItem question = questionList.getSelectionModel().getSelectedItem();
            if (question == null) {
                appendLog("Seleziona una domanda dall'elenco prima di premere Fai domanda selezionata.");
                return;
            }

            appendLog("Domanda selezionata posta: " + question.questionText() + ".");
            controller.askQuestion(
                    question.suspectId(),
                    question.interrogationId(),
                    question.questionId()
            );
        }));

        linkEvidenceButton.setOnAction(event -> withController(() -> {
            SuspectItem suspect = suspectList.getSelectionModel().getSelectedItem();
            EvidenceItem evidence = evidenceList.getSelectionModel().getSelectedItem();
            AnswerLinkItem answer = answerComboBox.getValue();

            if (suspect == null && evidence == null && answer == null) {
                appendLog("Per collegare una prova a una risposta, seleziona sospetto, prova e risposta ottenuta.");
                return;
            }
            if (suspect == null) {
                appendLog("Per collegare una prova, seleziona prima il sospetto interrogato.");
                return;
            }
            if (evidence == null) {
                appendLog("Per collegare una risposta, seleziona anche la prova da analizzare.");
                return;
            }
            if (answer == null) {
                appendLog("Per collegare la prova, scegli una risposta gia ottenuta del sospetto selezionato.");
                return;
            }
            if (!answer.suspectId().equals(suspect.id())) {
                appendLog("La risposta selezionata non appartiene al sospetto corrente. Cambia sospetto o risposta.");
                return;
            }

            appendLog("Collegamento richiesto: prova '" + evidence.title()
                    + "' alla risposta '" + answer.answerPreview()
                    + "' di " + suspect.name() + ".");
            controller.linkEvidenceToAnswer(
                    evidence.id(),
                    suspect.id(),
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
                appendLog("Per formulare l'accusa, seleziona un evento della cronologia rilevante.");
                return;
            }

            appendLog("Accusa strutturata formulata contro " + accusedSuspect.name()
                    + " con prova, contraddizione ed evento della cronologia selezionati.");
            controller.submitAccusation(
                    accusedSuspect.id(),
                    primaryEvidence.id(),
                    primaryContradiction.id(),
                    relevantTimelineEvent.id(),
                    "Accusa formulata dalla GUI con dossier strutturato."
            );
        }));

        resetInvestigationButton.setOnAction(event -> withController(() -> controller.resetDemoInvestigation()));
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
        List<QuestionPromptItem> questionItems = buildQuestionPromptItems(suspect);
        List<String> interrogationLines = buildInterrogationLines(caseFile, suspect);
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

        String reliabilityLabel = reliabilityCategory(suspect.getReliabilityScore());
        String label = suspect.getName()
                + " | affidabilita "
                + reliabilityLabel;
        return new SuspectItem(
                suspect.getId(),
                suspect.getName(),
                label,
                reliabilityLabel,
                detailText,
                questionItems,
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

        boolean contradictionEvidence = caseFile.getInterrogations().stream()
                .flatMap(interrogation -> interrogation.getContradictions().stream())
                .anyMatch(contradiction -> contradiction.getEvidence().getId().equals(evidence.getId()));

        return new EvidenceItem(
                evidence.getId(),
                evidence.getTitle(),
                evidence.getType().toString(),
                evidence.isDiscovered(),
                contradictionEvidence,
                label,
                detailText
        );
    }

    private EvidenceAccusationItem toEvidenceAccusationItem(Evidence evidence) {
        String label = evidence.getTitle() + " | " + evidence.getType();
        return new EvidenceAccusationItem(evidence.getId(), evidence.getTitle(), label);
    }

    private TimelineEventItem toTimelineEventItem(TimelineEvent event) {
        return new TimelineEventItem(event.getId(), formatTimelineEvent(event));
    }

    private List<String> buildInterrogationLines(CaseFile caseFile, Suspect suspect) {
        if (suspect.getInterrogations().isEmpty()) {
            return List.of("Nessun interrogatorio disponibile per il sospetto selezionato.");
        }

        List<String> lines = new ArrayList<>();
        boolean anyAnswerCollected = false;
        for (Interrogation interrogation : suspect.getInterrogations()) {
            lines.add("Interrogatorio " + interrogation.getId()
                    + " | " + TIMELINE_FORMATTER.format(interrogation.getStartedAt()));
            for (Question question : interrogation.getQuestions()) {
                if (question.isAnswerObtained() && question.getAnswer() != null) {
                    anyAnswerCollected = true;
                    Answer answer = question.getAnswer();
                    lines.add("Domanda [" + question.getCategory() + "]: " + question.getText());
                    lines.add("Risposta [" + answer.getReliabilityLevel() + "]: " + answer.getText());
                    lines.add("Prova collegata: "
                            + joinedOrFallback(resolveLinkedEvidenceTitles(caseFile, answer), "nessuna"));
                    lines.add("Contraddizione: "
                            + contradictionFor(interrogation, question)
                                    .map(this::formatContradictionLine)
                                    .orElse("nessuna rilevata"));
                }
            }
        }
        return anyAnswerCollected ? lines : List.of("Nessuna domanda posta.");
    }

    private List<QuestionPromptItem> buildQuestionPromptItems(Suspect suspect) {
        List<QuestionPromptItem> items = new ArrayList<>();
        for (Interrogation interrogation : suspect.getInterrogations()) {
            for (Question question : interrogation.getQuestions()) {
                if (question.isAnswerObtained()) {
                    continue;
                }
                items.add(new QuestionPromptItem(
                        suspect.getId(),
                        interrogation.getId(),
                        question.getId(),
                        question.getCategory().toString(),
                        question.getText(),
                        question.isAnswerObtained()
                ));
            }
        }
        return items;
    }

    private List<AnswerLinkItem> buildAnswerLinkItems(Suspect suspect) {
        List<AnswerLinkItem> items = new ArrayList<>();
        for (Interrogation interrogation : suspect.getInterrogations()) {
            for (Question question : interrogation.getQuestions()) {
                if (question.isAnswerObtained() && question.getAnswer() != null) {
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
                            + formatContradictionLine(contradiction)
                            + ". "
                            + contradiction.getExplanation())
                    .orElse(event.getMessage());
        }

        if (event.getType() == InvestigationEventType.ANSWER_OBTAINED) {
            return event.getMessage();
        }

        if (event.getType() == InvestigationEventType.EVIDENCE_LINKED_TO_ANSWER) {
            return event.getMessage();
        }

        if (event.getType() == InvestigationEventType.NO_CONTRADICTION_DETECTED) {
            return event.getMessage();
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

    private List<String> resolveLinkedEvidenceTitles(CaseFile caseFile, Answer answer) {
        return answer.getLinkedEvidenceIds().stream()
                .map(evidenceId -> caseFile.findEvidenceById(evidenceId)
                        .map(Evidence::getTitle)
                        .orElse(evidenceId))
                .toList();
    }

    private Optional<Contradiction> contradictionFor(Interrogation interrogation, Question question) {
        return interrogation.getContradictions().stream()
                .filter(contradiction -> contradiction.getQuestion() == question)
                .findFirst();
    }

    private String evidenceRelevance(List<String> linkedSuspects, boolean discovered) {
        if (!linkedSuspects.isEmpty()) {
            return "aiuta a confrontare " + String.join(", ", linkedSuspects)
                    + " con cronologia, accessi e dichiarazioni.";
        }
        if (discovered) {
            return "e una prova gia emersa da valutare insieme alla cronologia e agli interrogatori.";
        }
        return "scoprirla puo chiarire accessi, movimenti o alibi ancora aperti.";
    }

    private void refreshSelectedSuspectPresentation() {
        SuspectItem suspect = suspectList.getSelectionModel().getSelectedItem();
        if (suspect == null) {
            suspectDetailsArea.setText("Seleziona un sospetto per visualizzare movente, alibi, affidabilita e prove collegate.");
            questionList.getItems().clear();
            questionList.getSelectionModel().clearSelection();
            interrogationList.getItems().setAll("Gli interrogatori compariranno dopo la selezione di un sospetto.");
            answerComboBox.getItems().clear();
            answerComboBox.setValue(null);
            selectedSuspectLabel.setText("Sospetto selezionato: nessuno");
            return;
        }

        suspectDetailsArea.setText(suspect.detailText());
        questionList.getItems().setAll(suspect.questionItems());
        questionList.getSelectionModel().clearSelection();
        interrogationList.getItems().setAll(suspect.interrogationLines());
        answerComboBox.getItems().setAll(suspect.answerLinkItems());
        answerComboBox.setValue(null);
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

    private void clearInvestigationSelections() {
        suspectList.getSelectionModel().clearSelection();
        evidenceList.getSelectionModel().clearSelection();
        questionList.getSelectionModel().clearSelection();
        accusedSuspectComboBox.setValue(null);
        primaryEvidenceComboBox.setValue(null);
        primaryContradictionComboBox.setValue(null);
        relevantTimelineComboBox.setValue(null);
        answerComboBox.setValue(null);
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

    private String buildInvestigationDossier(CaseFile caseFile) {
        List<String> lines = new ArrayList<>();
        lines.add("Prove scoperte");
        List<String> discoveredEvidence = caseFile.getEvidences().stream()
                .filter(Evidence::isDiscovered)
                .map(evidence -> "- " + evidence.getTitle() + " [" + evidence.getType() + "]")
                .toList();
        lines.addAll(discoveredEvidence.isEmpty() ? List.of("- Nessuna prova scoperta.") : discoveredEvidence);

        lines.add("");
        lines.add("Contraddizioni trovate");
        List<String> contradictions = caseFile.getInterrogations().stream()
                .flatMap(interrogation -> interrogation.getContradictions().stream())
                .map(contradiction -> "- " + formatContradictionLine(contradiction))
                .toList();
        lines.addAll(contradictions.isEmpty() ? List.of("- Nessuna contraddizione confermata.") : contradictions);

        lines.add("");
        lines.add("Risposte ottenute");
        List<String> obtainedAnswers = caseFile.getSuspects().stream()
                .flatMap(suspect -> suspect.getInterrogations().stream()
                        .flatMap(interrogation -> interrogation.getQuestions().stream()
                                .filter(Question::isAnswerObtained)
                                .filter(question -> question.getAnswer() != null)
                                .map(question -> "- " + suspect.getName()
                                        + ": " + question.getText()
                                        + " -> " + question.getAnswer().getText())))
                .toList();
        lines.addAll(obtainedAnswers.isEmpty() ? List.of("- Nessuna risposta raccolta.") : obtainedAnswers);

        lines.add("");
        lines.add("Eventi della cronologia importanti");
        List<String> timelineEvents = caseFile.getTimeline().getEvents().stream()
                .map(event -> "- " + TIMELINE_FORMATTER.format(event.getOccurredAt())
                        + " | " + event.getTitle())
                .toList();
        lines.addAll(timelineEvents.isEmpty() ? List.of("- Nessun evento disponibile.") : timelineEvents);

        return String.join(System.lineSeparator(), lines);
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
        heading.getStyleClass().add("section-title");
        return heading;
    }

    private Label accusationHeading() {
        Label heading = sectionHeading("Accusa strutturata");
        heading.getStyleClass().add("accusation-heading");
        return heading;
    }

    private VBox accusationField(String label, ComboBox<?> comboBox) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("field-label");
        VBox field = new VBox(6, fieldLabel, comboBox);
        field.getStyleClass().add("accusation-field");
        return field;
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

    private void configureVisualStyles() {
        root.getStyleClass().add("caseforge-root");
        titleLabel.getStyleClass().add("case-title");
        descriptionLabel.getStyleClass().add("case-description");
        statusLabel.getStyleClass().add("status-banner");
        selectedSuspectLabel.getStyleClass().add("selection-chip");
        selectedEvidenceLabel.getStyleClass().add("selection-chip");

        suspectList.getStyleClass().addAll("dossier-list", "suspect-list");
        evidenceList.getStyleClass().addAll("dossier-list", "evidence-list");
        questionList.getStyleClass().addAll("dossier-list", "question-list");
        interrogationList.getStyleClass().addAll("dossier-list", "interrogation-list");
        timelineList.getStyleClass().addAll("dossier-list", "timeline-list");

        suspectDetailsArea.getStyleClass().addAll("detail-area", "suspect-detail-area");
        evidenceDetailsArea.getStyleClass().addAll("detail-area", "evidence-detail-area");
        dossierArea.getStyleClass().addAll("detail-area", "dossier-area");
        logArea.getStyleClass().addAll("detail-area", "log-area");

        answerComboBox.getStyleClass().add("dossier-combo");
        accusedSuspectComboBox.getStyleClass().add("dossier-combo");
        primaryEvidenceComboBox.getStyleClass().add("dossier-combo");
        primaryContradictionComboBox.getStyleClass().add("dossier-combo");
        relevantTimelineComboBox.getStyleClass().add("dossier-combo");

        discoverEvidenceButton.getStyleClass().addAll("case-button", "button-muted");
        askQuestionButton.getStyleClass().addAll("case-button", "button-accent");
        linkEvidenceButton.getStyleClass().addAll("case-button", "button-accent");
        formulateAccusationButton.getStyleClass().addAll("case-button", "button-primary");
        resetInvestigationButton.getStyleClass().addAll("case-button", "button-secondary");

        configureSuspectCells();
        configureEvidenceCells();
        configureQuestionCells();
    }

    private void configureSuspectCells() {
        suspectList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SuspectItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("suspect-cell");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label nameLabel = new Label(item.name());
                nameLabel.getStyleClass().add("list-primary-text");

                Label badge = new Label(item.reliabilityLabel());
                badge.getStyleClass().addAll(
                        "badge",
                        "reliability-badge",
                        reliabilityStyleClass(item.reliabilityLabel())
                );

                HBox row = new HBox(10, nameLabel, badge);
                row.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                setText(null);
                setGraphic(row);
                getStyleClass().add("suspect-cell");
            }
        });
    }

    private void configureEvidenceCells() {
        evidenceList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(EvidenceItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("evidence-cell", "evidence-discovered", "evidence-undiscovered");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label title = new Label(item.title());
                title.getStyleClass().add("list-primary-text");

                Label type = new Label(item.typeLabel());
                type.getStyleClass().add("list-secondary-text");

                VBox copy = new VBox(3, title, type);
                HBox.setHgrow(copy, Priority.ALWAYS);

                String statusLabel = item.contradictionEvidence()
                        ? "CONTRADDIZIONE"
                        : item.discovered() ? "SCOPERTA" : "DA SCOPRIRE";
                Label status = new Label(statusLabel);
                status.getStyleClass().addAll(
                        "badge",
                        item.contradictionEvidence()
                                ? "evidence-status-contradiction"
                                : item.discovered() ? "evidence-status-discovered" : "evidence-status-hidden"
                );

                HBox row = new HBox(10, copy, status);
                row.setAlignment(Pos.CENTER_LEFT);

                setText(null);
                setGraphic(row);
                getStyleClass().addAll(
                        "evidence-cell",
                        item.contradictionEvidence()
                                ? "evidence-contradiction"
                                : item.discovered() ? "evidence-discovered" : "evidence-undiscovered"
                );
            }
        });
    }

    private void configureQuestionCells() {
        questionList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(QuestionPromptItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("question-cell", "question-answered", "question-pending");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label question = new Label(item.questionText());
                question.getStyleClass().add("list-primary-text");
                question.setWrapText(true);

                Label category = new Label(item.categoryLabel());
                category.getStyleClass().add("list-secondary-text");

                VBox copy = new VBox(3, question, category);
                HBox.setHgrow(copy, Priority.ALWAYS);

                Label state = new Label(item.answerObtained() ? "RISPOSTA OTTENUTA" : "DA PORRE");
                state.getStyleClass().addAll(
                        "badge",
                        item.answerObtained() ? "question-status-answered" : "question-status-pending"
                );

                HBox row = new HBox(10, copy, state);
                row.setAlignment(Pos.CENTER_LEFT);

                setText(null);
                setGraphic(row);
                getStyleClass().addAll(
                        "question-cell",
                        item.answerObtained() ? "question-answered" : "question-pending"
                );
            }
        });
    }

    private String reliabilityStyleClass(String reliabilityLabel) {
        return switch (reliabilityLabel) {
            case "ALTA" -> "reliability-high";
            case "MEDIA" -> "reliability-medium";
            case "BASSA" -> "reliability-low";
            default -> "reliability-critical";
        };
    }

    private interface IdentifiedItem {
        String id();
    }

    private record SuspectItem(
            String id,
            String name,
            String label,
            String reliabilityLabel,
            String detailText,
            List<QuestionPromptItem> questionItems,
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
            String typeLabel,
            boolean discovered,
            boolean contradictionEvidence,
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

    private record QuestionPromptItem(
            String suspectId,
            String interrogationId,
            String questionId,
            String categoryLabel,
            String questionText,
            boolean answerObtained
    ) {
        @Override
        public String toString() {
            return questionText;
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
