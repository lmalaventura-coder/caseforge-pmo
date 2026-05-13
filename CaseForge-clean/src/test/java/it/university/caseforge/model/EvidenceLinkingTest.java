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
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());

        investigation.linkEvidenceToSuspect("ev-call-record", "sus-luca-conti");

        Evidence evidence = investigation.getCaseFile().findEvidenceById("ev-call-record").orElseThrow();
        assertTrue(evidence.isLinkedTo("sus-luca-conti"));
    }

    @Test
    void linkingSameEvidenceTwiceNotifiesOnlyOnce() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);

        investigation.linkEvidenceToSuspect("ev-call-record", "sus-luca-conti");
        investigation.linkEvidenceToSuspect("ev-call-record", "sus-luca-conti");

        assertEquals(1, events.size());
        assertEquals(InvestigationEventType.EVIDENCE_LINKED_TO_SUSPECT, events.get(0).getType());
    }
}
