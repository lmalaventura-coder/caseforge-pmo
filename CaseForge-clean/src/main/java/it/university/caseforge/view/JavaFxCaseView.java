package it.university.caseforge.view;

import it.university.caseforge.controller.CaseController;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.model.Evidence;
import it.university.caseforge.model.Suspect;
import it.university.caseforge.model.TimelineEvent;
import it.university.caseforge.observer.InvestigationEvent;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaFxCaseView implements CaseView {

    private static final DateTimeFormatter TIMELINE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BorderPane root = new BorderPane();
    private final Label titleLabel = new Label("CaseForge");
    private final Label descriptionLabel = new Label();
    private final Label statusLabel = new Label("In attesa del caso demo.");

    private final ListView<SelectionItem> suspectList = new ListView<>();
    private final ListView<SelectionItem> evidenceList = new ListView<>();
    private final ListView<String> timelineList = new ListView<>();
    private final ComboBox<SelectionItem> accusedSuspectComboBox = new ComboBox<>();
    private final TextArea logArea = new TextArea();

    private final Button discoverEvidenceButton = new Button("Scopri prova");
    private final Button linkEvidenceButton = new Button("Collega prova a sospetto");
    private final Button formulateAccusationButton = new Button("Formula accusa");

    private CaseController controller;
    private boolean caseLoaded;

    public JavaFxCaseView() {
        configureRoot();
        configureActions();
        appendLog("Interfaccia pronta.");
    }

    public void bindController(CaseController controller) {
        this.controller = Objects.requireNonNull(controller);
    }

    public Parent getRoot() {
        return root;
    }

    @Override
    public void showCase(CaseFile caseFile) {
        String selectedSuspectId = selectedId(suspectList.getSelectionModel().getSelectedItem());
        String selectedEvidenceId = selectedId(evidenceList.getSelectionModel().getSelectedItem());
        String selectedAccusedSuspectId = selectedId(accusedSuspectComboBox.getValue());

        titleLabel.setText(caseFile.getTitle());
        descriptionLabel.setText(caseFile.getDescription());

        suspectList.getItems().setAll(caseFile.getSuspects().stream()
                .map(this::toSuspectItem)
                .toList());
        accusedSuspectComboBox.getItems().setAll(suspectList.getItems());

        evidenceList.getItems().setAll(caseFile.getEvidences().stream()
                .map(this::toEvidenceItem)
                .toList());
        timelineList.getItems().setAll(caseFile.getTimeline().getEvents().stream()
                .map(this::formatTimelineEvent)
                .toList());

        restoreSelection(suspectList, selectedSuspectId);
        restoreSelection(evidenceList, selectedEvidenceId);
        restoreSelection(accusedSuspectComboBox, selectedAccusedSuspectId);

        if (!caseLoaded) {
            statusLabel.setText("Caso demo caricato.");
            appendLog("Caso demo caricato.");
            caseLoaded = true;
        }
    }

    @Override
    public void showInvestigationEvent(InvestigationEvent event) {
        String detail = event.getEvidence()
                .map(evidence -> " " + evidence.getTitle())
                .orElse("");
        String message = event.getMessage() + detail;
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
        root.setCenter(buildWorkspace());
        root.setBottom(buildLogPanel());
    }

    private VBox buildHeader() {
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 13px;");
        statusLabel.setStyle("-fx-font-weight: bold;");

        VBox header = new VBox(8, titleLabel, descriptionLabel, statusLabel);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private VBox buildWorkspace() {
        HBox columns = new HBox(14,
                buildListPanel("Sospetti", suspectList),
                buildListPanel("Prove", evidenceList),
                buildListPanel("Timeline", timelineList)
        );

        HBox.setHgrow(columns.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(columns.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(columns.getChildren().get(2), Priority.ALWAYS);

        HBox actionBar = new HBox(12,
                discoverEvidenceButton,
                linkEvidenceButton,
                accusedSuspectComboBox,
                formulateAccusationButton
        );
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(14, 0, 0, 0));
        accusedSuspectComboBox.setPromptText("Sospetto accusato");
        accusedSuspectComboBox.setPrefWidth(260);

        VBox workspace = new VBox(0, columns, actionBar);
        VBox.setVgrow(columns, Priority.ALWAYS);
        return workspace;
    }

    private VBox buildListPanel(String title, ListView<?> listView) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        listView.setPrefHeight(360);

        VBox panel = new VBox(8, heading, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        panel.setMinWidth(260);
        return panel;
    }

    private VBox buildLogPanel() {
        Label logLabel = new Label("Log eventi");
        logLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(8);

        VBox logPanel = new VBox(8, logLabel, logArea);
        logPanel.setPadding(new Insets(18, 0, 0, 0));
        return logPanel;
    }

    private void configureActions() {
        discoverEvidenceButton.setOnAction(event -> withController(() -> {
            SelectionItem evidence = evidenceList.getSelectionModel().getSelectedItem();
            if (evidence == null) {
                appendLog("Seleziona una prova da scoprire.");
                return;
            }
            controller.discoverEvidence(evidence.id());
        }));

        linkEvidenceButton.setOnAction(event -> withController(() -> {
            SelectionItem evidence = evidenceList.getSelectionModel().getSelectedItem();
            SelectionItem suspect = suspectList.getSelectionModel().getSelectedItem();
            if (evidence == null || suspect == null) {
                appendLog("Seleziona una prova e un sospetto da collegare.");
                return;
            }
            controller.linkEvidenceToSuspect(evidence.id(), suspect.id());
        }));

        formulateAccusationButton.setOnAction(event -> withController(() -> {
            SelectionItem accusedSuspect = accusedSuspectComboBox.getValue();
            if (accusedSuspect == null) {
                appendLog("Seleziona il sospetto accusato.");
                return;
            }
            controller.submitAccusationWithDiscoveredEvidence(
                    accusedSuspect.id(),
                    "Accusa formulata dalla GUI con le prove scoperte."
            );
        }));
    }

    private SelectionItem toSuspectItem(Suspect suspect) {
        String label = suspect.getName() + " | movente: " + suspect.getMotive();
        return new SelectionItem(suspect.getId(), label);
    }

    private SelectionItem toEvidenceItem(Evidence evidence) {
        String discoveryStatus = evidence.isDiscovered() ? "scoperta" : "non scoperta";
        String linkedSuspects = evidence.getLinkedSuspectIds().isEmpty()
                ? "nessun collegamento"
                : "collegata a: " + evidence.getLinkedSuspectIds().stream().collect(Collectors.joining(", "));
        String label = evidence.getTitle()
                + " | " + evidence.getType()
                + " | " + discoveryStatus
                + " | " + linkedSuspects;
        return new SelectionItem(evidence.getId(), label);
    }

    private String formatTimelineEvent(TimelineEvent event) {
        return TIMELINE_FORMATTER.format(event.getOccurredAt())
                + " | " + event.getTitle()
                + " | " + event.getDescription();
    }

    private void appendLog(String message) {
        logArea.appendText(message + System.lineSeparator());
    }

    private String selectedId(SelectionItem item) {
        return Optional.ofNullable(item)
                .map(SelectionItem::id)
                .orElse(null);
    }

    private void restoreSelection(ListView<SelectionItem> listView, String selectedId) {
        if (selectedId == null) {
            return;
        }

        listView.getItems().stream()
                .filter(item -> item.id().equals(selectedId))
                .findFirst()
                .ifPresent(item -> listView.getSelectionModel().select(item));
    }

    private void restoreSelection(ComboBox<SelectionItem> comboBox, String selectedId) {
        comboBox.setValue(null);

        if (selectedId != null) {
            comboBox.getItems().stream()
                    .filter(item -> item.id().equals(selectedId))
                    .findFirst()
                    .ifPresent(comboBox::setValue);
        }

        if (comboBox.getValue() == null && !comboBox.getItems().isEmpty()) {
            comboBox.getSelectionModel().selectFirst();
        }
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

    private record SelectionItem(String id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
