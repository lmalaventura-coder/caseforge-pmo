package it.university.caseforge.model;

public class DigitalEvidence extends AbstractEvidence {

    private final String sourceDevice;
    private final String hash;

    public DigitalEvidence(String id, String title, String description, String sourceDevice, String hash) {
        super(id, title, description);
        this.sourceDevice = requireText(sourceDevice, "sourceDevice");
        this.hash = requireText(hash, "hash");
    }

    @Override
    public EvidenceType getType() {
        return EvidenceType.DIGITAL;
    }

    public String getSourceDevice() {
        return sourceDevice;
    }

    public String getHash() {
        return hash;
    }
}
