package it.university.caseforge.model;

import it.university.caseforge.factory.DemoCaseFactory;
import it.university.caseforge.observer.InvestigationEvent;
import it.university.caseforge.observer.InvestigationEventType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenceLinkingTest {

    @Test
    void evidenceCanBeLinkedToASuspect() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());

        investigation.linkEvidenceToSuspect("ev-fingerprint", "sus-marta-greco");

        Evidence evidence = investigation.getCaseFile().findEvidenceById("ev-fingerprint").orElseThrow();
        assertTrue(evidence.isLinkedTo("sus-marta-greco"));
    }

    @Test
    void linkingSameEvidenceTwiceNotifiesOnlyOnce() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);

        investigation.linkEvidenceToSuspect("ev-fingerprint", "sus-marta-greco");
        investigation.linkEvidenceToSuspect("ev-fingerprint", "sus-marta-greco");

        assertEquals(1, events.size());
        assertEquals(InvestigationEventType.EVIDENCE_LINKED, events.get(0).getType());
    }
}
