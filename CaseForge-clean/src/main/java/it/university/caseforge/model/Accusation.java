package it.university.caseforge.model;

public class Accusation {

    private final String suspectId;
    private final String primaryEvidenceId;
    private final String primaryContradictionId;
    private final String relevantTimelineEventId;
    private final String reasoning;

    public Accusation(
            String suspectId,
            String primaryEvidenceId,
            String primaryContradictionId,
            String relevantTimelineEventId,
            String reasoning
    ) {
        this.suspectId = requireText(suspectId, "suspectId");
        this.primaryEvidenceId = requireText(primaryEvidenceId, "primaryEvidenceId");
        this.primaryContradictionId = requireText(primaryContradictionId, "primaryContradictionId");
        this.relevantTimelineEventId = requireText(relevantTimelineEventId, "relevantTimelineEventId");
        this.reasoning = reasoning == null ? "" : reasoning;
    }

    public String getSuspectId() {
        return suspectId;
    }

    public String getPrimaryEvidenceId() {
        return primaryEvidenceId;
    }

    public String getPrimaryContradictionId() {
        return primaryContradictionId;
    }

    public String getRelevantTimelineEventId() {
        return relevantTimelineEventId;
    }

    public String getReasoning() {
        return reasoning;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " non puo essere vuoto.");
        }
        return value;
    }
}
