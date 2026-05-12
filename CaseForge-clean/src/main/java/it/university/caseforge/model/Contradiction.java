package it.university.caseforge.model;

import java.util.Objects;

public class Contradiction {

    private final String suspectId;
    private final Question question;
    private final Answer answer;
    private final Evidence evidence;
    private final String explanation;

    public Contradiction(
            String suspectId,
            Question question,
            Answer answer,
            Evidence evidence,
            String explanation
    ) {
        if (suspectId == null || suspectId.isBlank()) {
            throw new IllegalArgumentException("suspectId cannot be blank.");
        }
        this.suspectId = suspectId;
        this.question = Objects.requireNonNull(question);
        this.answer = Objects.requireNonNull(answer);
        this.evidence = Objects.requireNonNull(evidence);
        this.explanation = explanation == null ? "" : explanation;
    }

    public String getSuspectId() {
        return suspectId;
    }

    public Question getQuestion() {
        return question;
    }

    public Answer getAnswer() {
        return answer;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public String getExplanation() {
        return explanation;
    }
}
