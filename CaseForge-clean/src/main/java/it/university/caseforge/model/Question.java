package it.university.caseforge.model;

import java.util.Objects;

public class Question {

    private final String id;
    private final String text;
    private final QuestionCategory category;
    private Answer answer;

    public Question(String text) {
        this(defaultId(text), text, QuestionCategory.GENERAL);
    }

    public Question(String text, QuestionCategory category) {
        this(defaultId(text), text, category);
    }

    public Question(String id, String text, QuestionCategory category) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id non puo essere vuoto.");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text non puo essere vuoto.");
        }
        this.id = id;
        this.text = text;
        this.category = Objects.requireNonNull(category);
    }

    public String getId() {
        return id;
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

    private static String defaultId(String text) {
        return "question-" + Math.abs(Objects.requireNonNull(text).hashCode());
    }
}
