package it.university.caseforge.factory;

import it.university.caseforge.model.Answer;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.CaseSolution;
import it.university.caseforge.model.DigitalEvidence;
import it.university.caseforge.model.Evidence;
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
                "Chief financial officer of the startup and holder of emergency payment credentials.",
                "A pending investor audit could expose unauthorized transfers hidden in vendor budgets.",
                "Claims she joined a remote board call from home after 21:30."
        );

        Suspect luca = suspectFactory.createSuspect(
                "sus-luca-conti",
                "Luca Conti",
                "Co-founder recently removed from product leadership after a bitter governance dispute.",
                "He wanted leverage before signing away his remaining voting rights.",
                "Says he returned only to collect archived prototypes and left before the incident."
        );

        Suspect sofia = suspectFactory.createSuspect(
                "sus-sofia-rinaldi",
                "Sofia Rinaldi",
                "Senior platform engineer responsible for deployment pipelines and privileged server access.",
                "A canceled promotion and blame for recent outages gave her a credible personal grievance.",
                "Maintains she stayed on a production bridge call with two colleagues."
        );

        Suspect davide = suspectFactory.createSuspect(
                "sus-davide-serra",
                "Davide Serra",
                "Head of partnerships negotiating a make-or-break contract the victim was ready to reject.",
                "The victim's refusal would cost him a performance bonus and weaken his position.",
                "Claims he was meeting a partner downtown and never returned to headquarters."
        );

        DigitalEvidence emailWarning = evidenceFactory.createDigitalEvidence(
                "ev-email-warning",
                "Email: audit escalation",
                "An internal email from the victim announces that vendor reimbursements will be audited at 09:00.",
                "Victim mailbox export",
                "SHA-256:email-audit-warning"
        );

        DigitalEvidence badgeAccessLog = evidenceFactory.createDigitalEvidence(
                "ev-badge-log",
                "Badge access log",
                "The rear office badge reader records Marta entering the executive floor at 22:11.",
                "Building access controller",
                "SHA-256:badge-entry-2211"
        );

        TestimonyEvidence witnessStatement = evidenceFactory.createTestimonyEvidence(
                "ev-guard-statement",
                "Guard testimony",
                "The night guard saw a woman in a graphite coat leave the executive floor shortly after 22:30.",
                "Night guard Elena Valli",
                "She walked quickly, avoided the front desk, and kept a laptop bag close."
        );

        PhysicalEvidence fingerprint = evidenceFactory.createPhysicalEvidence(
                "ev-fingerprint",
                "Fingerprint on trophy shard",
                "A partial print matching Marta was recovered from a cracked acrylic award found near the victim.",
                "Executive meeting room",
                "Latent fingerprint"
        );

        DigitalEvidence phoneCall = evidenceFactory.createDigitalEvidence(
                "ev-call-record",
                "Phone call record",
                "Davide placed a thirteen-minute call to an investor contact during the critical window.",
                "Carrier metadata extract",
                "SHA-256:call-davide-investor"
        );

        DigitalEvidence serverLog = evidenceFactory.createDigitalEvidence(
                "ev-server-log",
                "Server privilege log",
                "A privileged session from Marta's laptop exports payment reconciliation files at 22:18.",
                "Finance data server",
                "SHA-256:server-export-2218"
        );

        PhysicalEvidence parkingTicket = evidenceFactory.createPhysicalEvidence(
                "ev-parking-ticket",
                "Parking garage ticket",
                "A paid exit ticket places Davide's car in the city garage at 22:24, away from headquarters.",
                "Garage kiosk receipt",
                "Printed thermal ticket"
        );

        DigitalEvidence chatMessage = evidenceFactory.createDigitalEvidence(
                "ev-chat-message",
                "Encrypted team chat export",
                "Sofia writes that she will stay on the release bridge until the rollback is stable.",
                "Incident chat archive",
                "SHA-256:chat-release-bridge"
        );

        linkEvidenceToSuspects(
                emailWarning,
                marta,
                luca,
                davide
        );
        linkEvidenceToSuspects(badgeAccessLog, marta);
        linkEvidenceToSuspects(witnessStatement, marta);
        linkEvidenceToSuspects(fingerprint, marta);
        linkEvidenceToSuspects(phoneCall, davide);
        linkEvidenceToSuspects(serverLog, marta);
        linkEvidenceToSuspects(parkingTicket, davide);
        linkEvidenceToSuspects(chatMessage, sofia);

        Interrogation martaInterrogation = createMartaInterrogation(marta, serverLog, fingerprint);
        Interrogation lucaInterrogation = createLucaInterrogation(luca);
        Interrogation sofiaInterrogation = createSofiaInterrogation(sofia);

        return CaseFile.builder("case-001", "Startup Midnight Breach")
                .description(
                        "At 22:36, the founder of HelixNova is found unconscious in the executive meeting room, "
                                + "minutes after preparing an audit package for investors. Sensitive finance exports "
                                + "vanish from the server, the office access timeline fractures, and several senior "
                                + "staff have both motive and proximity."
                )
                .addSuspect(marta)
                .addSuspect(luca)
                .addSuspect(sofia)
                .addSuspect(davide)
                .addEvidence(emailWarning)
                .addEvidence(badgeAccessLog)
                .addEvidence(witnessStatement)
                .addEvidence(fingerprint)
                .addEvidence(phoneCall)
                .addEvidence(serverLog)
                .addEvidence(parkingTicket)
                .addEvidence(chatMessage)
                .addInterrogation(martaInterrogation)
                .addInterrogation(lucaInterrogation)
                .addInterrogation(sofiaInterrogation)
                .addTimelineEvent(new TimelineEvent(
                        "tl-audit-mail",
                        LocalDateTime.of(2026, 3, 4, 18, 12),
                        "Audit email sent",
                        "The victim warns leadership that reimbursement files will be audited the next morning.",
                        null
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-investor-call",
                        LocalDateTime.of(2026, 3, 4, 21, 48),
                        "Partnership call begins",
                        "Davide calls an investor contact from downtown before the office incident.",
                        "sus-davide-serra"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-badge-entry",
                        LocalDateTime.of(2026, 3, 4, 22, 11),
                        "Executive floor badge entry",
                        "The rear reader accepts Marta's badge on the executive floor.",
                        "sus-marta-greco"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-server-export",
                        LocalDateTime.of(2026, 3, 4, 22, 18),
                        "Sensitive export detected",
                        "Finance reconciliation files are exported through a privileged server session.",
                        "sus-marta-greco"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-parking-exit",
                        LocalDateTime.of(2026, 3, 4, 22, 24),
                        "Garage exit paid",
                        "A parking ticket confirms Davide's vehicle exits a downtown garage.",
                        "sus-davide-serra"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-chat-bridge",
                        LocalDateTime.of(2026, 3, 4, 22, 27),
                        "Release bridge chat",
                        "Sofia posts a rollback status update in the production incident channel.",
                        "sus-sofia-rinaldi"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-witness-exit",
                        LocalDateTime.of(2026, 3, 4, 22, 31),
                        "Witnessed exit",
                        "The night guard reports a woman leaving the executive floor in a hurry.",
                        "sus-marta-greco"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-victim-found",
                        LocalDateTime.of(2026, 3, 4, 22, 36),
                        "Victim discovered",
                        "Security finds the founder injured beside broken acrylic fragments.",
                        null
                ))
                .solution(new CaseSolution(
                        "sus-marta-greco",
                        Set.of("ev-fingerprint", "ev-server-log"),
                        "Marta Greco staged a late remote-work alibi while retrieving sensitive files and "
                                + "confronting the founder over the audit."
                ))
                .build();
    }

    private Interrogation createMartaInterrogation(
            Suspect marta,
            DigitalEvidence serverLog,
            PhysicalEvidence fingerprint
    ) {
        Interrogation interrogation = new Interrogation(
                "int-marta-001",
                marta,
                LocalDateTime.of(2026, 3, 5, 9, 30)
        );

        Question serverAccess = new Question(
                "Did you access company finance systems after 22:00?",
                QuestionCategory.ACCESS
        );
        serverAccess.answerWith(new Answer(
                "No. I stayed on the board call and never opened finance tools.",
                ReliabilityLevel.HIGH,
                serverLog
        ));

        Question meetingRoom = new Question(
                "Did you enter the executive meeting room after office hours?",
                QuestionCategory.TIMELINE
        );
        meetingRoom.answerWith(new Answer(
                "No. I had no reason to go back to headquarters.",
                ReliabilityLevel.MEDIUM,
                fingerprint
        ));

        Question motive = new Question(
                "Were you worried about the audit announced that evening?",
                QuestionCategory.MOTIVE
        );
        motive.answerWith(new Answer(
                "It was routine. I was not personally concerned.",
                ReliabilityLevel.MEDIUM
        ));

        interrogation.addQuestion(serverAccess);
        interrogation.addQuestion(meetingRoom);
        interrogation.addQuestion(motive);
        return interrogation;
    }

    private Interrogation createLucaInterrogation(Suspect luca) {
        Interrogation interrogation = new Interrogation(
                "int-luca-001",
                luca,
                LocalDateTime.of(2026, 3, 5, 10, 15)
        );

        Question emailReaction = new Question(
                "Did the audit email change your plans that evening?",
                QuestionCategory.MOTIVE
        );
        emailReaction.answerWith(new Answer(
                "I read it, but it did not involve my current role.",
                ReliabilityLevel.MEDIUM
        ));

        Question access = new Question(
                "Did you access the executive floor after 22:00?",
                QuestionCategory.ACCESS
        );
        access.answerWith(new Answer(
                "No. My visit ended before the building entered night mode.",
                ReliabilityLevel.MEDIUM
        ));

        interrogation.addQuestion(emailReaction);
        interrogation.addQuestion(access);
        return interrogation;
    }

    private Interrogation createSofiaInterrogation(Suspect sofia) {
        Interrogation interrogation = new Interrogation(
                "int-sofia-001",
                sofia,
                LocalDateTime.of(2026, 3, 5, 11, 0)
        );

        Question releaseBridge = new Question(
                "Were you continuously present on the release bridge during the incident window?",
                QuestionCategory.TIMELINE
        );
        releaseBridge.answerWith(new Answer(
                "Yes. I was coordinating rollback steps in chat and on the incident call.",
                ReliabilityLevel.HIGH
        ));

        Question motive = new Question(
                "Did you resent leadership after the promotion delay?",
                QuestionCategory.MOTIVE
        );
        motive.answerWith(new Answer(
                "I was angry, but I wanted the platform stable, not revenge.",
                ReliabilityLevel.MEDIUM
        ));

        interrogation.addQuestion(releaseBridge);
        interrogation.addQuestion(motive);
        return interrogation;
    }

    private void linkEvidenceToSuspects(Evidence evidence, Suspect... suspects) {
        for (Suspect suspect : suspects) {
            evidence.linkToSuspect(suspect);
        }
    }
}
