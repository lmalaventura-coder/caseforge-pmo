package it.university.caseforge.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.university.caseforge.factory.CaseFactory;
import it.university.caseforge.model.Accusation;
import it.university.caseforge.model.Answer;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.Contradiction;
import it.university.caseforge.model.EvaluationResult;
import it.university.caseforge.model.Evidence;
import it.university.caseforge.model.Interrogation;
import it.university.caseforge.model.Investigation;
import it.university.caseforge.model.InvestigationStatus;
import it.university.caseforge.model.Question;
import it.university.caseforge.model.Suspect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JsonInvestigationRepository implements InvestigationRepository {

    private final CaseFactory caseFactory;
    private final ObjectMapper objectMapper;

    public JsonInvestigationRepository(CaseFactory caseFactory) {
        this.caseFactory = Objects.requireNonNull(caseFactory);
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void save(Investigation investigation, String selectedSuspectId, Path path) throws IOException {
        Objects.requireNonNull(investigation);
        Path nonNullPath = Objects.requireNonNull(path);
        Path parent = nonNullPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        InvestigationSaveData data = toSaveData(investigation, selectedSuspectId);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(nonNullPath.toFile(), data);
    }

    @Override
    public LoadedInvestigation load(Path path) throws IOException {
        Path nonNullPath = Objects.requireNonNull(path);
        InvestigationSaveData data = objectMapper.readValue(nonNullPath.toFile(), InvestigationSaveData.class);
        validate(data);

        CaseFile caseFile = caseFactory.createCase(data.caseId);
        Investigation investigation = new Investigation(caseFile);

        for (String evidenceId : safeList(data.discoveredEvidenceIds)) {
            investigation.discoverEvidence(evidenceId);
        }
        for (QuestionStateData questionState : safeList(data.obtainedQuestions)) {
            investigation.askQuestion(
                    questionState.suspectId,
                    questionState.interrogationId,
                    questionState.questionId
            );
        }
        for (EvidenceSuspectLinkData link : safeList(data.evidenceSuspectLinks)) {
            investigation.linkEvidenceToSuspect(link.evidenceId, link.suspectId);
        }
        for (EvidenceAnswerLinkData link : safeList(data.evidenceAnswerLinks)) {
            investigation.linkEvidenceToAnswer(
                    link.evidenceId,
                    link.suspectId,
                    link.interrogationId,
                    link.questionId
            );
        }

        InvestigationStatus status = parseStatus(data.status);
        if (status != InvestigationStatus.OPEN) {
            if (data.accusation == null || data.evaluationResult == null) {
                throw new IOException("File indagine non valido: accusa o valutazione finale mancante.");
            }
            investigation.restoreState(status, toAccusation(data.accusation), toEvaluationResult(data.evaluationResult));
        }

        return new LoadedInvestigation(investigation, data.selectedSuspectId);
    }

    private InvestigationSaveData toSaveData(Investigation investigation, String selectedSuspectId) {
        CaseFile caseFile = investigation.getCaseFile();
        InvestigationSaveData data = new InvestigationSaveData();
        data.caseId = caseFile.getId();
        data.selectedSuspectId = selectedSuspectId;
        data.status = investigation.getStatus().name();

        for (Evidence evidence : caseFile.getEvidences()) {
            if (evidence.isDiscovered()) {
                data.discoveredEvidenceIds.add(evidence.getId());
            }
            for (String suspectId : evidence.getLinkedSuspectIds()) {
                data.evidenceSuspectLinks.add(new EvidenceSuspectLinkData(evidence.getId(), suspectId));
            }
        }

        for (Suspect suspect : caseFile.getSuspects()) {
            for (Interrogation interrogation : suspect.getInterrogations()) {
                collectQuestionState(data, suspect, interrogation);
                collectContradictions(data, interrogation);
            }
        }

        if (investigation.getLastAccusation() != null) {
            data.accusation = toAccusationData(investigation.getLastAccusation());
        }
        if (investigation.getLastEvaluationResult() != null) {
            data.evaluationResult = toEvaluationResultData(investigation.getLastEvaluationResult());
        }

        return data;
    }

    private void collectQuestionState(
            InvestigationSaveData data,
            Suspect suspect,
            Interrogation interrogation
    ) {
        for (Question question : interrogation.getQuestions()) {
            if (!question.isAnswerObtained() || question.getAnswer() == null) {
                continue;
            }

            Answer answer = question.getAnswer();
            data.obtainedQuestions.add(new QuestionStateData(
                    suspect.getId(),
                    interrogation.getId(),
                    question.getId(),
                    answer.getId()
            ));

            for (String evidenceId : answer.getLinkedEvidenceIds()) {
                data.evidenceAnswerLinks.add(new EvidenceAnswerLinkData(
                        evidenceId,
                        suspect.getId(),
                        interrogation.getId(),
                        question.getId()
                ));
            }
        }
    }

    private void collectContradictions(InvestigationSaveData data, Interrogation interrogation) {
        for (Contradiction contradiction : interrogation.getContradictions()) {
            data.contradictions.add(new ContradictionData(
                    contradiction.getId(),
                    contradiction.getSuspectId(),
                    interrogation.getId(),
                    contradiction.getQuestion().getId(),
                    contradiction.getEvidence().getId()
            ));
        }
    }

    private AccusationData toAccusationData(Accusation accusation) {
        return new AccusationData(
                accusation.getSuspectId(),
                accusation.getPrimaryEvidenceId(),
                accusation.getPrimaryContradictionId(),
                accusation.getRelevantTimelineEventId(),
                accusation.getReasoning()
        );
    }

    private EvaluationResultData toEvaluationResultData(EvaluationResult result) {
        return new EvaluationResultData(
                result.isSolved(),
                result.isCorrectSuspect(),
                result.isCorrectPrimaryEvidence(),
                result.isCorrectPrimaryContradiction(),
                result.isCorrectTimelineEvent(),
                result.getScore(),
                result.getMessage(),
                new ArrayList<>(result.getMissingEvidenceIds())
        );
    }

    private Accusation toAccusation(AccusationData data) {
        return new Accusation(
                data.suspectId,
                data.primaryEvidenceId,
                data.primaryContradictionId,
                data.relevantTimelineEventId,
                data.reasoning
        );
    }

    private EvaluationResult toEvaluationResult(EvaluationResultData data) {
        return new EvaluationResult(
                data.solved,
                data.correctSuspect,
                data.correctPrimaryEvidence,
                data.correctPrimaryContradiction,
                data.correctTimelineEvent,
                data.score,
                data.message,
                new LinkedHashSet<>(safeList(data.missingEvidenceIds))
        );
    }

    private void validate(InvestigationSaveData data) throws IOException {
        if (data == null || data.caseId == null || data.caseId.isBlank()) {
            throw new IOException("File indagine non valido: caso investigativo mancante.");
        }
        parseStatus(data.status);
    }

    private InvestigationStatus parseStatus(String value) throws IOException {
        if (value == null || value.isBlank()) {
            return InvestigationStatus.OPEN;
        }
        try {
            return InvestigationStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IOException("File indagine non valido: stato indagine sconosciuto " + value + ".", exception);
        }
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    public static final class InvestigationSaveData {
        public String formatVersion = "1";
        public String caseId;
        public String selectedSuspectId;
        public String status = InvestigationStatus.OPEN.name();
        public List<String> discoveredEvidenceIds = new ArrayList<>();
        public List<EvidenceSuspectLinkData> evidenceSuspectLinks = new ArrayList<>();
        public List<QuestionStateData> obtainedQuestions = new ArrayList<>();
        public List<EvidenceAnswerLinkData> evidenceAnswerLinks = new ArrayList<>();
        public List<ContradictionData> contradictions = new ArrayList<>();
        public AccusationData accusation;
        public EvaluationResultData evaluationResult;
    }

    public static final class EvidenceSuspectLinkData {
        public String evidenceId;
        public String suspectId;

        public EvidenceSuspectLinkData() {
        }

        public EvidenceSuspectLinkData(String evidenceId, String suspectId) {
            this.evidenceId = evidenceId;
            this.suspectId = suspectId;
        }
    }

    public static final class QuestionStateData {
        public String suspectId;
        public String interrogationId;
        public String questionId;
        public String answerId;

        public QuestionStateData() {
        }

        public QuestionStateData(
                String suspectId,
                String interrogationId,
                String questionId,
                String answerId
        ) {
            this.suspectId = suspectId;
            this.interrogationId = interrogationId;
            this.questionId = questionId;
            this.answerId = answerId;
        }
    }

    public static final class EvidenceAnswerLinkData {
        public String evidenceId;
        public String suspectId;
        public String interrogationId;
        public String questionId;

        public EvidenceAnswerLinkData() {
        }

        public EvidenceAnswerLinkData(
                String evidenceId,
                String suspectId,
                String interrogationId,
                String questionId
        ) {
            this.evidenceId = evidenceId;
            this.suspectId = suspectId;
            this.interrogationId = interrogationId;
            this.questionId = questionId;
        }
    }

    public static final class ContradictionData {
        public String id;
        public String suspectId;
        public String interrogationId;
        public String questionId;
        public String evidenceId;

        public ContradictionData() {
        }

        public ContradictionData(
                String id,
                String suspectId,
                String interrogationId,
                String questionId,
                String evidenceId
        ) {
            this.id = id;
            this.suspectId = suspectId;
            this.interrogationId = interrogationId;
            this.questionId = questionId;
            this.evidenceId = evidenceId;
        }
    }

    public static final class AccusationData {
        public String suspectId;
        public String primaryEvidenceId;
        public String primaryContradictionId;
        public String relevantTimelineEventId;
        public String reasoning;

        public AccusationData() {
        }

        public AccusationData(
                String suspectId,
                String primaryEvidenceId,
                String primaryContradictionId,
                String relevantTimelineEventId,
                String reasoning
        ) {
            this.suspectId = suspectId;
            this.primaryEvidenceId = primaryEvidenceId;
            this.primaryContradictionId = primaryContradictionId;
            this.relevantTimelineEventId = relevantTimelineEventId;
            this.reasoning = reasoning;
        }
    }

    public static final class EvaluationResultData {
        public boolean solved;
        public boolean correctSuspect;
        public boolean correctPrimaryEvidence;
        public boolean correctPrimaryContradiction;
        public boolean correctTimelineEvent;
        public int score;
        public String message;
        public List<String> missingEvidenceIds = new ArrayList<>();

        public EvaluationResultData() {
        }

        public EvaluationResultData(
                boolean solved,
                boolean correctSuspect,
                boolean correctPrimaryEvidence,
                boolean correctPrimaryContradiction,
                boolean correctTimelineEvent,
                int score,
                String message,
                List<String> missingEvidenceIds
        ) {
            this.solved = solved;
            this.correctSuspect = correctSuspect;
            this.correctPrimaryEvidence = correctPrimaryEvidence;
            this.correctPrimaryContradiction = correctPrimaryContradiction;
            this.correctTimelineEvent = correctTimelineEvent;
            this.score = score;
            this.message = message;
            this.missingEvidenceIds = new ArrayList<>(Objects.requireNonNull(missingEvidenceIds));
        }
    }
}
