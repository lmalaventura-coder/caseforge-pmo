package it.university.caseforge.model;

import it.university.caseforge.factory.DemoCaseFactory;
import it.university.caseforge.observer.InvestigationEvent;
import it.university.caseforge.observer.InvestigationEventType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccusationEvaluationTest {

    @Test
    void correctAccusationSolvesAndClosesCase() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
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

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation(
                        "sus-marta-greco",
                        "ev-server-log",
                        Contradiction.idFor(
                                "sus-marta-greco",
                                "q-marta-server-access",
                                "ev-server-log"
                        ),
                        "tl-server-export",
                        "Evidence, contradiction, and timeline align."
                ),
                new DeductionEngine(new StrictAccusationEvaluationStrategy())
        );

        assertTrue(result.isSolved());
        assertTrue(result.isCorrectPrimaryEvidence());
        assertTrue(result.isCorrectPrimaryContradiction());
        assertTrue(result.isCorrectTimelineEvent());
        assertEquals(100, result.getScore());
        assertEquals(InvestigationStatus.CLOSED, investigation.getStatus());
        assertEquals(InvestigationEventType.CASE_CLOSED, events.get(events.size() - 1).getType());
    }

    @Test
    void wrongSuspectDoesNotSolveCase() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
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

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation(
                        "sus-luca-conti",
                        "ev-server-log",
                        Contradiction.idFor(
                                "sus-marta-greco",
                                "q-marta-server-access",
                                "ev-server-log"
                        ),
                        "tl-server-export",
                        "Looks suspicious."
                ),
                new DeductionEngine(new StrictAccusationEvaluationStrategy())
        );

        assertFalse(result.isSolved());
        assertFalse(result.isCorrectSuspect());
        assertFalse(result.isCorrectPrimaryContradiction());
        assertEquals(50, result.getScore());
    }

    @Test
    void undiscoveredEvidenceDoesNotCountForFinalAccusation() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation(
                        "sus-marta-greco",
                        "ev-server-log",
                        Contradiction.idFor(
                                "sus-marta-greco",
                                "q-marta-server-access",
                                "ev-server-log"
                        ),
                        "tl-server-export",
                        "One clue is missing."
                ),
                new DeductionEngine(new StrictAccusationEvaluationStrategy())
        );

        assertFalse(result.isSolved());
        assertTrue(result.isCorrectSuspect());
        assertFalse(result.isCorrectPrimaryEvidence());
        assertFalse(result.isCorrectPrimaryContradiction());
        assertEquals(java.util.Set.of("ev-server-log"), result.getMissingEvidenceIds());
        assertEquals(50, result.getScore());
    }

    @Test
    void correctEvidenceWithoutConfirmedContradictionDoesNotSolveCase() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
        investigation.discoverEvidence("ev-server-log");

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation(
                        "sus-marta-greco",
                        "ev-server-log",
                        Contradiction.idFor(
                                "sus-marta-greco",
                                "q-marta-server-access",
                                "ev-server-log"
                        ),
                        "tl-server-export",
                        "The contradiction has not been confirmed."
                ),
                new DeductionEngine(new StrictAccusationEvaluationStrategy())
        );

        assertFalse(result.isSolved());
        assertTrue(result.isCorrectPrimaryEvidence());
        assertFalse(result.isCorrectPrimaryContradiction());
        assertTrue(result.isCorrectTimelineEvent());
        assertEquals(75, result.getScore());
    }

    @Test
    void wrongTimelineEventKeepsTheAccusationIncomplete() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
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

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation(
                        "sus-marta-greco",
                        "ev-server-log",
                        Contradiction.idFor(
                                "sus-marta-greco",
                                "q-marta-server-access",
                                "ev-server-log"
                        ),
                        "tl-badge-entry",
                        "The timeline anchor is wrong."
                ),
                new DeductionEngine(new StrictAccusationEvaluationStrategy())
        );

        assertFalse(result.isSolved());
        assertTrue(result.isCorrectPrimaryContradiction());
        assertFalse(result.isCorrectTimelineEvent());
        assertEquals(75, result.getScore());
    }
}
