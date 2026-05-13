package it.university.caseforge.persistence;

import it.university.caseforge.model.Investigation;

import java.io.IOException;
import java.nio.file.Path;

public interface InvestigationRepository {

    void save(Investigation investigation, String selectedSuspectId, Path path) throws IOException;

    LoadedInvestigation load(Path path) throws IOException;
}
