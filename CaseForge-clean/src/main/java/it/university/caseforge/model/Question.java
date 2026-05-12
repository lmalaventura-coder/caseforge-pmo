package it.university.caseforge.model;

import java.util.Objects;

public class Question {

    private final String text;
    private final QuestionCategory category;
    private Answer answer;

    public Question(String text) {
        this(text, QuestionCategory.GENERAL);
    }

    public Question(String text, QuestionCategory category) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be blank.");
        }
        this.text = text;
        this.category = Objects.requireNonNull(category);
    }

    public String getText() {
        return text;
    }

    public QuestionCategory getCategory() {
        return category;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void answerWith(Answer answer) {
        this.answer = Objects.requireNonNull(answer);
    }
}
