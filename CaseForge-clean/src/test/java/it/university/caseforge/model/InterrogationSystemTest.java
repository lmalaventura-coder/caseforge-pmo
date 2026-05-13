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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        Suspect marta = investigation.getCaseFile().findSuspectById("sus-marta-greco").orElseThrow();

        investigation.discoverEvidence("ev-server-log");
        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        investigation.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );

        assertEquals(70, marta.getReliabilityScore());
        assertEquals(1, marta.getInterrogations().get(0).getContradictions().size());
    }

    @Test
    void discoveringSameEvidenceTwiceDoesNotDuplicateContradictions() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        Suspect marta = investigation.getCaseFile().findSuspectById("sus-marta-greco").orElseThrow();

        investigation.discoverEvidence("ev-server-log");
        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        investigation.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        investigation.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );

        assertEquals(70, marta.getReliabilityScore());
        assertEquals(1, marta.getInterrogations().get(0).getContradictions().size());
    }

    @Test
    void contradictionDetectionNotifiesObservers() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);

        investigation.discoverEvidence("ev-server-log");
        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        investigation.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );

        assertTrue(events.stream()
                .anyMatch(event -> event.getType() == InvestigationEventType.CONTRADICTION_DETECTED));
    }

    @Test
    void discoveringEvidenceDoesNotChangeReliability() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        Suspect marta = investigation.getCaseFile().findSuspectById("sus-marta-greco").orElseThrow();

        investigation.discoverEvidence("ev-server-log");

        assertEquals(100, marta.getReliabilityScore());
        assertEquals(0, marta.getInterrogations().get(0).getContradictions().size());
    }

    @Test
    void linkingCompatibleEvidenceDoesNotChangeReliability() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        Suspect sofia = investigation.getCaseFile().findSuspectById("sus-sofia-rinaldi").orElseThrow();

        investigation.discoverEvidence("ev-chat-message");
        investigation.askQuestion(
                "sus-sofia-rinaldi",
                "int-sofia-001",
                "q-sofia-release-bridge"
        );
        investigation.linkEvidenceToAnswer(
                "ev-chat-message",
                "sus-sofia-rinaldi",
                "int-sofia-001",
                "q-sofia-release-bridge"
        );

        assertEquals(100, sofia.getReliabilityScore());
        assertEquals(0, sofia.getInterrogations().get(0).getContradictions().size());
    }

    @Test
    void linkingSameEvidenceToSameAnswerDoesNotDuplicateEvents() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);
        investigation.discoverEvidence("ev-chat-message");
        investigation.askQuestion(
                "sus-sofia-rinaldi",
                "int-sofia-001",
                "q-sofia-release-bridge"
        );

        investigation.linkEvidenceToAnswer(
                "ev-chat-message",
                "sus-sofia-rinaldi",
                "int-sofia-001",
                "q-sofia-release-bridge"
        );
        investigation.linkEvidenceToAnswer(
                "ev-chat-message",
                "sus-sofia-rinaldi",
                "int-sofia-001",
                "q-sofia-release-bridge"
        );

        long linkedEvents = events.stream()
                .filter(event -> event.getType() == InvestigationEventType.EVIDENCE_LINKED_TO_ANSWER)
                .count();
        long noContradictionEvents = events.stream()
                .filter(event -> event.getType() == InvestigationEventType.NO_CONTRADICTION_DETECTED)
                .count();

        assertEquals(1, linkedEvents);
        assertEquals(1, noContradictionEvents);
    }

    @Test
    void askingQuestionStoresTheObtainedAnswerAndNotifiesObservers() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);

        Question question = investigation.getCaseFile()
                .findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getInterrogations()
                .get(0)
                .findQuestionById("q-marta-server-access")
                .orElseThrow();

        assertFalse(question.isAnswerObtained());

        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );

        assertTrue(question.isAnswerObtained());
        assertTrue(events.stream()
                .anyMatch(event -> event.getType() == InvestigationEventType.ANSWER_OBTAINED));
    }

    @Test
    void askingAChosenQuestionRevealsOnlyThatAnswer() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        Interrogation interrogation = investigation.getCaseFile()
                .findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getInterrogations()
                .get(0);
        Question chosenQuestion = interrogation.findQuestionById("q-marta-audit-worry").orElseThrow();
        Question untouchedQuestion = interrogation.findQuestionById("q-marta-server-access").orElseThrow();

        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-audit-worry"
        );

        assertTrue(chosenQuestion.isAnswerObtained());
        assertFalse(untouchedQuestion.isAnswerObtained());
    }

    @Test
    void sameSuspectCanAnswerMultipleExplicitQuestions() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        Interrogation interrogation = investigation.getCaseFile()
                .findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getInterrogations()
                .get(0);

        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-audit-worry"
        );
        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-meeting-room"
        );

        long obtainedAnswers = interrogation.getQuestions().stream()
                .filter(Question::isAnswerObtained)
                .count();

        assertEquals(2, obtainedAnswers);
    }

    @Test
    void obtainedAnswerRemainsAvailableForTheInterrogationDossier() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        Question question = investigation.getCaseFile()
                .findSuspectById("sus-sofia-rinaldi")
                .orElseThrow()
                .getInterrogations()
                .get(0)
                .findQuestionById("q-sofia-release-bridge")
                .orElseThrow();

        investigation.askQuestion(
                "sus-sofia-rinaldi",
                "int-sofia-001",
                "q-sofia-release-bridge"
        );

        assertTrue(question.isAnswerObtained());
        assertEquals(
                "Si. Coordinavo i passaggi di rollback in chat e nella chiamata dell'incidente.",
                question.getAnswer().getText()
        );
    }

    @Test
    void askingTheSameQuestionTwiceDoesNotDuplicateAnswerEvents() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);

        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-audit-worry"
        );
        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-audit-worry"
        );

        long answerEvents = events.stream()
                .filter(event -> event.getType() == InvestigationEventType.ANSWER_OBTAINED)
                .count();

        assertEquals(1, answerEvents);
    }

    @Test
    void evidenceCannotBeLinkedBeforeAnswerIsObtained() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDefaultCase());
        investigation.discoverEvidence("ev-server-log");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> investigation.linkEvidenceToAnswer(
                        "ev-server-log",
                        "sus-marta-greco",
                        "int-marta-001",
                        "q-marta-server-access"
                )
        );

        assertTrue(exception.getMessage().contains("ottenere la risposta"));
    }
}
