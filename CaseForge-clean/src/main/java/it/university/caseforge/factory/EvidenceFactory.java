package it.university.caseforge.factory;

import it.university.caseforge.model.DigitalEvidence;
import it.university.caseforge.model.PhysicalEvidence;
import it.university.caseforge.model.TestimonyEvidence;

public class EvidenceFactory {

    public PhysicalEvidence createPhysicalEvidence(
            String id,
            String title,
            String description,
            String locationFound,
            String material
    ) {
        return new PhysicalEvidence(id, title, description, locationFound, material);
    }

    public DigitalEvidence createDigitalEvidence(
            String id,
            String title,
            String description,
            String sourceDevice,
            String hash
    ) {
        return new DigitalEvidence(id, title, description, sourceDevice, hash);
    }

    public TestimonyEvidence createTestimonyEvidence(
            String id,
            String title,
            String description,
            String witnessName,
            String statement
    ) {
        return new TestimonyEvidence(id, title, description, witnessName, statement);
    }
}
