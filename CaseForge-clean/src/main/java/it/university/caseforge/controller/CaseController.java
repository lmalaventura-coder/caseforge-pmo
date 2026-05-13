package it.university.caseforge.controller;

import it.university.caseforge.model.CaseSummary;
import it.university.caseforge.model.EvaluationResult;

import java.nio.file.Path;
import java.util.List;

public interface CaseController {

    List<CaseSummary> getAvailableCases();

    void loadDefaultCase();

    void loadCase(String caseId);

    void resetCurrentInvestigation();

    @Deprecated
    default void loadDemoCase() {
        loadDefaultCase();
    }

    @Deprecated
    default void resetDemoInvestigation() {
        resetCurrentInvestigation();
    }

    void saveInvestigation(Path path, String selectedSuspectId);

    void loadInvestigation(Path path);

    void discoverEvidence(String evidenceId);

    void askQuestion(
            String suspectId,
            String interrogationId,
            String questionId
    );

    void linkEvidenceToSuspect(String evidenceId, String suspectId);

    void linkEvidenceToAnswer(
            String evidenceId,
            String suspectId,
            String interrogationId,
            String questionId
    );

    EvaluationResult submitAccusation(
            String suspectId,
            String primaryEvidenceId,
            String primaryContradictionId,
            String relevantTimelineEventId,
            String reasoning
    );
}
