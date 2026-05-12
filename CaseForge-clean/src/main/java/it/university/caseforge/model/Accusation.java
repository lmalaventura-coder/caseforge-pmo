package it.university.caseforge.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Accusation {

    private final String suspectId;
    private final Set<String> evidenceIds;
    private final String reasoning;

    public Accusation(String suspectId, Set<String> evidenceIds, String reasoning) {
        if (suspectId == null || suspectId.isBlank()) {
            throw new IllegalArgumentException("suspectId cannot be blank.");
        }
        this.suspectId = suspectId;
        this.evidenceIds = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(evidenceIds)));
        this.reasoning = reasoning == null ? "" : reasoning;
    }

    public String getSuspectId() {
        return suspectId;
    }

    public Set<String> getEvidenceIds() {
        return evidenceIds;
    }

    public String getReasoning() {
        return reasoning;
    }
}
