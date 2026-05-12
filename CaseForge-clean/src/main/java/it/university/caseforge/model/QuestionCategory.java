package it.university.caseforge.model;

public enum QuestionCategory {
    ALIBI("Alibi"),
    MOTIVE("Movente"),
    ACCESS("Accessi"),
    TIMELINE("Cronologia"),
    GENERAL("Generale");

    private final String label;

    QuestionCategory(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
