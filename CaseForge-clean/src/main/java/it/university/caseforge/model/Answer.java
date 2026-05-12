package it.university.caseforge.model;

public class Answer {

    private final String text;

    public Answer(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be blank.");
        }
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
