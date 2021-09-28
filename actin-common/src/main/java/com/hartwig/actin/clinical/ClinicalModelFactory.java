package com.hartwig.actin.clinical;

import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationModel;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.feed.FeedModel;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

            QuestionnaireEntry entry = feed.latestQuestionnaireForSubject(subject);
            Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

            records.add(ImmutableClinicalRecord.builder()
                    .sampleId(sampleId)
                    .patient(extractPatientDetails(subject, entry))
                    .tumor(createTumorDetails(questionnaire))
                    .clinicalStatus(extractClinicalStatus())
                    .priorTumorTreatments(extractPriorTumorTreatments(questionnaire))
                    .build());
        }

        return new ClinicalModel(records);
    }

    @NotNull
    private PatientDetails extractPatientDetails(@NotNull String subject, @Nullable QuestionnaireEntry entry) {
        PatientEntry patient = feed.patient(subject);

        return ImmutablePatientDetails.builder()
                .sex(patient.sex())
                .birthYear(patient.birthYear())
                .registrationDate(patient.periodStart())
                .questionnaireDate(entry != null ? entry.authoredDateTime() : null)
                .build();
    }

    @NotNull
    private TumorDetails createTumorDetails(@Nullable Questionnaire questionnaire) {
        if (questionnaire == null) {
            return ImmutableTumorDetails.builder().build();
        }

        return ImmutableTumorDetails.builder()
                .from(curation.toTumorDetails(questionnaire.tumorLocation(), questionnaire.tumorType()))
                .stage(questionnaire.stage())
                .hasMeasurableLesionRecist(questionnaire.hasMeasurableLesionRecist())
                .hasBrainLesions(questionnaire.hasBrainLesions())
                .hasActiveBrainLesions(questionnaire.hasActiveBrainLesions())
                .hasSymptomaticBrainLesions(questionnaire.hasSymptomaticBrainLesions())
                .hasCnsLesions(questionnaire.hasCnsLesions())
                .hasActiveCnsLesions(questionnaire.hasActiveCnsLesions())
                .hasSymptomaticCnsLesions(questionnaire.hasSymptomaticCnsLesions())
                .hasBoneLesions(questionnaire.hasBoneLesions())
                .hasLiverLesions(questionnaire.hasLiverLesions())
                .build();
    }

    @NotNull
    private List<PriorTumorTreatment> extractPriorTumorTreatments(@Nullable Questionnaire questionnaire) {
        if (questionnaire != null) {
            List<String> treatmentHistories = questionnaire.treatmentHistoriesCurrentTumor();
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
