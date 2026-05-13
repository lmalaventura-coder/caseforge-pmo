package it.university.caseforge.controller;

import it.university.caseforge.factory.DemoCaseFactory;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.DeductionEngine;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.model.StrictAccusationEvaluationStrategy;
import it.university.caseforge.observer.InvestigationEvent;
import it.university.caseforge.observer.InvestigationEventType;
import it.university.caseforge.persistence.InMemoryCaseRepository;
import it.university.caseforge.view.CaseView;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvestigationControllerTest {

    @Test
    void discoverEvidenceRefreshesTheDisplayedCase() {
        RecordingCaseView view = new RecordingCaseView();
        InvestigationController controller = createController(view);
        controller.loadDemoCase();

        controller.discoverEvidence("ev-fingerprint");

        assertEquals(2, view.showCaseCalls);
        assertNotNull(view.lastCaseFile);
        assertTrue(view.lastCaseFile.findEvidenceById("ev-fingerprint").orElseThrow().isDiscovered());
    }

    @Test
    void structuredAccusationUsesEvidenceContradictionAndTimelineSelectedByTheController() {
        RecordingCaseView view = new RecordingCaseView();
        InvestigationController controller = createController(view);
        controller.loadDemoCase();
        controller.discoverEvidence("ev-server-log");
        controller.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        controller.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );

        EvaluationResult result = controller.submitAccusation(
                "sus-marta-greco",
                "ev-server-log",
                it.university.caseforge.model.Contradiction.idFor(
                        "sus-marta-greco",
                        "q-marta-server-access",
                        "ev-server-log"
                ),
                "tl-server-export",
                "Prova, contraddizione e timeline convergono."
        );

        assertTrue(result.isSolved());
        assertEquals(100, result.getScore());
        assertEquals(result, view.lastEvaluationResult);
    }

    @Test
    void controllerLinksDiscoveredEvidenceToSelectedAnswer() {
        RecordingCaseView view = new RecordingCaseView();
        InvestigationController controller = createController(view);
        controller.loadDemoCase();
        controller.discoverEvidence("ev-server-log");
        controller.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );

        controller.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );

        assertEquals(4, view.showCaseCalls);
        assertTrue(view.lastCaseFile.findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getReliabilityScore() < 100);
    }

    @Test
    void resetDemoInvestigationRestoresInitialCaseStateAfterClosure() {
        RecordingCaseView view = new RecordingCaseView();
        InvestigationController controller = createController(view);
        controller.loadDemoCase();
        controller.discoverEvidence("ev-server-log");
        controller.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        controller.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        CaseFile beforeReset = view.lastCaseFile;

        controller.submitAccusation(
                "sus-marta-greco",
                "ev-server-log",
                it.university.caseforge.model.Contradiction.idFor(
                        "sus-marta-greco",
                        "q-marta-server-access",
                        "ev-server-log"
                ),
                "tl-server-export",
                "La ricostruzione e coerente."
        );

        controller.resetDemoInvestigation();

        CaseFile resetCase = view.lastCaseFile;
        assertEquals(1, view.resetCalls);
        assertNotSame(beforeReset, resetCase);
        assertFalse(resetCase.findEvidenceById("ev-server-log").orElseThrow().isDiscovered());
        assertEquals(100, resetCase.findSuspectById("sus-marta-greco").orElseThrow().getReliabilityScore());
        assertEquals(0, resetCase.findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getInterrogations()
                .get(0)
                .getContradictions()
                .size());
        assertFalse(resetCase.findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getInterrogations()
                .get(0)
                .findQuestionById("q-marta-server-access")
                .orElseThrow()
                .isAnswerObtained());
        assertEquals(8, resetCase.getTimeline().getEvents().size());
        assertNull(view.lastEvaluationResult);
    }

    @Test
    void resetDemoInvestigationKeepsObserverNotificationsSingleAfterMultipleRestarts() {
        RecordingCaseView view = new RecordingCaseView();
        InvestigationController controller = createController(view);
        controller.loadDemoCase();
        controller.resetDemoInvestigation();
        controller.resetDemoInvestigation();
        view.events.clear();

        controller.discoverEvidence("ev-fingerprint");

        long discoveryEvents = view.events.stream()
                .filter(event -> event.getType() == InvestigationEventType.EVIDENCE_DISCOVERED)
                .count();
        assertEquals(1, discoveryEvents);
        assertTrue(view.lastCaseFile.findEvidenceById("ev-fingerprint").orElseThrow().isDiscovered());
    }

    @Test
    void askingQuestionRefreshesTheDisplayedCaseAndRecordsTheAnswer() {
        RecordingCaseView view = new RecordingCaseView();
        InvestigationController controller = createController(view);
        controller.loadDemoCase();

        controller.askQuestion(
                "sus-sofia-rinaldi",
                "int-sofia-001",
                "q-sofia-release-bridge"
        );

        assertEquals(2, view.showCaseCalls);
        assertTrue(view.lastCaseFile.findSuspectById("sus-sofia-rinaldi")
                .orElseThrow()
                .getInterrogations()
                .get(0)
                .findQuestionById("q-sofia-release-bridge")
                .orElseThrow()
                .isAnswerObtained());
    }

    private InvestigationController createController(RecordingCaseView view) {
        return new InvestigationController(
                new DemoCaseFactory(),
                new InMemoryCaseRepository(),
                new DeductionEngine(new StrictAccusationEvaluationStrategy()),
                view
        );
    }

    private static final class RecordingCaseView implements CaseView {

        private int showCaseCalls;
        private int resetCalls;
        private CaseFile lastCaseFile;
        private EvaluationResult lastEvaluationResult;
        private final List<InvestigationEvent> events = new ArrayList<>();

        @Override
        public void showCase(CaseFile caseFile) {
            showCaseCalls++;
            lastCaseFile = caseFile;
        }

        @Override
        public void resetInvestigation(CaseFile caseFile) {
            resetCalls++;
            lastCaseFile = caseFile;
            lastEvaluationResult = null;
        }

        @Override
        public void showInvestigationEvent(InvestigationEvent event) {
            events.add(event);
        }

        @Override
        public void showEvaluationResult(EvaluationResult result) {
            lastEvaluationResult = result;
        }
    }
}
