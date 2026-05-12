package it.university.caseforge.model;

import it.university.caseforge.factory.DemoCaseFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenceLinkingTest {

    @Test
    void evidenceCanBeLinkedToASuspect() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());

        investigation.linkEvidenceToSuspect("ev-fingerprint", "sus-marta-greco");

        Evidence evidence = investigation.getCaseFile().findEvidenceById("ev-fingerprint").orElseThrow();
        assertTrue(evidence.isLinkedTo("sus-marta-greco"));
    }
}
