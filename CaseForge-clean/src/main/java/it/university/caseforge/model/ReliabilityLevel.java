package it.university.caseforge.model;

public enum ReliabilityLevel {
    LOW(1, "Bassa"),
    MEDIUM(2, "Media"),
    HIGH(3, "Alta");

    private final int weight;
    private final String label;

    ReliabilityLevel(int weight, String label) {
        this.weight = weight;
        this.label = label;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return label;
    }
}
