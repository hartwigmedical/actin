package com.hartwig.actin.clinical;

import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationModel;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.feed.FeedModel;
import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntry;
import com.hartwig.actin.clinical.feed.encounter.EncounterEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.lab.LabExtraction;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction;
import com.hartwig.actin.clinical.sort.LabValueComparator;
import com.hartwig.actin.clinical.sort.MedicationComparator;
import com.hartwig.actin.datamodel.ClinicalModel;
import com.hartwig.actin.datamodel.clinical.Allergy;
import com.hartwig.actin.datamodel.clinical.BloodPressure;
import com.hartwig.actin.datamodel.clinical.CancerRelatedComplication;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.clinical.ClinicalStatus;
import com.hartwig.actin.datamodel.clinical.ImmutableAllergy;
import com.hartwig.actin.datamodel.clinical.ImmutableBloodPressure;
import com.hartwig.actin.datamodel.clinical.ImmutableClinicalRecord;
import com.hartwig.actin.datamodel.clinical.ImmutableClinicalStatus;
import com.hartwig.actin.datamodel.clinical.ImmutableMedication;
import com.hartwig.actin.datamodel.clinical.ImmutablePatientDetails;
import com.hartwig.actin.datamodel.clinical.ImmutableSurgery;
import com.hartwig.actin.datamodel.clinical.ImmutableToxicity;
import com.hartwig.actin.datamodel.clinical.ImmutableTumorDetails;
import com.hartwig.actin.datamodel.clinical.LabValue;
import com.hartwig.actin.datamodel.clinical.Medication;
import com.hartwig.actin.datamodel.clinical.PatientDetails;
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition;
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary;
import com.hartwig.actin.datamodel.clinical.PriorTumorTreatment;
import com.hartwig.actin.datamodel.clinical.Surgery;
import com.hartwig.actin.datamodel.clinical.Toxicity;
import com.hartwig.actin.datamodel.clinical.ToxicitySource;
import com.hartwig.actin.datamodel.clinical.TumorDetails;

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
        List<ClinicalRecord> records = Lists.newArrayList();
        LOGGER.info("Creating clinical model");
        for (String subject : feed.subjects()) {
            String sampleId = toSampleId(subject);
            LOGGER.info(" Extracting data for sample {}", sampleId);

            Questionnaire questionnaire = QuestionnaireExtraction.extract(feed.latestQuestionnaireEntry(subject));

            records.add(ImmutableClinicalRecord.builder()
                    .sampleId(sampleId)
                    .patient(extractPatientDetails(subject, questionnaire))
                    .tumor(extractTumorDetails(questionnaire))
                    .clinicalStatus(extractClinicalStatus(questionnaire))
                    .priorTumorTreatments(extractPriorTumorTreatments(questionnaire))
                    .priorSecondPrimaries(extractPriorSecondPrimaries(questionnaire))
                    .priorOtherConditions(extractPriorOtherConditions(questionnaire))
                    .cancerRelatedComplications(extractCancerRelatedComplications(questionnaire))
                    .labValues(extractLabValues(subject))
                    .toxicities(extractToxicities(subject, questionnaire))
                    .allergies(extractAllergies(subject))
                    .surgeries(extractSurgeries(subject))
                    .bloodPressures(extractBloodPressures(subject))
                    .medications(extractMedications(subject))
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
    private PatientDetails extractPatientDetails(@NotNull String subject, @Nullable Questionnaire questionnaire) {
        PatientEntry patient = feed.patientEntry(subject);

        return ImmutablePatientDetails.builder()
                .sex(patient.sex())
                .birthYear(patient.birthYear())
                .registrationDate(patient.periodStart())
                .questionnaireDate(questionnaire != null ? questionnaire.date() : null)
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
    private List<PriorTumorTreatment> extractPriorTumorTreatments(@Nullable Questionnaire questionnaire) {
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        if (questionnaire != null) {
            List<String> treatmentHistories = questionnaire.treatmentHistoryCurrentTumor();
            priorTumorTreatments.addAll(curation.curatePriorTumorTreatments(treatmentHistories));

            List<String> otherOncologicalHistories = questionnaire.otherOncologicalHistory();
            priorTumorTreatments.addAll(curation.curatePriorTumorTreatments(otherOncologicalHistories));
        }
        return priorTumorTreatments;
    }

    @NotNull
    private List<PriorSecondPrimary> extractPriorSecondPrimaries(@Nullable Questionnaire questionnaire) {
        if (questionnaire != null) {
            List<String> otherOncologicalHistories = questionnaire.otherOncologicalHistory();
            return curation.curatePriorSecondPrimaries(otherOncologicalHistories);
        } else {
            return Lists.newArrayList();
        }
    }

    @NotNull
    private List<PriorOtherCondition> extractPriorOtherConditions(@Nullable Questionnaire questionnaire) {
        if (questionnaire != null) {
            List<String> nonOncologicalHistories = questionnaire.nonOncologicalHistory();
            return curation.curatePriorOtherConditions(nonOncologicalHistories);
        } else {
            return Lists.newArrayList();
        }
    }

    @NotNull
    private List<CancerRelatedComplication> extractCancerRelatedComplications(@Nullable Questionnaire questionnaire) {
        if (questionnaire != null) {
            List<String> cancerRelatedComplications = questionnaire.cancerRelatedComplications();
            return curation.curateCancerRelatedComplications(cancerRelatedComplications);
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

        values.sort(new LabValueComparator());

        return values;
    }

    @NotNull
    private List<Toxicity> extractToxicities(@NotNull String subject, @Nullable Questionnaire questionnaire) {
        List<Toxicity> toxicities = Lists.newArrayList();
        if (questionnaire != null) {
            List<String> unresolvedToxicities = questionnaire.unresolvedToxicities();
            toxicities.addAll(curation.curateQuestionnaireToxicities(unresolvedToxicities, questionnaire.date()));
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
            allergies.add(curation.translateAllergy(ImmutableAllergy.builder()
                    .name(CurationUtil.capitalizeFirstLetterOnly(entry.codeText()))
                    .category(CurationUtil.capitalizeFirstLetterOnly(entry.category()))
                    .criticality(CurationUtil.capitalizeFirstLetterOnly(entry.criticality()))
                    .build()));
        }
        return allergies;
    }

    @NotNull
    private List<Surgery> extractSurgeries(@NotNull String subject) {
        List<Surgery> surgeries = Lists.newArrayList();
        for (EncounterEntry entry : feed.uniqueEncounterEntries(subject)) {
            surgeries.add(ImmutableSurgery.builder().endDate(entry.periodEnd()).build());
        }
        return surgeries;
    }

    @NotNull
    private List<BloodPressure> extractBloodPressures(@NotNull String subject) {
        List<BloodPressure> bloodPressures = Lists.newArrayList();
        for (BloodPressureEntry entry : feed.bloodPressureEntries(subject)) {
            bloodPressures.add(ImmutableBloodPressure.builder()
                    .date(entry.effectiveDateTime())
                    .category(entry.componentCodeDisplay())
                    .value(entry.componentValueQuantityValue())
                    .unit(entry.componentValueQuantityCode())
                    .build());
        }
        return bloodPressures;
    }

    @NotNull
    private List<Medication> extractMedications(@NotNull String subject) {
        List<Medication> medications = Lists.newArrayList();

        for (MedicationEntry entry : feed.medicationEntries(subject)) {
            Medication dosageCurated = curation.curateMedicationDosage(entry.dosageInstructionText());

            ImmutableMedication.Builder builder = ImmutableMedication.builder();
            if (dosageCurated != null) {
                builder = builder.from(dosageCurated);
            }

            String name = CurationUtil.capitalizeFirstLetterOnly(entry.code5ATCDisplay());
            if (!name.isEmpty() && !name.equalsIgnoreCase("null")) {
                Medication medication = builder.name(name)
                        .type(Strings.EMPTY)
                        .startDate(entry.periodOfUseValuePeriodStart())
                        .stopDate(entry.periodOfUseValuePeriodEnd())
                        .active(entry.active())
                        .build();

                medications.add(curation.annotateWithMedicationType(medication));
            }
        }

        medications.sort(new MedicationComparator());

        return medications;
    }
}
