package it.university.caseforge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class CaseFile {

    private final String id;
    private final String title;
    private final String description;
    private final List<Suspect> suspects;
    private final List<Evidence> evidences;
    private final List<Interrogation> interrogations;
    private final Timeline timeline;
    private final CaseSolution solution;

    private CaseFile(Builder builder) {
        this.id = requireText(builder.id, "id");
        this.title = requireText(builder.title, "title");
        this.description = builder.description == null ? "" : builder.description;
        this.suspects = new ArrayList<>(builder.suspects);
        this.evidences = new ArrayList<>(builder.evidences);
        this.interrogations = new ArrayList<>(builder.interrogations);
        this.timeline = builder.timeline;
        this.solution = builder.solution;
    }

    public static Builder builder(String id, String title) {
        return new Builder(id, title);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Suspect> getSuspects() {
        return Collections.unmodifiableList(suspects);
    }

    public List<Evidence> getEvidences() {
        return Collections.unmodifiableList(evidences);
    }

    public List<Interrogation> getInterrogations() {
        return Collections.unmodifiableList(interrogations);
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public Optional<CaseSolution> getSolution() {
        return Optional.ofNullable(solution);
    }

    public void addSuspect(Suspect suspect) {
        Objects.requireNonNull(suspect);
        ensureUniqueSuspect(suspect.getId());
        suspects.add(suspect);
    }

    public void addEvidence(Evidence evidence) {
        Objects.requireNonNull(evidence);
        ensureUniqueEvidence(evidence.getId());
        evidences.add(evidence);
    }

    public void addTimelineEvent(TimelineEvent event) {
        timeline.addEvent(event);
    }

    public Optional<Suspect> findSuspectById(String suspectId) {
        return suspects.stream()
                .filter(suspect -> suspect.getId().equals(suspectId))
                .findFirst();
    }

    public Optional<Evidence> findEvidenceById(String evidenceId) {
        return evidences.stream()
                .filter(evidence -> evidence.getId().equals(evidenceId))
                .findFirst();
    }

    private void ensureUniqueSuspect(String suspectId) {
        if (findSuspectById(suspectId).isPresent()) {
            throw new IllegalArgumentException("Duplicate suspect id: " + suspectId);
        }
    }

    private void ensureUniqueEvidence(String evidenceId) {
        if (findEvidenceById(evidenceId).isPresent()) {
            throw new IllegalArgumentException("Duplicate evidence id: " + evidenceId);
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value;
    }

    public static final class Builder {

        private final String id;
        private final String title;
        private final List<Suspect> suspects = new ArrayList<>();
        private final List<Evidence> evidences = new ArrayList<>();
        private final List<Interrogation> interrogations = new ArrayList<>();
        private String description;
        private Timeline timeline = new Timeline();
        private CaseSolution solution;

        private Builder(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder addSuspect(Suspect suspect) {
            suspects.add(Objects.requireNonNull(suspect));
            return this;
        }

        public Builder addEvidence(Evidence evidence) {
            evidences.add(Objects.requireNonNull(evidence));
            return this;
        }

        public Builder timeline(Timeline timeline) {
            this.timeline = Objects.requireNonNull(timeline);
            return this;
        }

        public Builder addTimelineEvent(TimelineEvent event) {
            timeline.addEvent(event);
            return this;
        }

        public Builder addInterrogation(Interrogation interrogation) {
            interrogations.add(Objects.requireNonNull(interrogation));
            return this;
        }

        public Builder solution(CaseSolution solution) {
            this.solution = Objects.requireNonNull(solution);
            return this;
        }

        public CaseFile build() {
            validateUniqueIds();
            return new CaseFile(this);
        }

        private void validateUniqueIds() {
            Set<String> suspectIds = new HashSet<>();
            for (Suspect suspect : suspects) {
                if (!suspectIds.add(suspect.getId())) {
                    throw new IllegalArgumentException("Duplicate suspect id: " + suspect.getId());
                }
            }

            Set<String> evidenceIds = new HashSet<>();
            for (Evidence evidence : evidences) {
                if (!evidenceIds.add(evidence.getId())) {
                    throw new IllegalArgumentException("Duplicate evidence id: " + evidence.getId());
                }
            }
        }
    }
}
