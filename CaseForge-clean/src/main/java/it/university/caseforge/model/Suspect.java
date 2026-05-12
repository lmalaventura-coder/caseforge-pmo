package it.university.caseforge.model;

public class Suspect {

    private final String id;
    private final String name;
    private final String profile;
    private final String motive;
    private final String alibi;

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

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value;
    }
}
