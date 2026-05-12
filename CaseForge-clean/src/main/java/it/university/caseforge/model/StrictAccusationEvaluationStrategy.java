package it.university.caseforge.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class StrictAccusationEvaluationStrategy implements AccusationEvaluationStrategy {

    @Override
    public EvaluationResult evaluate(CaseFile caseFile, Accusation accusation) {
        CaseSolution solution = caseFile.getSolution()
                .orElseThrow(() -> new IllegalStateException("Case has no solution configured."));

        boolean correctSuspect = solution.getCulpritSuspectId().equals(accusation.getSuspectId());
        Set<String> countedEvidence = discoveredSubmittedEvidence(caseFile, accusation);

        Set<String> missingEvidence = new LinkedHashSet<>(solution.getRequiredEvidenceIds());
        missingEvidence.removeAll(countedEvidence);

        int score = calculateScore(correctSuspect, solution.getRequiredEvidenceIds(), missingEvidence);
        boolean solved = correctSuspect && missingEvidence.isEmpty();
        String message = solved
                ? "Accusation accepted. The case is solved."
                : "Accusation rejected or incomplete.";

        return new EvaluationResult(solved, correctSuspect, score, message, missingEvidence);
    }

    private Set<String> discoveredSubmittedEvidence(CaseFile caseFile, Accusation accusation) {
        Set<String> discoveredSubmitted = new LinkedHashSet<>();
        for (String evidenceId : accusation.getEvidenceIds()) {
            caseFile.findEvidenceById(evidenceId)
                    .filter(Evidence::isDiscovered)
                    .map(Evidence::getId)
                    .ifPresent(discoveredSubmitted::add);
        }
        return discoveredSubmitted;
    }

    private int calculateScore(boolean correctSuspect, Set<String> requiredEvidenceIds, Set<String> missingEvidence) {
        int suspectScore = correctSuspect ? 60 : 0;
        if (requiredEvidenceIds.isEmpty()) {
            return suspectScore;
        }

        int foundEvidence = requiredEvidenceIds.size() - missingEvidence.size();
        int evidenceScore = Math.round((foundEvidence * 40.0f) / requiredEvidenceIds.size());
        return suspectScore + evidenceScore;
    }
}
