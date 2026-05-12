package it.university.caseforge.controller;

import it.university.caseforge.model.EvaluationResult;

import java.util.Set;

public interface CaseController {

    void loadDemoCase();

    void discoverEvidence(String evidenceId);

    void linkEvidenceToSuspect(String evidenceId, String suspectId);

    EvaluationResult submitAccusation(String suspectId, Set<String> evidenceIds, String reasoning);

    EvaluationResult submitAccusationWithDiscoveredEvidence(String suspectId, String reasoning);
}
