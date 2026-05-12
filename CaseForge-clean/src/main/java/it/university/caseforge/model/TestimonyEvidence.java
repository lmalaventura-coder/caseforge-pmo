package it.university.caseforge.model;

public class TestimonyEvidence extends AbstractEvidence {

    private final String witnessName;
    private final String statement;

    public TestimonyEvidence(String id, String title, String description, String witnessName, String statement) {
        super(id, title, description);
        this.witnessName = requireText(witnessName, "witnessName");
        this.statement = requireText(statement, "statement");
    }

    @Override
    public EvidenceType getType() {
        return EvidenceType.TESTIMONY;
    }

    public String getWitnessName() {
        return witnessName;
    }

    public String getStatement() {
        return statement;
    }
}
