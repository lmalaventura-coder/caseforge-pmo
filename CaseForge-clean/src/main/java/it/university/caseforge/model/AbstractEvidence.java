package it.university.caseforge.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractEvidence implements Evidence {

    private final String id;
    private final String title;
    private final String description;
    private final Set<String> linkedSuspectIds = new LinkedHashSet<>();

    private boolean discovered;

    protected AbstractEvidence(String id, String title, String description) {
        this.id = requireText(id, "id");
        this.title = requireText(title, "title");
        this.description = requireText(description, "description");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isDiscovered() {
        return discovered;
    }

    @Override
    public void markDiscovered() {
        discovered = true;
    }

    @Override
    public void linkToSuspect(Suspect suspect) {
        linkedSuspectIds.add(Objects.requireNonNull(suspect).getId());
    }

    @Override
    public boolean isLinkedTo(String suspectId) {
        return linkedSuspectIds.contains(suspectId);
    }

    @Override
    public Set<String> getLinkedSuspectIds() {
        return Collections.unmodifiableSet(linkedSuspectIds);
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value;
    }
}
