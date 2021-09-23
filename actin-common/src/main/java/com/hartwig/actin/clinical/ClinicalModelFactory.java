package com.hartwig.actin.clinical;

import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationModel;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.feed.FeedModel;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class ClinicalModelFactory {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalModelFactory.class);

    @NotNull
    private final FeedModel feed;
    @NotNull
    private final CurationModel curation;

    @NotNull
    public static ClinicalModel fromFeedAndCurationDirectories(@NotNull String clinicalFeedDirectory,
            @NotNull String clinicalCurationDirectory) throws IOException {
        return new ClinicalModelFactory(FeedModel.fromFeedDirectory(clinicalFeedDirectory),
                CurationModel.fromCurationDirectory(clinicalCurationDirectory)).create();
    }

    @VisibleForTesting
    ClinicalModelFactory(@NotNull final FeedModel feed, @NotNull final CurationModel curation) {
        this.feed = feed;
        this.curation = curation;
    }

    @NotNull
    @VisibleForTesting
    ClinicalModel create() {
        LOGGER.info("Creating clinical model");
        List<ClinicalRecord> records = Lists.newArrayList();
        for (String subject : feed.subjects()) {
            String sampleId = toSampleId(subject);
            LOGGER.info(" Extracting data for sample {}", sampleId);

            QuestionnaireEntry questionnaire = feed.latestQuestionnaireForSubject(subject);
            records.add(ImmutableClinicalRecord.builder()
                    .sampleId(sampleId)
                    .patient(extractPatientDetails(subject, questionnaire))
                    .tumor(createTumorDetails(questionnaire))
                    .clinicalStatus(extractClinicalStatus())
                    .priorTumorTreatments(extractPriorTumorTreatments(subject))
                    .build());
        }

        return new ClinicalModel(records);
    }

    @NotNull
    private PatientDetails extractPatientDetails(@NotNull String subject, @NotNull QuestionnaireEntry questionnaire) {
        PatientEntry patient = feed.patient(subject);

        return ImmutablePatientDetails.builder()
                .sex(patient.sex())
                .birthYear(patient.birthYear())
                .registrationDate(patient.periodStart())
                .questionnaireDate(questionnaire != null ? questionnaire.authoredDateTime() : null)
                .build();
    }

    @NotNull
    private TumorDetails createTumorDetails(@NotNull QuestionnaireEntry questionnaire) {
        String tumorLocation = QuestionnaireExtraction.tumorLocation(questionnaire);
        String tumorType = QuestionnaireExtraction.tumorType(questionnaire);

        return curation.toTumorDetails(tumorLocation, tumorType);
    }

    @NotNull
    private List<PriorTumorTreatment> extractPriorTumorTreatments(@NotNull String subject) {
        QuestionnaireEntry latestQuestionnaire = feed.latestQuestionnaireForSubject(subject);

        if (latestQuestionnaire != null) {
            List<String> treatmentHistories = QuestionnaireExtraction.treatmentHistoriesCurrentTumor(latestQuestionnaire);
            return treatmentHistories != null ? curation.toPriorTumorTreatments(treatmentHistories) : Lists.newArrayList();
        } else {
            return Lists.newArrayList();
        }
    }

    @NotNull
    private static ClinicalStatus extractClinicalStatus() {
        return ImmutableClinicalStatus.builder()
                .who(0)
                .hasCurrentInfection(false)
                .hasSigAberrationLatestEcg(false)
                .cancerRelatedComplication(Strings.EMPTY)
                .build();
    }

    @NotNull
    private static String toSampleId(@NotNull String subject) {
        // Assume a single sample per patient ending with "T". No "TII" supported yet.
        return subject.replaceAll("-", "") + "T";
    }
}
