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
            detectContradictions(evidence);
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
                    InvestigationEventType.EVIDENCE_LINKED,
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

    private void closeCase() {
        status = InvestigationStatus.CLOSED;
        notifyObservers(InvestigationEvent.caseClosed(lastAccusation, lastEvaluationResult, LocalDateTime.now()));
    }

    private void detectContradictions(Evidence evidence) {
        for (Suspect suspect : caseFile.getSuspects()) {
            for (Interrogation interrogation : suspect.getInterrogations()) {
                for (Question question : interrogation.getQuestions()) {
                    contradictionStrategy.evaluate(suspect, question, evidence)
                            .filter(interrogation::addContradiction)
                            .ifPresent(contradiction -> {
                                suspect.decreaseReliability(
                                        contradiction.getAnswer().getReliabilityLevel().getWeight() * 10
                                );
                                notifyObservers(InvestigationEvent.contradictionDetected(
                                        contradiction,
                                        LocalDateTime.now()
                                ));
                            });
                }
            }
        }
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
