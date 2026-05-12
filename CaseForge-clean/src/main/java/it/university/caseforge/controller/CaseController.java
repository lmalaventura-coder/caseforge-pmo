package it.university.caseforge.controller;

import it.university.caseforge.model.EvaluationResult;

public interface CaseController {

    void loadDemoCase();

    void resetDemoInvestigation();

    void discoverEvidence(String evidenceId);

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
