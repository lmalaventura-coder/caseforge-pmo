package it.university.caseforge.model;

public enum EvidenceType {
    PHYSICAL("Fisica"),
    DIGITAL("Digitale"),
    TESTIMONY("Testimonianza");

    private final String label;

    EvidenceType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
