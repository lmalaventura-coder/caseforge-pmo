package it.university.caseforge.model;

import it.university.caseforge.factory.DemoCaseFactory;
import it.university.caseforge.observer.InvestigationEvent;
import it.university.caseforge.observer.InvestigationEventType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccusationEvaluationTest {

    @Test
    void correctAccusationSolvesAndClosesCase() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);
        investigation.discoverEvidence("ev-fingerprint");
        investigation.discoverEvidence("ev-server-log");

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation("sus-marta-greco", Set.of("ev-fingerprint", "ev-server-log"), "Evidence matches."),
                new DeductionEngine(new StrictAccusationEvaluationStrategy())
        );

        assertTrue(result.isSolved());
        assertEquals(100, result.getScore());
        assertEquals(InvestigationStatus.CLOSED, investigation.getStatus());
        assertEquals(InvestigationEventType.CASE_CLOSED, events.get(events.size() - 1).getType());
    }

    @Test
    void wrongSuspectDoesNotSolveCase() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
        investigation.discoverEvidence("ev-fingerprint");
        investigation.discoverEvidence("ev-server-log");

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation("sus-luca-conti", Set.of("ev-fingerprint", "ev-server-log"), "Looks suspicious."),
                new DeductionEngine(new StrictAccusationEvaluationStrategy())
        );

        assertFalse(result.isSolved());
        assertFalse(result.isCorrectSuspect());
        assertEquals(40, result.getScore());
    }

    @Test
    void undiscoveredEvidenceDoesNotCountForFinalAccusation() {
        Investigation investigation = new Investigation(new DemoCaseFactory().createDemoCase());
        investigation.discoverEvidence("ev-fingerprint");

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation("sus-marta-greco", Set.of("ev-fingerprint", "ev-server-log"), "One clue is missing."),
                new DeductionEngine(new StrictAccusationEvaluationStrategy())
        );

        assertFalse(result.isSolved());
        assertTrue(result.isCorrectSuspect());
        assertEquals(Set.of("ev-server-log"), result.getMissingEvidenceIds());
        assertEquals(80, result.getScore());
    }
}
