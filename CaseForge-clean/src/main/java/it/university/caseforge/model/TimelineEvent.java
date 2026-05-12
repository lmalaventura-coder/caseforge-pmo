package it.university.caseforge.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class TimelineEvent {

    private final String id;
    private final LocalDateTime occurredAt;
    private final String title;
    private final String description;
    private final String relatedSuspectId;

    public TimelineEvent(
            String id,
            LocalDateTime occurredAt,
            String title,
            String description,
            String relatedSuspectId
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank.");
        }
        this.id = id;
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.title = requireText(title, "title");
        this.description = description == null ? "" : description;
        this.relatedSuspectId = relatedSuspectId;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Optional<String> getRelatedSuspectId() {
        return Optional.ofNullable(relatedSuspectId);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value;
    }
}
