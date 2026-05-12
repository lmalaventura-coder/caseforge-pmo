package it.university.caseforge.persistence;

import it.university.caseforge.model.CaseFile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class InMemoryCaseRepository implements CaseRepository {

    private final Map<String, CaseFile> cases = new LinkedHashMap<>();

    @Override
    public void save(CaseFile caseFile) {
        cases.put(Objects.requireNonNull(caseFile).getId(), caseFile);
    }

    @Override
    public Optional<CaseFile> findById(String id) {
        return Optional.ofNullable(cases.get(id));
    }
}
