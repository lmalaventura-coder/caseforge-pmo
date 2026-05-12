package it.university.caseforge.model;

public class PhysicalEvidence extends AbstractEvidence {

    private final String locationFound;
    private final String material;

    public PhysicalEvidence(String id, String title, String description, String locationFound, String material) {
        super(id, title, description);
        this.locationFound = requireText(locationFound, "locationFound");
        this.material = requireText(material, "material");
    }

    @Override
    public EvidenceType getType() {
        return EvidenceType.PHYSICAL;
    }

    public String getLocationFound() {
        return locationFound;
    }

    public String getMaterial() {
        return material;
    }
}
