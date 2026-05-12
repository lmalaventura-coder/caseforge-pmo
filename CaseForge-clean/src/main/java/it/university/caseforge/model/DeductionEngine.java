package it.university.caseforge.model;

import java.util.Objects;

public class DeductionEngine {

    private AccusationEvaluationStrategy strategy;

    public DeductionEngine(AccusationEvaluationStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
    }

    public EvaluationResult evaluate(CaseFile caseFile, Accusation accusation) {
        return strategy.evaluate(Objects.requireNonNull(caseFile), Objects.requireNonNull(accusation));
    }

    public void setStrategy(AccusationEvaluationStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
    }
}
