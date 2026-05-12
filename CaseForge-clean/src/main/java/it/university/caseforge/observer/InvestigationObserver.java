package it.university.caseforge.observer;

@FunctionalInterface
public interface InvestigationObserver {

    void onInvestigationEvent(InvestigationEvent event);
}
