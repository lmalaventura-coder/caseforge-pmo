package it.university.caseforge.persistence;

import it.university.caseforge.factory.DemoCaseFactory;
import it.university.caseforge.model.Accusation;
import it.university.caseforge.model.Contradiction;
import it.university.caseforge.model.DeductionEngine;
import it.university.caseforge.model.Investigation;
import it.university.caseforge.model.InvestigationStatus;
import it.university.caseforge.model.Question;
import it.university.caseforge.model.StrictAccusationEvaluationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonInvestigationRepositoryTest {

    @TempDir
    private Path tempDir;

    private final DemoCaseFactory caseFactory = new DemoCaseFactory();
    private final JsonInvestigationRepository repository = new JsonInvestigationRepository(caseFactory);

    @Test
    void savesANewInvestigationAsJson() throws Exception {
        Investigation investigation = new Investigation(caseFactory.createDefaultCase());
        Path saveFile = tempDir.resolve("new-investigation.json");

        repository.save(investigation, "sus-marta-greco", saveFile);

        assertTrue(Files.exists(saveFile));
        String json = Files.readString(saveFile);
        assertTrue(json.contains("\"caseId\""));
        assertTrue(json.contains("case-001"));
        assertTrue(json.contains("sus-marta-greco"));
    }

    @Test
    void loadsASavedInvestigation() throws Exception {
        Investigation investigation = new Investigation(caseFactory.createDefaultCase());
        Path saveFile = tempDir.resolve("load-investigation.json");
        repository.save(investigation, "sus-luca-conti", saveFile);

        LoadedInvestigation loaded = repository.load(saveFile);

        assertEquals("case-001", loaded.investigation().getCaseFile().getId());
        assertEquals("sus-luca-conti", loaded.selectedSuspectId());
        assertEquals(InvestigationStatus.OPEN, loaded.investigation().getStatus());
    }

    @Test
    void persistsDiscoveredEvidence() throws Exception {
        Investigation investigation = new Investigation(caseFactory.createDefaultCase());
        investigation.discoverEvidence("ev-server-log");
        Path saveFile = tempDir.resolve("discovered-evidence.json");

        repository.save(investigation, null, saveFile);
        LoadedInvestigation loaded = repository.load(saveFile);

        assertTrue(loaded.investigation()
                .getCaseFile()
                .findEvidenceById("ev-server-log")
                .orElseThrow()
                .isDiscovered());
        assertFalse(loaded.investigation()
                .getCaseFile()
                .findEvidenceById("ev-fingerprint")
                .orElseThrow()
                .isDiscovered());
    }

    @Test
    void persistsObtainedAnswersLinkedEvidenceAndContradictions() throws Exception {
        Investigation investigation = new Investigation(caseFactory.createDefaultCase());
        investigation.discoverEvidence("ev-server-log");
        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        investigation.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        Path saveFile = tempDir.resolve("interrogation-state.json");

        repository.save(investigation, "sus-marta-greco", saveFile);
        LoadedInvestigation loaded = repository.load(saveFile);
        Question question = loaded.investigation()
                .getCaseFile()
                .findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getInterrogations()
                .get(0)
                .findQuestionById("q-marta-server-access")
                .orElseThrow();

        assertTrue(question.isAnswerObtained());
        assertTrue(question.getAnswer().isLinkedToEvidence("ev-server-log"));
        assertEquals(1, loaded.investigation()
                .getCaseFile()
                .findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getInterrogations()
                .get(0)
                .getContradictions()
                .size());
        assertEquals(70, loaded.investigation()
                .getCaseFile()
                .findSuspectById("sus-marta-greco")
                .orElseThrow()
                .getReliabilityScore());
    }

    @Test
    void persistsFinalAccusationAndClosedStatus() throws Exception {
        Investigation investigation = new Investigation(caseFactory.createDefaultCase());
        investigation.discoverEvidence("ev-server-log");
        investigation.askQuestion(
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        investigation.linkEvidenceToAnswer(
                "ev-server-log",
                "sus-marta-greco",
                "int-marta-001",
                "q-marta-server-access"
        );
        investigation.formulateAccusation(
                new Accusation(
                        "sus-marta-greco",
                        "ev-server-log",
                        Contradiction.idFor(
                                "sus-marta-greco",
                                "q-marta-server-access",
                                "ev-server-log"
                        ),
                        "tl-server-export",
                        "Dossier completo."
                ),
                new DeductionEngine(new StrictAccusationEvaluationStrategy())
        );
        Path saveFile = tempDir.resolve("closed-investigation.json");

        repository.save(investigation, "sus-marta-greco", saveFile);
        LoadedInvestigation loaded = repository.load(saveFile);

        assertEquals(InvestigationStatus.CLOSED, loaded.investigation().getStatus());
        assertNotNull(loaded.investigation().getLastAccusation());
        assertTrue(loaded.investigation().getLastEvaluationResult().isSolved());
    }
}
