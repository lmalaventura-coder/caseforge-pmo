package it.university.caseforge.controller;

import it.university.caseforge.factory.CaseFactory;
import it.university.caseforge.model.Accusation;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.DeductionEngine;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.model.Evidence;
import it.university.caseforge.model.Investigation;
import it.university.caseforge.persistence.CaseRepository;
import it.university.caseforge.view.CaseView;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class InvestigationController implements CaseController {

    private final CaseFactory caseFactory;
    private final CaseRepository caseRepository;
    private final DeductionEngine deductionEngine;
    private final CaseView caseView;

    private Investigation investigation;

    public InvestigationController(
            CaseFactory caseFactory,
            CaseRepository caseRepository,
            DeductionEngine deductionEngine,
            CaseView caseView
    ) {
        this.caseFactory = Objects.requireNonNull(caseFactory);
        this.caseRepository = Objects.requireNonNull(caseRepository);
        this.deductionEngine = Objects.requireNonNull(deductionEngine);
        this.caseView = Objects.requireNonNull(caseView);
    }

    @Override
    public void loadDemoCase() {
        CaseFile caseFile = caseFactory.createDemoCase();
        caseRepository.save(caseFile);
        investigation = new Investigation(caseFile);
        investigation.addObserver(caseView::showInvestigationEvent);
        caseView.showCase(caseFile);
    }

    @Override
    public void discoverEvidence(String evidenceId) {
        Investigation currentInvestigation = currentInvestigation();
        currentInvestigation.discoverEvidence(evidenceId);
        caseView.showCase(currentInvestigation.getCaseFile());
    }

    @Override
    public void linkEvidenceToSuspect(String evidenceId, String suspectId) {
        Investigation currentInvestigation = currentInvestigation();
        currentInvestigation.linkEvidenceToSuspect(evidenceId, suspectId);
        caseView.showCase(currentInvestigation.getCaseFile());
    }

    @Override
    public EvaluationResult submitAccusation(String suspectId, Set<String> evidenceIds, String reasoning) {
        Accusation accusation = new Accusation(suspectId, evidenceIds, reasoning);
        EvaluationResult result = currentInvestigation().formulateAccusation(accusation, deductionEngine);
        caseView.showEvaluationResult(result);
        return result;
    }

    @Override
    public EvaluationResult submitAccusationWithDiscoveredEvidence(String suspectId, String reasoning) {
        Investigation currentInvestigation = currentInvestigation();
        Set<String> discoveredEvidenceIds = currentInvestigation.getCaseFile().getEvidences().stream()
                .filter(Evidence::isDiscovered)
                .map(Evidence::getId)
                .collect(Collectors.toSet());

        return submitAccusation(suspectId, discoveredEvidenceIds, reasoning);
    }

    private Investigation currentInvestigation() {
        if (investigation == null) {
            throw new IllegalStateException("No investigation loaded.");
        }
        return investigation;
    }
}
