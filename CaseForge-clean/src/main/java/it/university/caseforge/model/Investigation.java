package it.university.caseforge.model;

import it.university.caseforge.observer.InvestigationEvent;
import it.university.caseforge.observer.InvestigationEventType;
import it.university.caseforge.observer.InvestigationObserver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Investigation {

    private final CaseFile caseFile;
    private final ContradictionEvaluationStrategy contradictionStrategy;
    private final List<InvestigationObserver> observers = new ArrayList<>();

    private InvestigationStatus status = InvestigationStatus.OPEN;
    private Accusation lastAccusation;
    private EvaluationResult lastEvaluationResult;

    public Investigation(CaseFile caseFile) {
        this(caseFile, new LinkedEvidenceContradictionStrategy());
    }

    public Investigation(CaseFile caseFile, ContradictionEvaluationStrategy contradictionStrategy) {
        this.caseFile = Objects.requireNonNull(caseFile);
        this.contradictionStrategy = Objects.requireNonNull(contradictionStrategy);
    }

    public CaseFile getCaseFile() {
        return caseFile;
    }

    public InvestigationStatus getStatus() {
        return status;
    }

    public Accusation getLastAccusation() {
        return lastAccusation;
    }

    public EvaluationResult getLastEvaluationResult() {
        return lastEvaluationResult;
    }

    public void addObserver(InvestigationObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    public void removeObserver(InvestigationObserver observer) {
        observers.remove(observer);
    }

    public void discoverEvidence(String evidenceId) {
        requireOpen();
        Evidence evidence = caseFile.findEvidenceById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Prova sconosciuta: " + evidenceId));

        if (!evidence.isDiscovered()) {
            evidence.markDiscovered();
            notifyObservers(InvestigationEvent.evidenceDiscovered(evidence, LocalDateTime.now()));
        }
    }

    public void linkEvidenceToSuspect(String evidenceId, String suspectId) {
        requireOpen();
        Evidence evidence = caseFile.findEvidenceById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Prova sconosciuta: " + evidenceId));
        Suspect suspect = caseFile.findSuspectById(suspectId)
                .orElseThrow(() -> new IllegalArgumentException("Sospetto sconosciuto: " + suspectId));

        if (!evidence.isLinkedTo(suspectId)) {
            evidence.linkToSuspect(suspect);
            notifyObservers(new InvestigationEvent(
                    InvestigationEventType.EVIDENCE_LINKED_TO_SUSPECT,
                    "Prova collegata al sospetto.",
                    LocalDateTime.now(),
                    evidence,
                    null,
                    null,
                    null
            ));
        }
    }

    public EvaluationResult formulateAccusation(Accusation accusation, DeductionEngine deductionEngine) {
        requireOpen();
        lastAccusation = Objects.requireNonNull(accusation);
        status = InvestigationStatus.ACCUSATION_SUBMITTED;
        lastEvaluationResult = Objects.requireNonNull(deductionEngine).evaluate(caseFile, accusation);
        closeCase();
        return lastEvaluationResult;
    }

    public void restoreState(
            InvestigationStatus status,
            Accusation lastAccusation,
            EvaluationResult lastEvaluationResult
    ) {
        this.status = Objects.requireNonNull(status);
        this.lastAccusation = lastAccusation;
        this.lastEvaluationResult = lastEvaluationResult;
    }

    public void askQuestion(
            String suspectId,
            String interrogationId,
            String questionId
    ) {
        requireOpen();
        Suspect suspect = findSuspect(suspectId);
        Interrogation interrogation = findInterrogation(suspect, interrogationId);
        Question question = findQuestion(interrogation, questionId);

        if (question.revealAnswer()) {
            notifyObservers(InvestigationEvent.answerObtained(question, LocalDateTime.now()));
        }
    }

    public void linkEvidenceToAnswer(
            String evidenceId,
            String suspectId,
            String interrogationId,
            String questionId
    ) {
        requireOpen();
        Evidence evidence = caseFile.findEvidenceById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Prova sconosciuta: " + evidenceId));
        if (!evidence.isDiscovered()) {
            throw new IllegalStateException("La prova deve essere scoperta prima di collegarla a una risposta.");
        }

        Suspect suspect = findSuspect(suspectId);
        Interrogation interrogation = findInterrogation(suspect, interrogationId);
        Question question = findQuestion(interrogation, questionId);
        if (!question.isAnswerObtained()) {
            throw new IllegalStateException(
                    "Occorre ottenere la risposta prima di collegare una prova a questa domanda."
            );
        }
        Answer answer = Objects.requireNonNull(question.getAnswer(), "La domanda non ha una risposta.");

        if (!answer.linkEvidence(evidence)) {
            return;
        }

        notifyObservers(InvestigationEvent.evidenceLinkedToAnswer(evidence, question, LocalDateTime.now()));
        contradictionStrategy.evaluate(suspect, question, evidence)
                .filter(interrogation::addContradiction)
                .ifPresentOrElse(
                        contradiction -> {
                            suspect.decreaseReliability(
                                    contradiction.getAnswer().getReliabilityLevel().getWeight() * 10
                            );
                            notifyObservers(InvestigationEvent.contradictionDetected(
                                    contradiction,
                                    LocalDateTime.now()
                            ));
                        },
                        () -> notifyObservers(InvestigationEvent.noContradictionDetected(
                                evidence,
                                question,
                                LocalDateTime.now()
                        ))
                );
    }

    private Suspect findSuspect(String suspectId) {
        return caseFile.findSuspectById(suspectId)
                .orElseThrow(() -> new IllegalArgumentException("Sospetto sconosciuto: " + suspectId));
    }

    private Interrogation findInterrogation(Suspect suspect, String interrogationId) {
        return suspect.getInterrogations().stream()
                .filter(candidate -> candidate.getId().equals(interrogationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Interrogatorio sconosciuto: " + interrogationId));
    }

    private Question findQuestion(Interrogation interrogation, String questionId) {
        return interrogation.findQuestionById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Domanda sconosciuta: " + questionId));
    }

    private void closeCase() {
        status = InvestigationStatus.CLOSED;
        notifyObservers(InvestigationEvent.caseClosed(lastAccusation, lastEvaluationResult, LocalDateTime.now()));
    }

    private void notifyObservers(InvestigationEvent event) {
        for (InvestigationObserver observer : List.copyOf(observers)) {
            observer.onInvestigationEvent(event);
        }
    }

    private void requireOpen() {
        if (status == InvestigationStatus.CLOSED) {
            throw new IllegalStateException("L'indagine e gia chiusa.");
        }
    }
}
