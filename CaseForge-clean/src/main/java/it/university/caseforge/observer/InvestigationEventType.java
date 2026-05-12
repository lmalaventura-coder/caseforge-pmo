package it.university.caseforge.observer;

public enum InvestigationEventType {
    EVIDENCE_DISCOVERED,
    EVIDENCE_LINKED_TO_SUSPECT,
    EVIDENCE_LINKED_TO_ANSWER,
    CONTRADICTION_DETECTED,
    NO_CONTRADICTION_DETECTED,
    CASE_CLOSED
}
