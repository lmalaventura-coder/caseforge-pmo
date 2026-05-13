package it.university.caseforge.model;

import it.university.caseforge.factory.DemoCaseFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseFactoryTest {

    @Test
    void demoCaseContainsCoreDomainData() {
        CaseFile caseFile = new DemoCaseFactory().createDemoCase();

        assertEquals("case-001", caseFile.getId());
        assertEquals(4, caseFile.getSuspects().size());
        assertEquals(8, caseFile.getEvidences().size());
        assertEquals(8, caseFile.getTimeline().getEvents().size());
        assertTrue(caseFile.getSolution().isPresent());
    }

    @Test
    void demoCasePresentsACoherentStartupInvestigation() {
        CaseFile caseFile = new DemoCaseFactory().createDemoCase();

        assertEquals("Violazione di mezzanotte in HelixNova", caseFile.getTitle());
        assertTrue(caseFile.getDescription().contains("HelixNova"));
        assertTrue(caseFile.findEvidenceById("ev-email-warning").isPresent());
        assertTrue(caseFile.findEvidenceById("ev-badge-log").isPresent());
        assertTrue(caseFile.findEvidenceById("ev-chat-message").isPresent());
    }

    @Test
    void demoCaseIncludesACredibleInnocentSuspectWithTimelineSupport() {
        CaseFile caseFile = new DemoCaseFactory().createDemoCase();
        Suspect davide = caseFile.findSuspectById("sus-davide-serra").orElseThrow();
        TimelineEvent parkingExit = caseFile.getTimeline().getEvents().stream()
                .filter(event -> event.getId().equals("tl-parking-exit"))
                .findFirst()
                .orElseThrow();

        assertFalse(davide.getMotive().isBlank());
        assertTrue(caseFile.findEvidenceById("ev-parking-ticket").isPresent());
        assertEquals("sus-marta-greco", caseFile.getSolution().orElseThrow().getCulpritSuspectId());
        assertFalse(caseFile.getSolution().orElseThrow().getCulpritSuspectId().equals(davide.getId()));
        assertEquals(davide.getId(), parkingExit.getRelatedSuspectId().orElseThrow());
    }

    @Test
    void demoCaseContainsAnAmbiguousLeadAndAConvincingFalseTrail() {
        CaseFile caseFile = new DemoCaseFactory().createDemoCase();
        Evidence badgeLog = caseFile.findEvidenceById("ev-badge-log").orElseThrow();
        Suspect davide = caseFile.findSuspectById("sus-davide-serra").orElseThrow();

        assertTrue(badgeLog.getDescription().contains("non prova da solo"));
        assertTrue(davide.getMotive().contains("pista forte"));
    }

    @Test
    void demoCaseKeepsAnOptionalContradictionBeyondThePrimarySolution() {
        CaseFile caseFile = new DemoCaseFactory().createDemoCase();
        Question optionalQuestion = caseFile.findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getInterrogations()
                .get(0)
                .findQuestionById("q-marta-meeting-room")
                .orElseThrow();

        assertEquals("ev-fingerprint", optionalQuestion.getAnswer()
                .getContradictionEvidence()
                .orElseThrow()
                .getId());
    }

    @Test
    void everyDemoSuspectHasAtLeastThreeAvailableQuestions() {
        CaseFile caseFile = new DemoCaseFactory().createDemoCase();

        for (Suspect suspect : caseFile.getSuspects()) {
            int questionCount = suspect.getInterrogations().stream()
                    .mapToInt(interrogation -> interrogation.getQuestions().size())
                    .sum();

            assertTrue(questionCount >= 3, suspect.getName() + " should have at least three questions");
        }
    }
}
