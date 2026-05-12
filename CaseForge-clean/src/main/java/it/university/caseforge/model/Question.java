package it.university.caseforge.model;

import java.util.Objects;

public class Question {

    private final String text;
    private Answer answer;

    public Question(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be blank.");
        }
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void answerWith(Answer answer) {
        this.answer = Objects.requireNonNull(answer);
    }
}
