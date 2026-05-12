package it.university.caseforge.model;

public interface AccusationEvaluationStrategy {

    EvaluationResult evaluate(CaseFile caseFile, Accusation accusation);
}
