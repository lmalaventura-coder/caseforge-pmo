package it.university.caseforge.model;

import it.university.caseforge.factory.DemoCaseFactory;
import it.university.caseforge.observer.InvestigationEvent;
import it.university.caseforge.observer.InvestigationEventType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvestigationObserverTest {

    @Test
    void discoveringEvidenceNotifiesObservers() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);

        investigation.discoverEvidence("ev-fingerprint");

        assertEquals(1, events.size());
        assertEquals(InvestigationEventType.EVIDENCE_DISCOVERED, events.get(0).getType());
        assertTrue(events.get(0).getEvidence().orElseThrow().isDiscovered());
    }
}
