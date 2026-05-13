package it.university.caseforge.factory;

import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.CaseSummary;

import java.util.List;

public interface CaseFactory {

    List<CaseSummary> availableCases();

    CaseFile createCase(String caseId);

    CaseFile createDemoCase();
}
