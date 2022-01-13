package com.hartwig.actin.clinical;

import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationModel;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableAllergy;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.feed.FeedModel;
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry;
import com.hartwig.actin.clinical.feed.encounter.EncounterEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.lab.LabExtraction;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction;
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry;
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionExtraction;
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator;
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator;
import com.hartwig.actin.clinical.sort.MedicationByNameComparator;
import com.hartwig.actin.util.ResourceFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClinicalRecordsFactory {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalRecordsFactory.class);

    @NotNull
    private final FeedModel feed;
    @NotNull
    private final CurationModel curation;

    @NotNull
    public static List<ClinicalRecord> fromFeedAndCurationDirectories(@NotNull String clinicalFeedDirectory,
            @NotNull String clinicalCurationDirectory) throws IOException {
        return new ClinicalRecordsFactory(FeedModel.fromFeedDirectory(clinicalFeedDirectory),
                CurationModel.fromCurationDirectory(clinicalCurationDirectory)).create();
    }

    @VisibleForTesting
    ClinicalRecordsFactory(@NotNull final FeedModel feed, @NotNull final CurationModel curation) {
        this.feed = feed;
        this.curation = curation;
    }

    @NotNull
    @VisibleForTesting
    List<ClinicalRecord> create() {
        List<ClinicalRecord> records = Lists.newArrayList();
        LOGGER.info("Creating clinical model");
        for (String subject : feed.subjects()) {
            String sampleId = toSampleId(subject);
            LOGGER.info(" Extracting data for sample {}", sampleId);

            Questionnaire questionnaire = QuestionnaireExtraction.extract(feed.latestQuestionnaireEntry(subject));

            if (containsSampleId(records, sampleId)) {
                throw new IllegalStateException("Cannot create clinical records. Duplicate sampleId: " + sampleId);
            }

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
                    .bodyWeights(extractBodyWeights(subject))
                    .vitalFunctions(extractVitalFunctions(subject))
                    .medications(extractMedications(subject))
                    .build());
        }

        records.sort(new ClinicalRecordComparator());
        LOGGER.info("Evaluating curation database");
        curation.evaluate();

        return records;
    }

    private static boolean containsSampleId(@NotNull List<ClinicalRecord> records, @NotNull String sampleId) {
        for (ClinicalRecord record : records) {
            if (record.sampleId().equals(sampleId)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    @VisibleForTesting
    static String toSampleId(@NotNull String subject) {
        String adjusted = subject;
        // Subjects have been passed with unexpected subject IDs in the past (eg without ACTN prefix)
        if (subject.length() == 10 && !subject.startsWith("ACTN")) {
            LOGGER.warn("Suspicious subject detected: {}", subject);
            adjusted = "ACTN" + subject;
        }

        // Assume a single sample per patient ending with "T". No "TII" supported yet.
        return adjusted.replaceAll("-", "") + "T";
    }

    @NotNull
    private PatientDetails extractPatientDetails(@NotNull String subject, @Nullable Questionnaire questionnaire) {
        PatientEntry patient = feed.patientEntry(subject);

        return ImmutablePatientDetails.builder()
                .gender(patient.gender())
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

        List<String> otherLesions = questionnaire.otherLesions();
        List<String> curatedOtherLesions = curation.curateOtherLesions(otherLesions);

        TumorDetails tumorDetails = ImmutableTumorDetails.builder()
                .from(curation.curateTumorDetails(questionnaire.tumorLocation(), questionnaire.tumorType()))
                .biopsyLocation(curation.curateBiopsyLocation(questionnaire.biopsyLocation()))
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

        return curation.evaluateKnownLesionLocations(tumorDetails, otherLesions);
    }

    @NotNull
    private ClinicalStatus extractClinicalStatus(@Nullable Questionnaire questionnaire) {
        if (questionnaire == null) {
            return ImmutableClinicalStatus.builder().build();
        }

        return ImmutableClinicalStatus.builder()
                .who(questionnaire.whoStatus())
                .infectionStatus(curation.curateInfectionStatus(questionnaire.infectionStatus()))
                .ecgAberration(curation.curateECGAberration(questionnaire.ecgAberration()))
                .lvef(curation.determineLVEF(questionnaire.nonOncologicalHistory()))
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

        values.sort(new LabValueDescendingDateComparator());

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
            Integer grade = ResourceFile.optionalInteger(entry.itemAnswerValueValueString());
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
                    .clinicalStatus(CurationUtil.capitalizeFirstLetterOnly(entry.clinicalStatus()))
                    .verificationStatus(CurationUtil.capitalizeFirstLetterOnly(entry.verificationStatus()))
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
    private List<BodyWeight> extractBodyWeights(@NotNull String subject) {
        List<BodyWeight> bodyWeights = Lists.newArrayList();
        for (BodyWeightEntry entry : feed.uniqueBodyWeightEntries(subject)) {
            double value = entry.valueQuantityValue();
            // A body weight of 0 can be assumed to be erroneous entry.
            if (value > 0) {
                bodyWeights.add(ImmutableBodyWeight.builder()
                        .date(entry.effectiveDateTime())
                        .value(value)
                        .unit(entry.valueQuantityUnit())
                        .build());
            }
        }
        return bodyWeights;
    }

    @NotNull
    private List<VitalFunction> extractVitalFunctions(@NotNull String subject) {
        List<VitalFunction> vitalFunctions = Lists.newArrayList();
        for (VitalFunctionEntry entry : feed.vitalFunctionEntries(subject)) {
            vitalFunctions.add(ImmutableVitalFunction.builder()
                    .date(entry.effectiveDateTime())
                    .category(VitalFunctionExtraction.determineCategory(entry.codeDisplayOriginal()))
                    .subcategory(entry.componentCodeDisplay())
                    .value(entry.quantityValue())
                    .unit(entry.quantityUnit())
                    .build());
        }
        return vitalFunctions;
    }

    @NotNull
    private List<Medication> extractMedications(@NotNull String subject) {
        List<Medication> medications = Lists.newArrayList();

        for (MedicationEntry entry : feed.medicationEntries(subject)) {
            Medication dosageCurated = curation.curateMedicationDosage(entry.dosageInstructionText());

            ImmutableMedication.Builder builder = ImmutableMedication.builder();
            if (dosageCurated != null) {
                builder.from(dosageCurated);
            }

            String name = CurationUtil.capitalizeFirstLetterOnly(entry.code5ATCDisplay());
            if (!name.isEmpty()) {
                Medication medication = builder.name(name)
                        .startDate(entry.periodOfUseValuePeriodStart())
                        .stopDate(entry.periodOfUseValuePeriodEnd())
                        .active(entry.active())
                        .build();

                medications.add(curation.annotateWithMedicationCategory(medication));
            }
        }

        medications.sort(new MedicationByNameComparator());

        return medications;
    }
}
