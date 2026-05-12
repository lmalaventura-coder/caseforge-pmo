package it.university.caseforge.controller;

import it.university.caseforge.factory.CaseFactory;
import it.university.caseforge.model.Accusation;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.DeductionEngine;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.model.Investigation;
import it.university.caseforge.persistence.CaseRepository;
import it.university.caseforge.view.CaseView;

import java.util.Objects;

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
    public void linkEvidenceToAnswer(
            String evidenceId,
            String suspectId,
            String interrogationId,
            String questionId
    ) {
        Investigation currentInvestigation = currentInvestigation();
        currentInvestigation.linkEvidenceToAnswer(evidenceId, suspectId, interrogationId, questionId);
        caseView.showCase(currentInvestigation.getCaseFile());
    }

    @Override
    public EvaluationResult submitAccusation(
            String suspectId,
            String primaryEvidenceId,
            String primaryContradictionId,
            String relevantTimelineEventId,
            String reasoning
    ) {
        Accusation accusation = new Accusation(
                suspectId,
                primaryEvidenceId,
                primaryContradictionId,
                relevantTimelineEventId,
                reasoning
        );
        EvaluationResult result = currentInvestigation().formulateAccusation(accusation, deductionEngine);
        caseView.showEvaluationResult(result);
        return result;
    }

    private Investigation currentInvestigation() {
        if (investigation == null) {
            throw new IllegalStateException("No investigation loaded.");
        }
        return investigation;
    }
}
