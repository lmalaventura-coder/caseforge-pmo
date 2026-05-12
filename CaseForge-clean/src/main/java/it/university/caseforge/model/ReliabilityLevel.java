package it.university.caseforge.model;

public enum ReliabilityLevel {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int weight;

    ReliabilityLevel(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
