package it.university.caseforge.model;

import java.util.Optional;

public class LinkedEvidenceContradictionStrategy implements ContradictionEvaluationStrategy {

    @Override
    public Optional<Contradiction> evaluate(Suspect suspect, Question question, Evidence evidence) {
        Answer answer = question.getAnswer();
        if (answer == null || !evidence.isDiscovered()) {
            return Optional.empty();
        }

        return answer.getLinkedEvidence()
                .filter(linkedEvidence -> linkedEvidence.getId().equals(evidence.getId()))
                .map(linkedEvidence -> new Contradiction(
                        suspect.getId(),
                        question,
                        answer,
                        linkedEvidence,
                        "The discovered evidence conflicts with the recorded answer."
                ));
    }
}
