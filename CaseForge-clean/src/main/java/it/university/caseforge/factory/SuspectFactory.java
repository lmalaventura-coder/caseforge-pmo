package it.university.caseforge.factory;

import it.university.caseforge.model.Suspect;

public class SuspectFactory {

    public Suspect createSuspect(String id, String name, String profile, String motive, String alibi) {
        return new Suspect(id, name, profile, motive, alibi);
    }
}
