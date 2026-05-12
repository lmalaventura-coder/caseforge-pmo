package it.university.caseforge.view;

import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.observer.InvestigationEvent;

public interface CaseView {

    void showCase(CaseFile caseFile);

    void showInvestigationEvent(InvestigationEvent event);

    void showEvaluationResult(EvaluationResult result);
}
