package it.university.caseforge.factory;

import it.university.caseforge.model.Answer;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.CaseSolution;
import it.university.caseforge.model.Contradiction;
import it.university.caseforge.model.DigitalEvidence;
import it.university.caseforge.model.Interrogation;
import it.university.caseforge.model.PhysicalEvidence;
import it.university.caseforge.model.Question;
import it.university.caseforge.model.QuestionCategory;
import it.university.caseforge.model.ReliabilityLevel;
import it.university.caseforge.model.Suspect;
import it.university.caseforge.model.TestimonyEvidence;
import it.university.caseforge.model.TimelineEvent;

import java.time.LocalDateTime;
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
                "Direttrice finanziaria della startup e titolare delle credenziali di pagamento d'emergenza.",
                "Un audit imminente degli investitori potrebbe far emergere trasferimenti non autorizzati nascosti nei budget dei fornitori.",
                "Sostiene di essersi collegata da casa a una riunione da remoto del consiglio dopo le 21:30."
        );

        Suspect luca = suspectFactory.createSuspect(
                "sus-luca-conti",
                "Luca Conti",
                "Co-fondatore appena estromesso dalla guida del prodotto dopo un duro scontro societario.",
                "Voleva aumentare il proprio potere negoziale prima di cedere gli ultimi diritti di voto.",
                "Dice di essere rientrato solo per recuperare prototipi archiviati e di essere andato via prima dell'incidente."
        );

        Suspect sofia = suspectFactory.createSuspect(
                "sus-sofia-rinaldi",
                "Sofia Rinaldi",
                "Ingegnera senior di piattaforma, responsabile delle pipeline di rilascio e degli accessi privilegiati ai server.",
                "Una promozione annullata e le accuse per recenti disservizi le davano un risentimento personale credibile.",
                "Afferma di essere rimasta in chiamata operativa con due colleghi durante la finestra critica."
        );

        Suspect davide = suspectFactory.createSuspect(
                "sus-davide-serra",
                "Davide Serra",
                "Responsabile partnership impegnato in una trattativa decisiva che la vittima era pronta a respingere.",
                "Il rifiuto della vittima gli avrebbe fatto perdere un bonus e avrebbe indebolito la sua posizione.",
                "Sostiene di aver incontrato un partner in centro e di non essere mai rientrato in sede."
        );

        DigitalEvidence emailWarning = evidenceFactory.createDigitalEvidence(
                "ev-email-warning",
                "Email: escalation dell'audit",
                "Una email interna della vittima annuncia che i rimborsi ai fornitori saranno verificati alle 09:00.",
                "Esportazione della casella email della vittima",
                "SHA-256:email-audit-warning"
        );

        DigitalEvidence badgeAccessLog = evidenceFactory.createDigitalEvidence(
                "ev-badge-log",
                "Registro accessi badge",
                "Il lettore badge dell'ingresso posteriore registra Marta al piano direzionale alle 22:11.",
                "Controller accessi dell'edificio",
                "SHA-256:badge-entry-2211"
        );

        TestimonyEvidence witnessStatement = evidenceFactory.createTestimonyEvidence(
                "ev-guard-statement",
                "Testimonianza della guardia",
                "La guardia notturna ha visto una donna con cappotto color grafite lasciare il piano direzionale poco dopo le 22:30.",
                "Guardia notturna Elena Valli",
                "Camminava in fretta, ha evitato il banco reception e teneva stretta una borsa porta computer."
        );

        PhysicalEvidence fingerprint = evidenceFactory.createPhysicalEvidence(
                "ev-fingerprint",
                "Impronta su frammento di trofeo",
                "Una traccia parziale compatibile con Marta e stata rilevata su un premio acrilico incrinato trovato vicino alla vittima.",
                "Sala riunioni direzionale",
                "Impronta latente"
        );

        DigitalEvidence phoneCall = evidenceFactory.createDigitalEvidence(
                "ev-call-record",
                "Registro telefonata",
                "Davide ha effettuato una chiamata di tredici minuti a un contatto degli investitori durante la finestra critica.",
                "Estratto metadati dell'operatore",
                "SHA-256:call-davide-investor"
        );

        DigitalEvidence serverLog = evidenceFactory.createDigitalEvidence(
                "ev-server-log",
                "Log privilegiati del server",
                "Una sessione privilegiata dal portatile di Marta esporta file di riconciliazione pagamenti alle 22:18.",
                "Server dati finanziari",
                "SHA-256:server-export-2218"
        );

        PhysicalEvidence parkingTicket = evidenceFactory.createPhysicalEvidence(
                "ev-parking-ticket",
                "Ticket del parcheggio",
                "Un ticket di uscita pagato colloca l'auto di Davide nel garage cittadino alle 22:24, lontano dalla sede.",
                "Ricevuta del chiosco parcheggio",
                "Scontrino termico stampato"
        );

        DigitalEvidence chatMessage = evidenceFactory.createDigitalEvidence(
                "ev-chat-message",
                "Esportazione chat cifrata del team",
                "Sofia scrive che restera sulla call di rilascio finche il rollback non sara stabile.",
                "Archivio chat dell'incidente",
                "SHA-256:chat-release-bridge"
        );

        Interrogation martaInterrogation = createMartaInterrogation(marta, serverLog, fingerprint);
        Interrogation lucaInterrogation = createLucaInterrogation(luca);
        Interrogation sofiaInterrogation = createSofiaInterrogation(sofia);

        return CaseFile.builder("case-001", "Violazione di mezzanotte in HelixNova")
                .description(
                        "Alle 22:36, il fondatore di HelixNova viene trovato privo di sensi nella sala riunioni "
                                + "direzionale, pochi minuti dopo aver preparato un dossier di audit per gli investitori. "
                                + "Dal server spariscono esportazioni finanziarie sensibili, la cronologia degli accessi "
                                + "si frammenta e diversi dirigenti hanno sia un movente sia una presenza plausibile."
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
                        "Email sull'audit inviata",
                        "La vittima avverte la direzione che i file dei rimborsi saranno verificati la mattina seguente.",
                        null
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-investor-call",
                        LocalDateTime.of(2026, 3, 4, 21, 48),
                        "Inizia la chiamata sulla partnership",
                        "Davide contatta un referente degli investitori dal centro prima dell'incidente in ufficio.",
                        "sus-davide-serra"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-badge-entry",
                        LocalDateTime.of(2026, 3, 4, 22, 11),
                        "Ingresso badge al piano direzionale",
                        "Il lettore posteriore accetta il badge di Marta al piano direzionale.",
                        "sus-marta-greco"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-server-export",
                        LocalDateTime.of(2026, 3, 4, 22, 18),
                        "Esportazione sensibile rilevata",
                        "I file di riconciliazione finanziaria vengono esportati tramite una sessione privilegiata del server.",
                        "sus-marta-greco"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-parking-exit",
                        LocalDateTime.of(2026, 3, 4, 22, 24),
                        "Uscita dal garage pagata",
                        "Un ticket del parcheggio conferma che l'auto di Davide lascia un garage in centro.",
                        "sus-davide-serra"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-chat-bridge",
                        LocalDateTime.of(2026, 3, 4, 22, 27),
                        "Chat della call di rilascio",
                        "Sofia pubblica un aggiornamento sul rollback nel canale dell'incidente di produzione.",
                        "sus-sofia-rinaldi"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-witness-exit",
                        LocalDateTime.of(2026, 3, 4, 22, 31),
                        "Uscita osservata",
                        "La guardia notturna riferisce di una donna che lascia in fretta il piano direzionale.",
                        "sus-marta-greco"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-victim-found",
                        LocalDateTime.of(2026, 3, 4, 22, 36),
                        "Vittima ritrovata",
                        "La sicurezza trova il fondatore ferito accanto a frammenti di acrilico spezzati.",
                        null
                ))
                .solution(new CaseSolution(
                        "sus-marta-greco",
                        "ev-server-log",
                        Contradiction.idFor(
                                "sus-marta-greco",
                                "q-marta-server-access",
                                "ev-server-log"
                        ),
                        "tl-server-export",
                        "Marta Greco ha costruito un alibi da lavoro remoto mentre recuperava file sensibili "
                                + "e affrontava il fondatore per l'audit."
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
                "q-marta-server-access",
                "Ha effettuato accessi ai sistemi finanziari aziendali dopo le 22:00?",
                QuestionCategory.ACCESS
        );
        serverAccess.answerWith(new Answer(
                "ans-marta-server-access",
                "No. Sono rimasta nella riunione del consiglio e non ho mai aperto strumenti finanziari.",
                ReliabilityLevel.HIGH,
                serverLog
        ));

        Question meetingRoom = new Question(
                "q-marta-meeting-room",
                "E entrata nella sala riunioni direzionale dopo l'orario d'ufficio?",
                QuestionCategory.TIMELINE
        );
        meetingRoom.answerWith(new Answer(
                "ans-marta-meeting-room",
                "No. Non avevo alcun motivo per tornare in sede.",
                ReliabilityLevel.MEDIUM,
                fingerprint
        ));

        Question motive = new Question(
                "q-marta-audit-worry",
                "Era preoccupata per l'audit annunciato quella sera?",
                QuestionCategory.MOTIVE
        );
        motive.answerWith(new Answer(
                "ans-marta-audit-worry",
                "Era una procedura ordinaria. Non ero preoccupata a livello personale.",
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
                "q-luca-audit-email",
                "L'email sull'audit ha cambiato i suoi piani per quella sera?",
                QuestionCategory.MOTIVE
        );
        emailReaction.answerWith(new Answer(
                "ans-luca-audit-email",
                "L'ho letta, ma non riguardava il mio ruolo attuale.",
                ReliabilityLevel.MEDIUM
        ));

        Question access = new Question(
                "q-luca-access-floor",
                "Ha raggiunto il piano direzionale dopo le 22:00?",
                QuestionCategory.ACCESS
        );
        access.answerWith(new Answer(
                "ans-luca-access-floor",
                "No. La mia visita si e conclusa prima che l'edificio passasse alla modalita notturna.",
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
                "q-sofia-release-bridge",
                "E rimasta presente senza interruzioni sulla call di rilascio durante la finestra dell'incidente?",
                QuestionCategory.TIMELINE
        );
        releaseBridge.answerWith(new Answer(
                "ans-sofia-release-bridge",
                "Si. Coordinavo i passaggi di rollback in chat e nella chiamata dell'incidente.",
                ReliabilityLevel.HIGH
        ));

        Question motive = new Question(
                "q-sofia-promotion",
                "Provava risentimento verso la direzione dopo il rinvio della promozione?",
                QuestionCategory.MOTIVE
        );
        motive.answerWith(new Answer(
                "ans-sofia-promotion",
                "Ero arrabbiata, ma volevo una piattaforma stabile, non vendetta.",
                ReliabilityLevel.MEDIUM
        ));

        interrogation.addQuestion(releaseBridge);
        interrogation.addQuestion(motive);
        return interrogation;
    }

}
