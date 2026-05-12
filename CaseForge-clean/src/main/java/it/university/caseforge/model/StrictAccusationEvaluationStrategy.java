package it.university.caseforge.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StrictAccusationEvaluationStrategy implements AccusationEvaluationStrategy {

    @Override
    public EvaluationResult evaluate(CaseFile caseFile, Accusation accusation) {
        CaseSolution solution = caseFile.getSolution()
                .orElseThrow(() -> new IllegalStateException("Il caso non ha una soluzione configurata."));

        boolean correctSuspect = solution.getCulpritSuspectId().equals(accusation.getSuspectId());
        boolean correctPrimaryEvidence = isCorrectPrimaryEvidence(caseFile, accusation, solution);
        boolean correctPrimaryContradiction = isCorrectPrimaryContradiction(caseFile, accusation, solution);
        boolean correctTimelineEvent = isCorrectTimelineEvent(caseFile, accusation, solution);

        Set<String> missingEvidence = correctPrimaryEvidence
                ? Set.of()
                : Set.of(solution.getPrimaryEvidenceId());

        int score = calculateScore(
                correctSuspect,
                correctPrimaryEvidence,
                correctPrimaryContradiction,
                correctTimelineEvent
        );
        boolean solved = correctSuspect
                && correctPrimaryEvidence
                && correctPrimaryContradiction
                && correctTimelineEvent;
        String message = buildMessage(
                solved,
                correctSuspect,
                correctPrimaryEvidence,
                correctPrimaryContradiction,
                correctTimelineEvent
        );

        return new EvaluationResult(
                solved,
                correctSuspect,
                correctPrimaryEvidence,
                correctPrimaryContradiction,
                correctTimelineEvent,
                score,
                message,
                missingEvidence
        );
    }

    private boolean isCorrectPrimaryEvidence(
            CaseFile caseFile,
            Accusation accusation,
            CaseSolution solution
    ) {
        return solution.getPrimaryEvidenceId().equals(accusation.getPrimaryEvidenceId())
                && caseFile.findEvidenceById(accusation.getPrimaryEvidenceId())
                .filter(Evidence::isDiscovered)
                .isPresent();
    }

    private boolean isCorrectPrimaryContradiction(
            CaseFile caseFile,
            Accusation accusation,
            CaseSolution solution
    ) {
        if (!solution.getPrimaryContradictionId().equals(accusation.getPrimaryContradictionId())) {
            return false;
        }

        return caseFile.findContradictionById(accusation.getPrimaryContradictionId())
                .filter(contradiction -> contradiction.getSuspectId().equals(accusation.getSuspectId()))
                .filter(contradiction -> contradiction.getEvidence().getId().equals(accusation.getPrimaryEvidenceId()))
                .isPresent();
    }

    private boolean isCorrectTimelineEvent(
            CaseFile caseFile,
            Accusation accusation,
            CaseSolution solution
    ) {
        return solution.getRelevantTimelineEventId().equals(accusation.getRelevantTimelineEventId())
                && caseFile.findTimelineEventById(accusation.getRelevantTimelineEventId()).isPresent();
    }

    private int calculateScore(
            boolean correctSuspect,
            boolean correctPrimaryEvidence,
            boolean correctPrimaryContradiction,
            boolean correctTimelineEvent
    ) {
        int score = 0;
        if (correctSuspect) {
            score += 25;
        }
        if (correctPrimaryEvidence) {
            score += 25;
        }
        if (correctPrimaryContradiction) {
            score += 25;
        }
        if (correctTimelineEvent) {
            score += 25;
        }
        return score;
    }

    private String buildMessage(
            boolean solved,
            boolean correctSuspect,
            boolean correctPrimaryEvidence,
            boolean correctPrimaryContradiction,
            boolean correctTimelineEvent
    ) {
        if (solved) {
            return "Accusa accolta. Sospetto, prova, contraddizione ed evento della cronologia risultano coerenti.";
        }

        List<String> missingElements = new ArrayList<>();
        if (!correctSuspect) {
            missingElements.add("sospetto");
        }
        if (!correctPrimaryEvidence) {
            missingElements.add("prova principale");
        }
        if (!correctPrimaryContradiction) {
            missingElements.add("contraddizione confermata");
        }
        if (!correctTimelineEvent) {
            missingElements.add("evento della cronologia");
        }
        return "Accusa respinta o incompleta: " + String.join(", ", missingElements) + ".";
    }
}
