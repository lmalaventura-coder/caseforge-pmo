package it.university.caseforge.persistence;

import it.university.caseforge.model.CaseFile;

import java.util.Optional;

public interface CaseRepository {

    void save(CaseFile caseFile);

    Optional<CaseFile> findById(String id);
}
