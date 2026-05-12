package it.university.caseforge.model;

import java.util.Set;

public interface Evidence {

    String getId();

    String getTitle();

    String getDescription();

    EvidenceType getType();

    boolean isDiscovered();

    void markDiscovered();

    void linkToSuspect(Suspect suspect);

    boolean isLinkedTo(String suspectId);

    Set<String> getLinkedSuspectIds();
}
