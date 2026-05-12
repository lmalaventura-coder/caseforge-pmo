package it.university.caseforge.model;

import java.util.Objects;
import java.util.Optional;

public class Answer {

    private final String text;
    private final ReliabilityLevel reliabilityLevel;
    private final Evidence linkedEvidence;

    public Answer(String text) {
        this(text, ReliabilityLevel.MEDIUM, null);
    }

    public Answer(String text, ReliabilityLevel reliabilityLevel) {
        this(text, reliabilityLevel, null);
    }

    public Answer(String text, ReliabilityLevel reliabilityLevel, Evidence linkedEvidence) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be blank.");
        }
        this.text = text;
        this.reliabilityLevel = Objects.requireNonNull(reliabilityLevel);
        this.linkedEvidence = linkedEvidence;
    }

    public String getText() {
        return text;
    }

    public ReliabilityLevel getReliabilityLevel() {
        return reliabilityLevel;
    }

    public Optional<Evidence> getLinkedEvidence() {
        return Optional.ofNullable(linkedEvidence);
    }
}
