package it.university.caseforge.persistence;

import it.university.caseforge.model.Investigation;

public record LoadedInvestigation(
        Investigation investigation,
        String selectedSuspectId
) {
}
