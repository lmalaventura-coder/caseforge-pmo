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
                .orElseThrow(() -> new IllegalArgumentException("Unknown evidence: " + evidenceId));

        if (!evidence.isDiscovered()) {
            evidence.markDiscovered();
            notifyObservers(InvestigationEvent.evidenceDiscovered(evidence, LocalDateTime.now()));
        }
    }

    public void linkEvidenceToSuspect(String evidenceId, String suspectId) {
        requireOpen();
        Evidence evidence = caseFile.findEvidenceById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown evidence: " + evidenceId));
        Suspect suspect = caseFile.findSuspectById(suspectId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown suspect: " + suspectId));

        if (!evidence.isLinkedTo(suspectId)) {
            evidence.linkToSuspect(suspect);
            notifyObservers(new InvestigationEvent(
                    InvestigationEventType.EVIDENCE_LINKED_TO_SUSPECT,
                    "Evidence linked to suspect.",
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

    public void linkEvidenceToAnswer(
            String evidenceId,
            String suspectId,
            String interrogationId,
            String questionId
    ) {
        requireOpen();
        Evidence evidence = caseFile.findEvidenceById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown evidence: " + evidenceId));
        if (!evidence.isDiscovered()) {
            throw new IllegalStateException("Evidence must be discovered before linking it to an answer.");
        }

        Suspect suspect = caseFile.findSuspectById(suspectId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown suspect: " + suspectId));
        Interrogation interrogation = suspect.getInterrogations().stream()
                .filter(candidate -> candidate.getId().equals(interrogationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown interrogation: " + interrogationId));
        Question question = interrogation.findQuestionById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown question: " + questionId));
        Answer answer = Objects.requireNonNull(question.getAnswer(), "Question has no answer.");

        if (!answer.linkEvidence(evidence)) {
            return;
        }

        notifyObservers(InvestigationEvent.evidenceLinkedToAnswer(evidence, LocalDateTime.now()));
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
                                LocalDateTime.now()
                        ))
                );
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
            throw new IllegalStateException("Investigation is already closed.");
        }
    }
}
