package it.university.caseforge.controller;

import it.university.caseforge.factory.DemoCaseFactory;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.DeductionEngine;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.model.StrictAccusationEvaluationStrategy;
import it.university.caseforge.observer.InvestigationEvent;
import it.university.caseforge.persistence.InMemoryCaseRepository;
import it.university.caseforge.view.CaseView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        controller.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );

        assertEquals(3, view.showCaseCalls);
        assertTrue(view.lastCaseFile.findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getReliabilityScore() < 100);
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
        private CaseFile lastCaseFile;
        private EvaluationResult lastEvaluationResult;

        @Override
        public void showCase(CaseFile caseFile) {
            showCaseCalls++;
            lastCaseFile = caseFile;
        }

        @Override
        public void showInvestigationEvent(InvestigationEvent event) {
        }

        @Override
        public void showEvaluationResult(EvaluationResult result) {
            lastEvaluationResult = result;
        }
    }
}
