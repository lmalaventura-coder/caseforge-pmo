package it.university.caseforge.model;

import it.university.caseforge.factory.DemoCaseFactory;
import it.university.caseforge.factory.EvidenceFactory;
import it.university.caseforge.observer.InvestigationEvent;
import it.university.caseforge.observer.InvestigationEventType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterrogationSystemTest {

    @Test
    void questionAndAnswerExposeInterrogationMetadata() {
        Evidence evidence = new EvidenceFactory().createDigitalEvidence(
                "ev-device",
                "Device trace",
                "Trace from a suspect laptop.",
                "Laptop",
                "hash-demo"
        );
        Question question = new Question("Were you online?", QuestionCategory.ACCESS);
        Answer answer = new Answer("No.", ReliabilityLevel.HIGH, evidence);

        question.answerWith(answer);

        assertEquals(QuestionCategory.ACCESS, question.getCategory());
        assertEquals(ReliabilityLevel.HIGH, question.getAnswer().getReliabilityLevel());
        assertSame(evidence, question.getAnswer().getLinkedEvidence().orElseThrow());
    }

    @Test
    void caseBuilderAttachesInterrogationsToTheirSuspect() {
        Suspect suspect = new Suspect("sus-1", "Elena Rossi", "", "", "");
        Interrogation interrogation = new Interrogation(
                "int-1",
                suspect,
                LocalDateTime.of(2026, 5, 12, 10, 0)
        );

        CaseFile caseFile = CaseFile.builder("case-interrogation", "Interrogation Case")
                .addSuspect(suspect)
                .addInterrogation(interrogation)
                .build();

        assertEquals(1, caseFile.getInterrogations().size());
        assertEquals(1, suspect.getInterrogations().size());
    }

    @Test
    void discoveredContradictingEvidenceReducesReliabilityAndStoresContradiction() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
        Suspect marta = investigation.getCaseFile().findSuspectById("sus-marta-greco").orElseThrow();

        investigation.discoverEvidence("ev-server-log");

        assertEquals(70, marta.getReliabilityScore());
        assertEquals(1, marta.getInterrogations().get(0).getContradictions().size());
    }

    @Test
    void discoveringSameEvidenceTwiceDoesNotDuplicateContradictions() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
        Suspect marta = investigation.getCaseFile().findSuspectById("sus-marta-greco").orElseThrow();

        investigation.discoverEvidence("ev-server-log");
        investigation.discoverEvidence("ev-server-log");

        assertEquals(70, marta.getReliabilityScore());
        assertEquals(1, marta.getInterrogations().get(0).getContradictions().size());
    }

    @Test
    void contradictionDetectionNotifiesObservers() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);

        investigation.discoverEvidence("ev-server-log");

        assertTrue(events.stream()
                .anyMatch(event -> event.getType() == InvestigationEventType.CONTRADICTION_DETECTED));
    }
}
