package it.university.caseforge.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class CaseSolution {

    private final String culpritSuspectId;
    private final Set<String> requiredEvidenceIds;
    private final String explanation;

    public CaseSolution(String culpritSuspectId, Set<String> requiredEvidenceIds, String explanation) {
        if (culpritSuspectId == null || culpritSuspectId.isBlank()) {
            throw new IllegalArgumentException("culpritSuspectId cannot be blank.");
        }
        this.culpritSuspectId = culpritSuspectId;
        this.requiredEvidenceIds = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(requiredEvidenceIds)));
        this.explanation = explanation == null ? "" : explanation;
    }

    public String getCulpritSuspectId() {
        return culpritSuspectId;
    }

    public Set<String> getRequiredEvidenceIds() {
        return requiredEvidenceIds;
    }

    public String getExplanation() {
        return explanation;
    }
}
