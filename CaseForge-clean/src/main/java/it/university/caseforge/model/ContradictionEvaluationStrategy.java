package it.university.caseforge.model;

import java.util.Optional;

public interface ContradictionEvaluationStrategy {

    Optional<Contradiction> evaluate(Suspect suspect, Question question, Evidence evidence);
}
