package it.university.caseforge.model;

import it.university.caseforge.factory.DemoCaseFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseFactoryTest {

    @Test
    void demoCaseContainsCoreDomainData() {
        CaseFile caseFile = new DemoCaseFactory().createDemoCase();

        assertEquals("case-001", caseFile.getId());
        assertEquals(2, caseFile.getSuspects().size());
        assertEquals(3, caseFile.getEvidences().size());
        assertEquals(3, caseFile.getTimeline().getEvents().size());
        assertTrue(caseFile.getSolution().isPresent());
    }
}
