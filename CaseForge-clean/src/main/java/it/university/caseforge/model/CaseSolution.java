package it.university.caseforge.model;

public class CaseSolution {

    private final String culpritSuspectId;
    private final String primaryEvidenceId;
    private final String primaryContradictionId;
    private final String relevantTimelineEventId;
    private final String explanation;

    public CaseSolution(
            String culpritSuspectId,
            String primaryEvidenceId,
            String primaryContradictionId,
            String relevantTimelineEventId,
            String explanation
    ) {
        this.culpritSuspectId = requireText(culpritSuspectId, "culpritSuspectId");
        this.primaryEvidenceId = requireText(primaryEvidenceId, "primaryEvidenceId");
        this.primaryContradictionId = requireText(primaryContradictionId, "primaryContradictionId");
        this.relevantTimelineEventId = requireText(relevantTimelineEventId, "relevantTimelineEventId");
        this.explanation = explanation == null ? "" : explanation;
    }

    public String getCulpritSuspectId() {
        return culpritSuspectId;
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

    public String getExplanation() {
        return explanation;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " non puo essere vuoto.");
        }
        return value;
    }
}
