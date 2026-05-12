package it.university.caseforge.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Answer {

    private final String id;
    private final String text;
    private final ReliabilityLevel reliabilityLevel;
    private final Evidence contradictionEvidence;
    private final Set<String> linkedEvidenceIds = new LinkedHashSet<>();

    public Answer(String text) {
        this(defaultId(text), text, ReliabilityLevel.MEDIUM, null);
    }

    public Answer(String text, ReliabilityLevel reliabilityLevel) {
        this(defaultId(text), text, reliabilityLevel, null);
    }

    public Answer(String text, ReliabilityLevel reliabilityLevel, Evidence contradictionEvidence) {
        this(defaultId(text), text, reliabilityLevel, contradictionEvidence);
    }

    public Answer(String id, String text, ReliabilityLevel reliabilityLevel) {
        this(id, text, reliabilityLevel, null);
    }

    public Answer(String id, String text, ReliabilityLevel reliabilityLevel, Evidence contradictionEvidence) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id non puo essere vuoto.");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text non puo essere vuoto.");
        }
        this.id = id;
        this.text = text;
        this.reliabilityLevel = Objects.requireNonNull(reliabilityLevel);
        this.contradictionEvidence = contradictionEvidence;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public ReliabilityLevel getReliabilityLevel() {
        return reliabilityLevel;
    }

    public Optional<Evidence> getContradictionEvidence() {
        return Optional.ofNullable(contradictionEvidence);
    }

    public Optional<Evidence> getLinkedEvidence() {
        return getContradictionEvidence();
    }

    public boolean linkEvidence(Evidence evidence) {
        return linkedEvidenceIds.add(Objects.requireNonNull(evidence).getId());
    }

    public boolean isLinkedToEvidence(String evidenceId) {
        return linkedEvidenceIds.contains(evidenceId);
    }

    public Set<String> getLinkedEvidenceIds() {
        return Collections.unmodifiableSet(linkedEvidenceIds);
    }

    private static String defaultId(String text) {
        return "answer-" + Math.abs(Objects.requireNonNull(text).hashCode());
    }
}
