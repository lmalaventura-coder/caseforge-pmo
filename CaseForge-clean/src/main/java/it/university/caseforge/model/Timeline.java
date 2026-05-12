package it.university.caseforge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Timeline {

    private final List<TimelineEvent> events = new ArrayList<>();

    public void addEvent(TimelineEvent event) {
        events.add(Objects.requireNonNull(event));
        events.sort(Comparator.comparing(TimelineEvent::getOccurredAt));
    }

    public List<TimelineEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public Optional<TimelineEvent> findEventById(String eventId) {
        return events.stream()
                .filter(event -> event.getId().equals(eventId))
                .findFirst();
    }
}
