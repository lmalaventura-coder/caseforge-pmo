package it.university.caseforge.model;

import java.util.Objects;

public class Contradiction {

    private final String id;
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
        this.id = idFor(suspectId, Objects.requireNonNull(question).getId(), Objects.requireNonNull(evidence).getId());
        this.suspectId = suspectId;
        this.question = question;
        this.answer = Objects.requireNonNull(answer);
        this.evidence = evidence;
        this.explanation = explanation == null ? "" : explanation;
    }

    public String getId() {
        return id;
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

    public static String idFor(String suspectId, String questionId, String evidenceId) {
        return requireText(suspectId, "suspectId")
                + "::"
                + requireText(questionId, "questionId")
                + "::"
                + requireText(evidenceId, "evidenceId");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value;
    }
}
