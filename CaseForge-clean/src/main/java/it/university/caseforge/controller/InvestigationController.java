package it.university.caseforge.controller;

import it.university.caseforge.factory.CaseFactory;
import it.university.caseforge.model.Accusation;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.CaseSummary;
import it.university.caseforge.model.DeductionEngine;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.model.Investigation;
import it.university.caseforge.observer.InvestigationObserver;
import it.university.caseforge.persistence.CaseRepository;
import it.university.caseforge.persistence.InvestigationRepository;
import it.university.caseforge.persistence.JsonInvestigationRepository;
import it.university.caseforge.persistence.LoadedInvestigation;
import it.university.caseforge.view.CaseView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class InvestigationController implements CaseController {

    private final CaseFactory caseFactory;
    private final CaseRepository caseRepository;
    private final InvestigationRepository investigationRepository;
    private final DeductionEngine deductionEngine;
    private final CaseView caseView;
    private final InvestigationObserver caseViewObserver;

    private Investigation investigation;
    private String currentCaseId;

    public InvestigationController(
            CaseFactory caseFactory,
            CaseRepository caseRepository,
            DeductionEngine deductionEngine,
            CaseView caseView
    ) {
        this(
                caseFactory,
                caseRepository,
                new JsonInvestigationRepository(caseFactory),
                deductionEngine,
                caseView
        );
    }

    public InvestigationController(
            CaseFactory caseFactory,
            CaseRepository caseRepository,
            InvestigationRepository investigationRepository,
            DeductionEngine deductionEngine,
            CaseView caseView
    ) {
        this.caseFactory = Objects.requireNonNull(caseFactory);
        this.caseRepository = Objects.requireNonNull(caseRepository);
        this.investigationRepository = Objects.requireNonNull(investigationRepository);
        this.deductionEngine = Objects.requireNonNull(deductionEngine);
        this.caseView = Objects.requireNonNull(caseView);
        this.caseViewObserver = caseView::showInvestigationEvent;
    }

    @Override
    public List<CaseSummary> getAvailableCases() {
        return caseFactory.availableCases();
    }

    @Override
    public void loadDemoCase() {
        startInvestigation(defaultCaseId(), false);
    }

    @Override
    public void loadCase(String caseId) {
        startInvestigation(caseId, investigation != null);
    }

    @Override
    public void resetDemoInvestigation() {
        resetCurrentInvestigation();
    }

    @Override
    public void resetCurrentInvestigation() {
        startInvestigation(currentCaseId == null ? defaultCaseId() : currentCaseId, true);
    }

    @Override
    public void saveInvestigation(Path path, String selectedSuspectId) {
        try {
            investigationRepository.save(currentInvestigation(), selectedSuspectId, path);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Impossibile salvare l'indagine: " + exception.getMessage(),
                    exception
            );
        }
    }

    @Override
    public void loadInvestigation(Path path) {
        LoadedInvestigation loadedInvestigation;
        try {
            loadedInvestigation = investigationRepository.load(path);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                    "Impossibile caricare l'indagine: " + exception.getMessage(),
                    exception
            );
        }

        replaceInvestigation(loadedInvestigation.investigation());
        caseView.showLoadedInvestigation(
                investigation.getCaseFile(),
                loadedInvestigation.selectedSuspectId()
        );
        if (investigation.getLastEvaluationResult() != null) {
            caseView.showEvaluationResult(investigation.getLastEvaluationResult());
        }
    }

    private void startInvestigation(String caseId, boolean resetView) {
        CaseFile caseFile = caseFactory.createCase(caseId);

        replaceInvestigation(new Investigation(caseFile));

        if (resetView) {
            caseView.resetInvestigation(caseFile);
        } else {
            caseView.showCase(caseFile);
        }
    }

    private void replaceInvestigation(Investigation newInvestigation) {
        if (investigation != null) {
            investigation.removeObserver(caseViewObserver);
        }

        investigation = Objects.requireNonNull(newInvestigation);
        investigation.addObserver(caseViewObserver);
        currentCaseId = investigation.getCaseFile().getId();
        caseRepository.save(investigation.getCaseFile());
    }

    private String defaultCaseId() {
        return caseFactory.availableCases().stream()
                .findFirst()
                .map(CaseSummary::id)
                .orElseThrow(() -> new IllegalStateException("Nessun caso disponibile."));
    }

    @Override
    public void discoverEvidence(String evidenceId) {
        Investigation currentInvestigation = currentInvestigation();
        currentInvestigation.discoverEvidence(evidenceId);
        caseView.showCase(currentInvestigation.getCaseFile());
    }

    @Override
    public void askQuestion(
            String suspectId,
            String interrogationId,
            String questionId
    ) {
        Investigation currentInvestigation = currentInvestigation();
        currentInvestigation.askQuestion(suspectId, interrogationId, questionId);
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
            throw new IllegalStateException("Nessuna indagine caricata.");
        }
        return investigation;
    }
}
