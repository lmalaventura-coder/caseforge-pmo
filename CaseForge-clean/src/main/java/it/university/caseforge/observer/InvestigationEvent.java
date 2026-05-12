package it.university.caseforge.observer;

import it.university.caseforge.model.Accusation;
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

    public InvestigationEvent(
            InvestigationEventType type,
            String message,
            LocalDateTime occurredAt,
            Evidence evidence,
            Accusation accusation,
            EvaluationResult evaluationResult
    ) {
        this.type = Objects.requireNonNull(type);
        this.message = message == null ? "" : message;
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.evidence = evidence;
        this.accusation = accusation;
        this.evaluationResult = evaluationResult;
    }

    public static InvestigationEvent evidenceDiscovered(Evidence evidence, LocalDateTime occurredAt) {
        return new InvestigationEvent(
                InvestigationEventType.EVIDENCE_DISCOVERED,
                "Evidence discovered.",
                occurredAt,
                Objects.requireNonNull(evidence),
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
                Objects.requireNonNull(evaluationResult)
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
}
