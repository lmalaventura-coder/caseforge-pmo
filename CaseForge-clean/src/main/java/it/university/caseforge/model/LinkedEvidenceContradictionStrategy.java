package it.university.caseforge.model;

import java.util.Optional;

public class LinkedEvidenceContradictionStrategy implements ContradictionEvaluationStrategy {

    @Override
    public Optional<Contradiction> evaluate(Suspect suspect, Question question, Evidence evidence) {
        Answer answer = question.getAnswer();
        if (answer == null || !evidence.isDiscovered()) {
            return Optional.empty();
        }

        return answer.getContradictionEvidence()
                .filter(contradictionEvidence -> contradictionEvidence.getId().equals(evidence.getId()))
                .map(contradictionEvidence -> new Contradiction(
                        suspect.getId(),
                        question,
                        answer,
                        contradictionEvidence,
                        "La prova scoperta contraddice la risposta registrata."
                ));
    }
}
