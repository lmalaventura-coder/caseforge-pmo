package it.university.caseforge.model;

public record CaseSummary(String id, String title) {

    public CaseSummary {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id non puo essere vuoto.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title non puo essere vuoto.");
        }
    }
}
