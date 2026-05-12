package it.university.caseforge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Suspect {

    private final String id;
    private final String name;
    private final String profile;
    private final String motive;
    private final String alibi;
    private final List<Interrogation> interrogations = new ArrayList<>();

    private int reliabilityScore = 100;

    public Suspect(String id, String name, String profile, String motive, String alibi) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.profile = profile == null ? "" : profile;
        this.motive = motive == null ? "" : motive;
        this.alibi = alibi == null ? "" : alibi;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    public String getMotive() {
        return motive;
    }

    public String getAlibi() {
        return alibi;
    }

    public List<Interrogation> getInterrogations() {
        return Collections.unmodifiableList(interrogations);
    }

    public void addInterrogation(Interrogation interrogation) {
        Interrogation nonNullInterrogation = Objects.requireNonNull(interrogation);
        if (!id.equals(nonNullInterrogation.getSuspectId())) {
            throw new IllegalArgumentException("Interrogation belongs to a different suspect.");
        }
        interrogations.add(nonNullInterrogation);
    }

    public int getReliabilityScore() {
        return reliabilityScore;
    }

    public void decreaseReliability(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative.");
        }
        reliabilityScore = Math.max(0, reliabilityScore - amount);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value;
    }
}
