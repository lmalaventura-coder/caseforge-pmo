package it.university.caseforge.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Interrogation {

    private final String id;
    private final String suspectId;
    private final LocalDateTime startedAt;
    private final List<Question> questions = new ArrayList<>();
    private final List<Contradiction> contradictions = new ArrayList<>();

    public Interrogation(String id, Suspect suspect, LocalDateTime startedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank.");
        }
        this.id = id;
        this.suspectId = Objects.requireNonNull(suspect).getId();
        this.startedAt = Objects.requireNonNull(startedAt);
    }

    public String getId() {
        return id;
    }

    public String getSuspectId() {
        return suspectId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public void addQuestion(Question question) {
        questions.add(Objects.requireNonNull(question));
    }

    public java.util.Optional<Question> findQuestionById(String questionId) {
        return questions.stream()
                .filter(question -> question.getId().equals(questionId))
                .findFirst();
    }

    public List<Contradiction> getContradictions() {
        return Collections.unmodifiableList(contradictions);
    }

    public boolean addContradiction(Contradiction contradiction) {
        Objects.requireNonNull(contradiction);
        boolean alreadyPresent = contradictions.stream()
                .anyMatch(existing -> existing.getEvidence().getId().equals(contradiction.getEvidence().getId())
                        && existing.getQuestion() == contradiction.getQuestion());
        if (alreadyPresent) {
            return false;
        }
        contradictions.add(contradiction);
        return true;
    }
}
