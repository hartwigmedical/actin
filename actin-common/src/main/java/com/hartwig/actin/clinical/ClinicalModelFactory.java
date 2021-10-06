package com.hartwig.actin.clinical;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationModel;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableAllergy;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.feed.FeedModel;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.lab.LabExtraction;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

            QuestionnaireEntry entry = feed.latestQuestionnaireEntry(subject);
            Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

            records.add(ImmutableClinicalRecord.builder()
                    .sampleId(sampleId)
                    .patient(extractPatientDetails(subject, entry))
                    .tumor(extractTumorDetails(questionnaire))
                    .clinicalStatus(extractClinicalStatus(questionnaire))
                    .cancerRelatedComplications(extractCancerRelatedComplications(questionnaire))
                    .priorTumorTreatments(extractPriorTumorTreatments(questionnaire))
                    .priorSecondPrimaries(extractPriorSecondPrimaries(questionnaire))
                    .priorOtherConditions(extractPriorOtherConditions(questionnaire))
                    .labValues(extractLabValues(subject))
                    .toxicities(extractToxicities(subject, questionnaire, entry != null ? entry.authoredDateTime() : null))
                    .allergies(extractAllergies(subject))
                    .build());
        }

        LOGGER.info("Evaluating curation database");
        curation.evaluate();

        return new ClinicalModel(records);
    }

    @NotNull
    private static String toSampleId(@NotNull String subject) {
        // Assume a single sample per patient ending with "T". No "TII" supported yet.
        return subject.replaceAll("-", "") + "T";
    }

    @NotNull
    private PatientDetails extractPatientDetails(@NotNull String subject, @Nullable QuestionnaireEntry entry) {
        PatientEntry patient = feed.patientEntry(subject);

        return ImmutablePatientDetails.builder()
                .sex(patient.sex())
                .birthYear(patient.birthYear())
                .registrationDate(patient.periodStart())
                .questionnaireDate(entry != null ? entry.authoredDateTime() : null)
                .build();
    }

    @NotNull
    private TumorDetails extractTumorDetails(@Nullable Questionnaire questionnaire) {
        if (questionnaire == null) {
            return ImmutableTumorDetails.builder().build();
        }

        List<String> curatedOtherLesions = null;
        if (questionnaire.otherLesions() != null) {
            curatedOtherLesions = Lists.newArrayList();
            for (String lesion : questionnaire.otherLesions()) {
                curatedOtherLesions.add(curation.curateLesionLocation(lesion));
            }
        }

        return ImmutableTumorDetails.builder()
                .from(curation.curateTumorDetails(questionnaire.tumorLocation(), questionnaire.tumorType()))
                .biopsyLocation(curation.curateLesionLocation(questionnaire.biopsyLocation()))
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
                .hasOtherLesions(curatedOtherLesions != null ? !curatedOtherLesions.isEmpty() : null)
                .otherLesions(curatedOtherLesions)
                .build();
    }

    @NotNull
    private ClinicalStatus extractClinicalStatus(@Nullable Questionnaire questionnaire) {
        if (questionnaire == null) {
            return ImmutableClinicalStatus.builder().build();

        }
        return ImmutableClinicalStatus.builder()
                .who(questionnaire.whoStatus())
                .hasActiveInfection(questionnaire.hasSignificantCurrentInfection())
                .hasSigAberrationLatestEcg(questionnaire.hasSignificantAberrationLatestECG())
                .ecgAberrationDescription(curation.curateAberrationECG(questionnaire.significantAberrationLatestECG()))
                .build();
    }

    @NotNull
    private List<CancerRelatedComplication> extractCancerRelatedComplications(@Nullable Questionnaire questionnaire) {
        List<CancerRelatedComplication> cancerRelatedComplications = Lists.newArrayList();
        if (questionnaire != null) {
            cancerRelatedComplications.addAll(curation.curateCancerRelatedComplications(questionnaire.cancerRelatedComplications()));
        }
        return cancerRelatedComplications;
    }

    @NotNull
    private List<PriorTumorTreatment> extractPriorTumorTreatments(@Nullable Questionnaire questionnaire) {
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        if (questionnaire != null) {
            List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
            priorTumorTreatments.addAll(curation.curatePriorTumorTreatments(treatmentHistory));

            List<String> otherOncologyHistory = questionnaire.otherOncologicalHistory();
            priorTumorTreatments.addAll(curation.curatePriorTumorTreatments(otherOncologyHistory));
        }
        return priorTumorTreatments;
    }

    @NotNull
    private List<PriorSecondPrimary> extractPriorSecondPrimaries(@Nullable Questionnaire questionnaire) {
        if (questionnaire != null) {
            List<String> otherOncologyHistory = questionnaire.otherOncologicalHistory();
            return curation.curatePriorSecondPrimaries(otherOncologyHistory);
        } else {
            return Lists.newArrayList();
        }
    }

    @NotNull
    private List<PriorOtherCondition> extractPriorOtherConditions(@Nullable Questionnaire questionnaire) {
        if (questionnaire != null) {
            List<String> nonOncologyHistory = questionnaire.nonOncologicalHistory();
            return curation.curatePriorOtherConditions(nonOncologyHistory);
        } else {
            return Lists.newArrayList();
        }
    }

    @NotNull
    private List<LabValue> extractLabValues(@NotNull String subject) {
        List<LabValue> values = Lists.newArrayList();
        for (LabEntry entry : feed.labEntries(subject)) {
            values.add(curation.translateLabValue(LabExtraction.extract(entry)));
        }
        return values;
    }

    @NotNull
    private List<Toxicity> extractToxicities(@NotNull String subject, @Nullable Questionnaire questionnaire,
            @Nullable LocalDate questionnaireDate) {
        List<Toxicity> toxicities = Lists.newArrayList();
        if (questionnaire != null && questionnaireDate != null) {
            List<String> unresolvedToxicities = questionnaire.unresolvedToxicities();
            toxicities.addAll(curation.curateQuestionnaireToxicities(questionnaireDate, unresolvedToxicities));
        }

        List<QuestionnaireEntry> toxicityQuestionnaires = feed.toxicityQuestionnaireEntries(subject);
        for (QuestionnaireEntry entry : toxicityQuestionnaires) {
            Integer grade = CurationUtil.parseOptionalInteger(entry.itemAnswerValueValueString());
            if (grade != null) {
                toxicities.add(ImmutableToxicity.builder()
                        .name(entry.itemText())
                        .evaluatedDate(entry.authoredDateTime())
                        .source(ToxicitySource.EHR)
                        .grade(grade)
                        .build());
            }
        }

        return toxicities;
    }

    @NotNull
    private List<Allergy> extractAllergies(@NotNull String subject) {
        List<Allergy> allergies = Lists.newArrayList();
        for (IntoleranceEntry entry : feed.intoleranceEntries(subject)) {
            allergies.add(ImmutableAllergy.builder()
                    .name(entry.codeText())
                    .category(entry.category())
                    .criticality(entry.criticality())
                    .build());
        }
        return allergies;
    }
}
