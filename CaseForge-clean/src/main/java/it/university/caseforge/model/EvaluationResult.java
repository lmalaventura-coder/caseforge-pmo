package it.university.caseforge.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class EvaluationResult {

    private final boolean solved;
    private final boolean correctSuspect;
    private final boolean correctPrimaryEvidence;
    private final boolean correctPrimaryContradiction;
    private final boolean correctTimelineEvent;
    private final int score;
    private final String message;
    private final Set<String> missingEvidenceIds;

    public EvaluationResult(
            boolean solved,
            boolean correctSuspect,
            boolean correctPrimaryEvidence,
            boolean correctPrimaryContradiction,
            boolean correctTimelineEvent,
            int score,
            String message,
            Set<String> missingEvidenceIds
    ) {
        this.solved = solved;
        this.correctSuspect = correctSuspect;
        this.correctPrimaryEvidence = correctPrimaryEvidence;
        this.correctPrimaryContradiction = correctPrimaryContradiction;
        this.correctTimelineEvent = correctTimelineEvent;
        this.score = Math.max(0, Math.min(100, score));
        this.message = Objects.requireNonNullElse(message, "");
        this.missingEvidenceIds = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(missingEvidenceIds)));
    }

    public boolean isSolved() {
        return solved;
    }

    public boolean isCorrectSuspect() {
        return correctSuspect;
    }

    public boolean isCorrectPrimaryEvidence() {
        return correctPrimaryEvidence;
    }

    public boolean isCorrectPrimaryContradiction() {
        return correctPrimaryContradiction;
    }

    public boolean isCorrectTimelineEvent() {
        return correctTimelineEvent;
    }

    public int getScore() {
        return score;
    }

    public String getMessage() {
        return message;
    }

    public Set<String> getMissingEvidenceIds() {
        return missingEvidenceIds;
    }
}
