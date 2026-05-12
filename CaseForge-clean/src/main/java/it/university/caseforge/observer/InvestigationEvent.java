package it.university.caseforge.observer;

import it.university.caseforge.model.Accusation;
import it.university.caseforge.model.Contradiction;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.model.Evidence;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class InvestigationEvent {

    private final InvestigationEventType type;
    private final String message;
    private final LocalDateTime occurredAt;
    private final Evidence evidence;
    private final Accusation accusation;
    private final EvaluationResult evaluationResult;
    private final Contradiction contradiction;

    public InvestigationEvent(
            InvestigationEventType type,
            String message,
            LocalDateTime occurredAt,
            Evidence evidence,
            Accusation accusation,
            EvaluationResult evaluationResult,
            Contradiction contradiction
    ) {
        this.type = Objects.requireNonNull(type);
        this.message = message == null ? "" : message;
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.evidence = evidence;
        this.accusation = accusation;
        this.evaluationResult = evaluationResult;
        this.contradiction = contradiction;
    }

    public static InvestigationEvent evidenceDiscovered(Evidence evidence, LocalDateTime occurredAt) {
        return new InvestigationEvent(
                InvestigationEventType.EVIDENCE_DISCOVERED,
                "Evidence discovered.",
                occurredAt,
                Objects.requireNonNull(evidence),
                null,
                null,
                null
        );
    }

    public static InvestigationEvent contradictionDetected(
            Contradiction contradiction,
            LocalDateTime occurredAt
    ) {
        return new InvestigationEvent(
                InvestigationEventType.CONTRADICTION_DETECTED,
                "Contradiction detected.",
                occurredAt,
                contradiction.getEvidence(),
                null,
                null,
                Objects.requireNonNull(contradiction)
        );
    }

    public static InvestigationEvent evidenceLinkedToAnswer(Evidence evidence, LocalDateTime occurredAt) {
        return new InvestigationEvent(
                InvestigationEventType.EVIDENCE_LINKED_TO_ANSWER,
                "Evidence linked to answer.",
                occurredAt,
                Objects.requireNonNull(evidence),
                null,
                null,
                null
        );
    }

    public static InvestigationEvent noContradictionDetected(Evidence evidence, LocalDateTime occurredAt) {
        return new InvestigationEvent(
                InvestigationEventType.NO_CONTRADICTION_DETECTED,
                "No contradiction detected.",
                occurredAt,
                Objects.requireNonNull(evidence),
                null,
                null,
                null
        );
    }

    public static InvestigationEvent caseClosed(
            Accusation accusation,
            EvaluationResult evaluationResult,
            LocalDateTime occurredAt
    ) {
        return new InvestigationEvent(
                InvestigationEventType.CASE_CLOSED,
                "Case closed.",
                occurredAt,
                null,
                Objects.requireNonNull(accusation),
                Objects.requireNonNull(evaluationResult),
                null
        );
    }

    public InvestigationEventType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public Optional<Evidence> getEvidence() {
        return Optional.ofNullable(evidence);
    }

    public Optional<Accusation> getAccusation() {
        return Optional.ofNullable(accusation);
    }

    public Optional<EvaluationResult> getEvaluationResult() {
        return Optional.ofNullable(evaluationResult);
    }

    public Optional<Contradiction> getContradiction() {
        return Optional.ofNullable(contradiction);
    }
}
