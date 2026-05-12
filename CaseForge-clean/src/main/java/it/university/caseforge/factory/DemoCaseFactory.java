package it.university.caseforge.factory;

import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.CaseSolution;
import it.university.caseforge.model.DigitalEvidence;
import it.university.caseforge.model.Answer;
import it.university.caseforge.model.Interrogation;
import it.university.caseforge.model.PhysicalEvidence;
import it.university.caseforge.model.Question;
import it.university.caseforge.model.QuestionCategory;
import it.university.caseforge.model.ReliabilityLevel;
import it.university.caseforge.model.Suspect;
import it.university.caseforge.model.TestimonyEvidence;
import it.university.caseforge.model.TimelineEvent;

import java.time.LocalDateTime;
import java.util.Set;

public class DemoCaseFactory implements CaseFactory {

    private final EvidenceFactory evidenceFactory;
    private final SuspectFactory suspectFactory;

    public DemoCaseFactory() {
        this(new EvidenceFactory(), new SuspectFactory());
    }

    public DemoCaseFactory(EvidenceFactory evidenceFactory, SuspectFactory suspectFactory) {
        this.evidenceFactory = evidenceFactory;
        this.suspectFactory = suspectFactory;
    }

    @Override
    public CaseFile createDemoCase() {
        Suspect marta = suspectFactory.createSuspect(
                "sus-marta-greco",
                "Marta Greco",
                "Chief financial officer with access to the victim's private files.",
                "The victim was about to expose a financial fraud.",
                "Claims she was in a video call from home."
        );

        Suspect luca = suspectFactory.createSuspect(
                "sus-luca-conti",
                "Luca Conti",
                "Former business partner recently removed from the company.",
                "Wanted revenge after losing control of the firm.",
                "Says he was driving outside the city."
        );

        PhysicalEvidence fingerprint = evidenceFactory.createPhysicalEvidence(
                "ev-fingerprint",
                "Partial fingerprint",
                "A partial fingerprint found on the letter opener.",
                "Victim office",
                "Latent print"
        );

        DigitalEvidence serverLog = evidenceFactory.createDigitalEvidence(
                "ev-server-log",
                "Server access log",
                "An admin login from Marta's laptop minutes before the crime.",
                "Accounting server",
                "SHA-256:demo-log-hash"
        );

        TestimonyEvidence guardStatement = evidenceFactory.createTestimonyEvidence(
                "ev-guard-statement",
                "Guard statement",
                "The night guard saw a woman leaving the office wing.",
                "Night guard",
                "A woman with a gray coat left at 22:15."
        );

        Interrogation martaInterrogation = new Interrogation(
                "int-marta-001",
                marta,
                LocalDateTime.of(2026, 3, 5, 9, 30)
        );
        Question martaServerAccess = new Question(
                "Did you access the accounting server after 22:00?",
                QuestionCategory.ACCESS
        );
        martaServerAccess.answerWith(new Answer(
                "No, I stayed away from company systems all evening.",
                ReliabilityLevel.HIGH,
                serverLog
        ));
        Question martaWeapon = new Question(
                "Did you handle the letter opener in the victim's office?",
                QuestionCategory.GENERAL
        );
        martaWeapon.answerWith(new Answer(
                "No, I never touched it.",
                ReliabilityLevel.MEDIUM,
                fingerprint
        ));
        martaInterrogation.addQuestion(martaServerAccess);
        martaInterrogation.addQuestion(martaWeapon);

        Interrogation lucaInterrogation = new Interrogation(
                "int-luca-001",
                luca,
                LocalDateTime.of(2026, 3, 5, 10, 15)
        );
        Question lucaLocation = new Question(
                "Where were you during the final hour before the body was found?",
                QuestionCategory.TIMELINE
        );
        lucaLocation.answerWith(new Answer(
                "I was driving outside the city and had no access to the office.",
                ReliabilityLevel.MEDIUM
        ));
        lucaInterrogation.addQuestion(lucaLocation);

        return CaseFile.builder("case-001", "The Locked Ledger")
                .description("A company director is found dead after announcing an internal audit.")
                .addSuspect(marta)
                .addSuspect(luca)
                .addEvidence(fingerprint)
                .addEvidence(serverLog)
                .addEvidence(guardStatement)
                .addInterrogation(martaInterrogation)
                .addInterrogation(lucaInterrogation)
                .addTimelineEvent(new TimelineEvent(
                        "tl-audit",
                        LocalDateTime.of(2026, 3, 4, 18, 0),
                        "Audit announcement",
                        "The victim announces an audit of accounting records.",
                        null
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-login",
                        LocalDateTime.of(2026, 3, 4, 22, 7),
                        "Suspicious login",
                        "The accounting server records a privileged login.",
                        "sus-marta-greco"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-body",
                        LocalDateTime.of(2026, 3, 4, 22, 40),
                        "Body discovered",
                        "Security finds the victim in the office.",
                        null
                ))
                .solution(new CaseSolution(
                        "sus-marta-greco",
                        Set.of("ev-fingerprint", "ev-server-log"),
                        "Marta Greco killed the victim to hide the fraud."
                ))
                .build();
    }
}
