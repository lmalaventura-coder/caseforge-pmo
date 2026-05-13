package it.university.caseforge.factory;

import it.university.caseforge.model.Answer;
import it.university.caseforge.model.CaseFile;
import it.university.caseforge.model.CaseSolution;
import it.university.caseforge.model.CaseSummary;
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
import java.util.List;

public class DemoCaseFactory implements CaseFactory {

    public static final String HELIX_NOVA_CASE_ID = "case-001";
    public static final String PROTOTYPE_THEFT_CASE_ID = "case-002";

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
    public List<CaseSummary> availableCases() {
        return List.of(
                new CaseSummary(HELIX_NOVA_CASE_ID, "Violazione di mezzanotte in HelixNova"),
                new CaseSummary(PROTOTYPE_THEFT_CASE_ID, "Il furto del prototipo")
        );
    }

    @Override
    public CaseFile createCase(String caseId) {
        return switch (caseId) {
            case HELIX_NOVA_CASE_ID -> createHelixNovaCase();
            case PROTOTYPE_THEFT_CASE_ID -> createPrototypeTheftCase();
            default -> throw new IllegalArgumentException("Caso investigativo sconosciuto: " + caseId);
        };
    }

    @Override
    public CaseFile createDemoCase() {
        return createHelixNovaCase();
    }

    private CaseFile createHelixNovaCase() {
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
                "Il rifiuto della vittima gli avrebbe fatto perdere un bonus e avrebbe indebolito la sua posizione: e una pista forte, ma non conclusiva.",
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
                "Il lettore badge dell'ingresso posteriore registra il badge assegnato a Marta al piano direzionale alle 22:11; il log non prova da solo chi lo stesse usando.",
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
                "Davide ha effettuato una chiamata di tredici minuti a un contatto degli investitori durante la finestra critica, dettaglio sospetto ma compatibile con la trattativa in corso.",
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
        Interrogation davideInterrogation = createDavideInterrogation(davide);

        return CaseFile.builder(HELIX_NOVA_CASE_ID, "Violazione di mezzanotte in HelixNova")
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
                .addInterrogation(davideInterrogation)
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

    private CaseFile createPrototypeTheftCase() {
        Suspect enrico = suspectFactory.createSuspect(
                "sus-enrico-bassi",
                "Enrico Bassi",
                "Ricercatore rivale del dipartimento di robotica, escluso dalla dimostrazione privata.",
                "Un risultato fallito nel suo laboratorio lo rendeva interessato a recuperare specifiche del prototipo.",
                "Sostiene di essere rimasto in biblioteca dopo la demo."
        );

        Suspect nadia = suspectFactory.createSuspect(
                "sus-nadia-ferri",
                "Nadia Ferri",
                "Stagista del laboratorio, incaricata di preparare il prototipo e chiudere il locker tecnico.",
                "Temeva di perdere la borsa di studio e aveva ricevuto pressioni da un acceleratore concorrente.",
                "Dichiara di essere uscita dal campus alle 18:30 senza rientrare in laboratorio."
        );

        Suspect marco = suspectFactory.createSuspect(
                "sus-marco-valenti",
                "Marco Valenti",
                "Responsabile sicurezza del polo universitario e custode dei registri di accesso.",
                "Un errore di vigilanza avrebbe potuto costargli il rinnovo del contratto.",
                "Dice di aver seguito il giro serale senza entrare nel laboratorio prototipi."
        );

        DigitalEvidence badgeLog = evidenceFactory.createDigitalEvidence(
                "ev-prototype-badge-log",
                "Badge ingresso laboratorio",
                "Il badge assegnato a Nadia viene registrato all'ingresso del laboratorio alle 19:42.",
                "Sistema accessi universitario",
                "SHA-256:prototype-badge-1942"
        );

        DigitalEvidence printerLog = evidenceFactory.createDigitalEvidence(
                "ev-prototype-printer-log",
                "Log stampante 3D",
                "La stampante 3D del banco prototipi avvia una stampa di supporto alle 19:51.",
                "Console laboratorio maker",
                "SHA-256:printer-support-1951"
        );

        DigitalEvidence deletedEmail = evidenceFactory.createDigitalEvidence(
                "ev-prototype-deleted-email",
                "Email cancellata sulle specifiche",
                "Un messaggio eliminato chiede a Enrico dettagli sulle tolleranze del prototipo mostrato in demo.",
                "Backup posta dipartimentale",
                "SHA-256:deleted-prototype-mail"
        );

        PhysicalEvidence lockerFingerprint = evidenceFactory.createPhysicalEvidence(
                "ev-prototype-locker-fingerprint",
                "Impronta sul locker tecnico",
                "Una impronta parziale compatibile con Nadia e rilevata sul locker dove era custodito il prototipo.",
                "Locker 4B del laboratorio",
                "Impronta latente"
        );

        TestimonyEvidence porterStatement = evidenceFactory.createTestimonyEvidence(
                "ev-prototype-porter-statement",
                "Testimonianza del portiere",
                "Il portiere ricorda una persona con felpa del laboratorio uscire verso il cortile poco prima delle 20:00.",
                "Portineria del polo tecnologico",
                "La persona teneva uno zaino rigido e sembrava conoscere bene l'edificio."
        );

        Interrogation enricoInterrogation = createEnricoInterrogation(enrico);
        Interrogation nadiaInterrogation = createNadiaInterrogation(nadia, badgeLog, lockerFingerprint);
        Interrogation marcoInterrogation = createMarcoInterrogation(marco);

        return CaseFile.builder(PROTOTYPE_THEFT_CASE_ID, "Il furto del prototipo")
                .description(
                        "Dopo una dimostrazione privata in un laboratorio universitario, un prototipo di sensore "
                                + "per robotica sparisce dal locker tecnico. Le tracce indicano accessi serali, "
                                + "una falsa pista accademica e un alibi da verificare."
                )
                .addSuspect(enrico)
                .addSuspect(nadia)
                .addSuspect(marco)
                .addEvidence(badgeLog)
                .addEvidence(printerLog)
                .addEvidence(deletedEmail)
                .addEvidence(lockerFingerprint)
                .addEvidence(porterStatement)
                .addInterrogation(enricoInterrogation)
                .addInterrogation(nadiaInterrogation)
                .addInterrogation(marcoInterrogation)
                .addTimelineEvent(new TimelineEvent(
                        "tl-prototype-demo",
                        LocalDateTime.of(2026, 4, 18, 17, 30),
                        "Dimostrazione privata conclusa",
                        "Il prototipo viene riposto nel locker tecnico dopo la presentazione agli investitori.",
                        null
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-prototype-email",
                        LocalDateTime.of(2026, 4, 18, 18, 12),
                        "Email sospetta eliminata",
                        "Una email sulle specifiche viene cancellata dalla casella dipartimentale.",
                        "sus-enrico-bassi"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-prototype-badge-entry",
                        LocalDateTime.of(2026, 4, 18, 19, 42),
                        "Badge al laboratorio",
                        "Il badge di Nadia apre il laboratorio dopo l'orario dichiarato.",
                        "sus-nadia-ferri"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-prototype-printer",
                        LocalDateTime.of(2026, 4, 18, 19, 51),
                        "Stampante 3D attivata",
                        "La stampante del banco prototipi avvia un job compatibile con un supporto di trasporto.",
                        "sus-nadia-ferri"
                ))
                .addTimelineEvent(new TimelineEvent(
                        "tl-prototype-missing",
                        LocalDateTime.of(2026, 4, 18, 20, 10),
                        "Prototipo mancante",
                        "Il responsabile della demo trova il locker vuoto durante il controllo serale.",
                        null
                ))
                .solution(new CaseSolution(
                        "sus-nadia-ferri",
                        "ev-prototype-badge-log",
                        Contradiction.idFor(
                                "sus-nadia-ferri",
                                "q-nadia-return-lab",
                                "ev-prototype-badge-log"
                        ),
                        "tl-prototype-badge-entry",
                        "Nadia ha dichiarato di non essere rientrata, ma il suo badge apre il laboratorio "
                                + "poco prima dell'attivazione della stampante e della sparizione del prototipo."
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

        Question prototypes = new Question(
                "q-luca-prototype-archive",
                "Quali prototipi e venuto a recuperare quella sera?",
                QuestionCategory.GENERAL
        );
        prototypes.answerWith(new Answer(
                "ans-luca-prototype-archive",
                "Vecchi deck e mockup del prodotto. Non avevo interesse per i file finanziari.",
                ReliabilityLevel.MEDIUM
        ));

        interrogation.addQuestion(emailReaction);
        interrogation.addQuestion(access);
        interrogation.addQuestion(prototypes);
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

        Question serverAccess = new Question(
                "q-sofia-server-access",
                "Ha usato credenziali privilegiate sui server finanziari durante il rollback?",
                QuestionCategory.ACCESS
        );
        serverAccess.answerWith(new Answer(
                "ans-sofia-server-access",
                "No. I miei accessi erano limitati alla piattaforma di rilascio, non ai dati finanziari.",
                ReliabilityLevel.HIGH
        ));

        interrogation.addQuestion(releaseBridge);
        interrogation.addQuestion(motive);
        interrogation.addQuestion(serverAccess);
        return interrogation;
    }

    private Interrogation createDavideInterrogation(Suspect davide) {
        Interrogation interrogation = new Interrogation(
                "int-davide-001",
                davide,
                LocalDateTime.of(2026, 3, 5, 11, 45)
        );

        Question investorCall = new Question(
                "q-davide-investor-call",
                "Con chi era al telefono durante la finestra critica?",
                QuestionCategory.TIMELINE
        );
        investorCall.answerWith(new Answer(
                "ans-davide-investor-call",
                "Con un referente degli investitori. Stavamo cercando di salvare la partnership.",
                ReliabilityLevel.MEDIUM
        ));

        Question returnOffice = new Question(
                "q-davide-return-office",
                "E rientrato in sede dopo l'incontro in centro?",
                QuestionCategory.ALIBI
        );
        returnOffice.answerWith(new Answer(
                "ans-davide-return-office",
                "No. Ho lasciato il garage e sono andato direttamente a casa.",
                ReliabilityLevel.MEDIUM
        ));

        Question motive = new Question(
                "q-davide-partnership",
                "La decisione della vittima avrebbe compromesso il suo bonus?",
                QuestionCategory.MOTIVE
        );
        motive.answerWith(new Answer(
                "ans-davide-partnership",
                "Si, ma una rottura pubblica avrebbe danneggiato anche me. Volevo convincerlo, non eliminarlo.",
                ReliabilityLevel.MEDIUM
        ));

        interrogation.addQuestion(investorCall);
        interrogation.addQuestion(returnOffice);
        interrogation.addQuestion(motive);
        return interrogation;
    }

    private Interrogation createEnricoInterrogation(Suspect enrico) {
        Interrogation interrogation = new Interrogation(
                "int-enrico-001",
                enrico,
                LocalDateTime.of(2026, 4, 19, 9, 20)
        );

        Question demoAccess = new Question(
                "q-enrico-demo-access",
                "Ha provato a ottenere accesso alla dimostrazione privata?",
                QuestionCategory.ACCESS
        );
        demoAccess.answerWith(new Answer(
                "ans-enrico-demo-access",
                "Ho chiesto di assistere, ma mi e stato negato. Non sono entrato nel laboratorio.",
                ReliabilityLevel.MEDIUM
        ));

        Question deletedMail = new Question(
                "q-enrico-deleted-mail",
                "Perche compare una email cancellata sulle specifiche del prototipo?",
                QuestionCategory.MOTIVE
        );
        deletedMail.answerWith(new Answer(
                "ans-enrico-deleted-mail",
                "Era una richiesta accademica vecchia. L'ho eliminata per non alimentare sospetti inutili.",
                ReliabilityLevel.LOW
        ));

        interrogation.addQuestion(demoAccess);
        interrogation.addQuestion(deletedMail);
        return interrogation;
    }

    private Interrogation createNadiaInterrogation(
            Suspect nadia,
            DigitalEvidence badgeLog,
            PhysicalEvidence lockerFingerprint
    ) {
        Interrogation interrogation = new Interrogation(
                "int-nadia-001",
                nadia,
                LocalDateTime.of(2026, 4, 19, 10, 0)
        );

        Question returnLab = new Question(
                "q-nadia-return-lab",
                "E rientrata nel laboratorio dopo le 18:30?",
                QuestionCategory.TIMELINE
        );
        returnLab.answerWith(new Answer(
                "ans-nadia-return-lab",
                "No. Ho lasciato il campus dopo aver pulito il banco e non sono piu rientrata.",
                ReliabilityLevel.HIGH,
                badgeLog
        ));

        Question locker = new Question(
                "q-nadia-locker",
                "Ha toccato il locker tecnico dopo la dimostrazione?",
                QuestionCategory.ACCESS
        );
        locker.answerWith(new Answer(
                "ans-nadia-locker",
                "Solo durante la chiusura ufficiale, mai dopo l'orario di uscita.",
                ReliabilityLevel.MEDIUM,
                lockerFingerprint
        ));

        Question motive = new Question(
                "q-nadia-scholarship",
                "La pressione sulla borsa di studio poteva spingerla a vendere il prototipo?",
                QuestionCategory.MOTIVE
        );
        motive.answerWith(new Answer(
                "ans-nadia-scholarship",
                "Ero sotto pressione, ma perdere il posto sarebbe stato peggio di qualsiasi offerta esterna.",
                ReliabilityLevel.MEDIUM
        ));

        interrogation.addQuestion(returnLab);
        interrogation.addQuestion(locker);
        interrogation.addQuestion(motive);
        return interrogation;
    }

    private Interrogation createMarcoInterrogation(Suspect marco) {
        Interrogation interrogation = new Interrogation(
                "int-marco-001",
                marco,
                LocalDateTime.of(2026, 4, 19, 10, 40)
        );

        Question patrol = new Question(
                "q-marco-patrol",
                "Ha controllato personalmente il laboratorio durante il giro serale?",
                QuestionCategory.TIMELINE
        );
        patrol.answerWith(new Answer(
                "ans-marco-patrol",
                "Sono passato nel corridoio, ma non sono entrato: la porta risultava chiusa.",
                ReliabilityLevel.MEDIUM
        ));

        Question badgeReview = new Question(
                "q-marco-badge-review",
                "Ha verificato subito i badge dopo la segnalazione del furto?",
                QuestionCategory.ACCESS
        );
        badgeReview.answerWith(new Answer(
                "ans-marco-badge-review",
                "No. Ho consegnato il log la mattina dopo, appena richiesto dalla direzione.",
                ReliabilityLevel.MEDIUM
        ));

        interrogation.addQuestion(patrol);
        interrogation.addQuestion(badgeReview);
        return interrogation;
    }

}
