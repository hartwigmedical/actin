package com.hartwig.actin.clinical;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationModel;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Sex;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.feed.FeedModel;
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
    public static ClinicalModelFactory fromFeedAndCurationDirectories(@NotNull String clinicalFeedDirectory,
            @NotNull String clinicalCurationDirectory) throws IOException {
        return new ClinicalModelFactory(FeedModel.fromFeedDirectory(clinicalFeedDirectory),
                CurationModel.fromCurationDirectory(clinicalCurationDirectory));
    }

    public ClinicalModelFactory(@NotNull final FeedModel feed, @NotNull final CurationModel curation) {
        this.feed = feed;
        this.curation = curation;
    }

    @NotNull
    public ClinicalModel create() {
        LOGGER.info("Creating clinical model");
        List<ClinicalRecord> records = Lists.newArrayList();
        for (String subject : feed.subjects()) {
            String sampleId = toSampleId(subject);

            LOGGER.info(" Extracting data for sample {}", sampleId);
            records.add(ImmutableClinicalRecord.builder()
                    .sampleId(sampleId)
                    .patient(extractPatientDetails(subject))
                    .tumor(createTumorDetails())
                    .clinicalStatus(createClinicalStatus())
                    .priorTumorTreatments(extractPriorTumorTreatments(subject))
                    .build());
        }

        return new ClinicalModel(records);
    }

    @NotNull
    private PatientDetails extractPatientDetails(@NotNull String subject) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        return ImmutablePatientDetails.builder()
                .sex(Sex.MALE)
                .birthYear(0)
                .registrationDate(LocalDate.parse("01-01-2020", format))
                .questionnaireDate(LocalDate.parse("01-01-2020", format))
                .build();
    }

    @NotNull
    private List<PriorTumorTreatment> extractPriorTumorTreatments(@NotNull String subject) {
        QuestionnaireEntry latestQuestionnaire = feed.latestQuestionnaireForSubject(subject);

        if (latestQuestionnaire != null) {
            List<String> treatmentHistories = QuestionnaireExtraction.treatmentHistoriesCurrentTumor(latestQuestionnaire);
            return curation.toPriorTumorTreatments(treatmentHistories);
        } else {
            return Lists.newArrayList();
        }
    }

    @NotNull
    private static TumorDetails createTumorDetails() {
        return ImmutableTumorDetails.builder()
                .primaryTumorLocation(Strings.EMPTY)
                .primaryTumorSubLocation(Strings.EMPTY)
                .primaryTumorType(Strings.EMPTY)
                .primaryTumorSubType(Strings.EMPTY)
                .stage(Strings.EMPTY)
                .hasMeasurableLesionRecist(false)
                .hasBrainLesions(false)
                .hasActiveBrainLesions(false)
                .hasSymptomaticBrainLesions(false)
                .hasCnsLesions(false)
                .hasActiveCnsLesions(false)
                .hasSymptomaticCnsLesions(false)
                .hasBoneLesions(false)
                .hasLiverLesions(false)
                .hasOtherLesions(false)
                .build();
    }

    @NotNull
    private static ClinicalStatus createClinicalStatus() {
        return ImmutableClinicalStatus.builder()
                .who(0)
                .hasCurrentInfection(false)
                .hasSigAberrationLatestEcg(false)
                .cancerRelatedComplication(Strings.EMPTY)
                .build();
    }

    @NotNull
    private static String toSampleId(@NotNull String subject) {
        // Assume a single sample per patient
        return subject.replaceAll("-", "") + "T";
    }
}
