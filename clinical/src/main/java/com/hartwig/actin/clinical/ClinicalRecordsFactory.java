package com.hartwig.actin.clinical;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.curation.CurationModel;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableSurgeryHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableSurgicalTreatment;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicityEvaluation;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.SurgeryStatus;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicityEvaluation;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.feed.FeedModel;
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry;
import com.hartwig.actin.clinical.feed.lab.LabExtraction;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction;
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionExtraction;
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator;
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator;
import com.hartwig.actin.clinical.sort.MedicationByNameComparator;

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

    public ClinicalRecordsFactory(@NotNull FeedModel feed, @NotNull CurationModel curation) {
        this.feed = feed;
        this.curation = curation;
    }

    @NotNull
    List<ClinicalRecord> create() {
        List<ClinicalRecord> records = Lists.newArrayList();
        Set<String> processedPatientIds = new HashSet<>();
        QuestionnaireExtraction extraction = new QuestionnaireExtraction(curation.questionnaireRawEntryMapper());

        LOGGER.info("Creating clinical model");
        for (String subject : feed.subjects()) {
            String patientId = toPatientId(subject);
            if (processedPatientIds.contains(patientId)) {
                throw new IllegalStateException("Cannot create clinical records. Duplicate patientId: " + patientId);
            }
            processedPatientIds.add(patientId);

            LOGGER.info(" Extracting data for patient {}", patientId);

            Questionnaire questionnaire = extraction.extract(feed.latestQuestionnaireEntry(subject));

            List<Toxicity> extractedToxicities = extractToxicities(subject, questionnaire);
            List<ToxicityEvaluation> toxicityEvaluations = extractedToxicities.stream()
                    .map(toxicity -> ImmutableToxicityEvaluation.builder()
                            .toxicities(Collections.singleton(ImmutableObservedToxicity.builder()
                                    .name(toxicity.name())
                                    .addAllCategories(toxicity.categories())
                                    .grade(toxicity.grade())
                                    .build()))
                            .evaluatedDate(toxicity.evaluatedDate())
                            .source(toxicity.source())
                            .build())
                    .collect(Collectors.toList());

            records.add(ImmutableClinicalRecord.builder()
                    .patientId(patientId)
                    .patient(extractPatientDetails(subject, questionnaire))
                    .tumor(extractTumorDetails(questionnaire))
                    .clinicalStatus(extractClinicalStatus(questionnaire))
                    .treatmentHistory(extractTreatmentHistory(questionnaire))
                    .priorTumorTreatments(extractPriorTumorTreatments(questionnaire))
                    .priorSecondPrimaries(extractPriorSecondPrimaries(questionnaire))
                    .priorOtherConditions(extractPriorOtherConditions(questionnaire))
                    .priorMolecularTests(extractPriorMolecularTests(questionnaire))
                    .complications(extractComplications(questionnaire))
                    .labValues(extractLabValues(subject))
                    .toxicities(extractedToxicities)
                    .toxicityEvaluations(toxicityEvaluations)
                    .intolerances(extractIntolerances(subject))
                    .surgeries(extractSurgeries(subject))
                    .surgicalTreatments(extractSurgicalTreatments(subject))
                    .bodyWeights(extractBodyWeights(subject))
                    .vitalFunctions(extractVitalFunctions(subject))
                    .bloodTransfusions(extractBloodTransfusions(subject))
                    .medications(extractMedications(subject))
                    .build());
        }

        records.sort(new ClinicalRecordComparator());
        LOGGER.info("Evaluating curation database");
        curation.evaluate();

        return records;
    }

    @NotNull
    @VisibleForTesting
    static String toPatientId(@NotNull String subject) {
        String adjusted = subject;
        // Subjects have been passed with unexpected subject IDs in the past (e.g. without ACTN prefix)
        if (subject.length() == 10 && !subject.startsWith("ACTN")) {
            LOGGER.warn("Suspicious subject detected. Pre-fixing with 'ACTN': {}", subject);
            adjusted = "ACTN" + subject;
        }

        return adjusted.replaceAll("-", "");
    }

    @NotNull
    private PatientDetails extractPatientDetails(@NotNull String subject, @Nullable Questionnaire questionnaire) {
        PatientEntry patient = feed.patientEntry(subject);

        return ImmutablePatientDetails.builder()
                .gender(patient.gender())
                .birthYear(patient.birthYear())
                .registrationDate(patient.periodStart())
                .questionnaireDate(questionnaire != null ? questionnaire.date() : null)
                .otherMolecularPatientId(questionnaire != null ? questionnaire.genayaSubjectNumber() : null)
                .build();
    }

    @NotNull
    private TumorDetails extractTumorDetails(@Nullable Questionnaire questionnaire) {
        if (questionnaire == null) {
            return ImmutableTumorDetails.builder().build();
        }

        String biopsyLocation = questionnaire.biopsyLocation();
        List<String> otherLesions = questionnaire.otherLesions();
        List<String> curatedOtherLesions = curation.curateOtherLesions(otherLesions);

        TumorDetails tumorDetails = ImmutableTumorDetails.builder()
                .from(curation.curateTumorDetails(questionnaire.tumorLocation(), questionnaire.tumorType()))
                .biopsyLocation(curation.curateBiopsyLocation(biopsyLocation))
                .stage(questionnaire.stage())
                .hasMeasurableDisease(questionnaire.hasMeasurableDisease())
                .hasBrainLesions(questionnaire.hasBrainLesions())
                .hasActiveBrainLesions(questionnaire.hasActiveBrainLesions())
                .hasCnsLesions(questionnaire.hasCnsLesions())
                .hasActiveCnsLesions(questionnaire.hasActiveCnsLesions())
                .hasBoneLesions(questionnaire.hasBoneLesions())
                .hasLiverLesions(questionnaire.hasLiverLesions())
                .otherLesions(curatedOtherLesions)
                .build();

        return curation.overrideKnownLesionLocations(tumorDetails, biopsyLocation, otherLesions);
    }

    @NotNull
    private ClinicalStatus extractClinicalStatus(@Nullable Questionnaire questionnaire) {
        return (questionnaire == null)
                ? ImmutableClinicalStatus.builder().build()
                : ImmutableClinicalStatus.builder()
                        .who(questionnaire.whoStatus())
                        .infectionStatus(curation.curateInfectionStatus(questionnaire.infectionStatus()))
                        .ecg(curation.curateECG(questionnaire.ecg()))
                        .lvef(curation.determineLVEF(questionnaire.nonOncologicalHistory()))
                        .hasComplications(Optional.ofNullable(extractComplications(questionnaire))
                                .map(complications -> !complications.isEmpty())
                                .orElse(null))
                        .build();
    }

    @NotNull
    private List<TreatmentHistoryEntry> extractTreatmentHistory(@Nullable Questionnaire questionnaire) {
        if (questionnaire == null) {
            return Collections.emptyList();
        }
        List<String> fullHistoryInput = Stream.of(questionnaire.treatmentHistoryCurrentTumor(), questionnaire.otherOncologicalHistory())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return curation.curateTreatmentHistory(fullHistoryInput);
    }

    @NotNull
    private List<PriorTumorTreatment> extractPriorTumorTreatments(@Nullable Questionnaire questionnaire) {
        return (questionnaire == null)
                ? Collections.emptyList()
                : Stream.of(questionnaire.treatmentHistoryCurrentTumor(), questionnaire.otherOncologicalHistory())
                        .flatMap(entry -> curation.curatePriorTumorTreatments(entry).stream())
                        .collect(Collectors.toList());
    }

    @NotNull
    private List<PriorSecondPrimary> extractPriorSecondPrimaries(@Nullable Questionnaire questionnaire) {
        return (questionnaire == null)
                ? Collections.emptyList()
                : Stream.of(questionnaire.otherOncologicalHistory(), questionnaire.secondaryPrimaries())
                        .flatMap(entry -> curation.curatePriorSecondPrimaries(entry).stream())
                        .collect(Collectors.toList());
    }

    @NotNull
    private List<PriorOtherCondition> extractPriorOtherConditions(@Nullable Questionnaire questionnaire) {
        return (questionnaire == null)
                ? Collections.emptyList()
                : curation.curatePriorOtherConditions(questionnaire.nonOncologicalHistory());
    }

    @NotNull
    private List<PriorMolecularTest> extractPriorMolecularTests(@Nullable Questionnaire questionnaire) {
        return (questionnaire == null)
                ? Collections.emptyList()
                : Stream.of(Maps.immutableEntry("IHC", questionnaire.ihcTestResults()),
                                Maps.immutableEntry("PD-L1", questionnaire.pdl1TestResults()))
                        .flatMap(entry -> curation.curatePriorMolecularTests(entry.getKey(), entry.getValue()).stream())
                        .collect(Collectors.toList());
    }

    @Nullable
    private List<Complication> extractComplications(@Nullable Questionnaire questionnaire) {
        return (questionnaire != null) ? curation.curateComplications(questionnaire.complications()) : null;
    }

    @NotNull
    private List<LabValue> extractLabValues(@NotNull String subject) {
        return feed.labEntries(subject)
                .stream()
                .map(LabExtraction::extract)
                .map(curation::translateLabValue)
                .sorted(new LabValueDescendingDateComparator())
                .collect(Collectors.toList());
    }

    @NotNull
    private List<Toxicity> extractToxicities(@NotNull String subject, @Nullable Questionnaire questionnaire) {
        List<Toxicity> toxicities = feed.toxicityEntries(subject).stream().flatMap(toxicityEntry -> {
            Integer grade = extractGrade(toxicityEntry);
            if (grade != null) {
                return Stream.of(ImmutableToxicity.builder()
                        .name(toxicityEntry.itemText())
                        .evaluatedDate(toxicityEntry.authored())
                        .source(ToxicitySource.EHR)
                        .grade(grade)
                        .build());
            } else {
                return Stream.empty();
            }
        }).map(curation::translateToxicity).collect(Collectors.toList());

        if (questionnaire != null) {
            toxicities.addAll(curation.curateQuestionnaireToxicities(questionnaire.unresolvedToxicities(), questionnaire.date()));
        }

        return toxicities;
    }

    @Nullable
    private static Integer extractGrade(@NotNull DigitalFileEntry entry) {
        String value = entry.itemAnswerValueValueString();
        if (value.isEmpty()) {
            return null;
        }

        String curated;
        int notApplicableIndex = value.indexOf(". Not applicable");
        if (notApplicableIndex > 0) {
            curated = value.substring(0, notApplicableIndex);
        } else {
            curated = value;
        }
        return Integer.valueOf(curated);
    }

    @NotNull
    private List<Intolerance> extractIntolerances(@NotNull String subject) {
        return feed.intoleranceEntries(subject)
                .stream()
                .map(entry -> ImmutableIntolerance.builder()
                        .name(CurationUtil.capitalizeFirstLetterOnly(entry.codeText()))
                        .category(CurationUtil.capitalizeFirstLetterOnly(entry.category()))
                        .type(CurationUtil.capitalizeFirstLetterOnly(entry.isSideEffect()))
                        .clinicalStatus(CurationUtil.capitalizeFirstLetterOnly(entry.clinicalStatus()))
                        .verificationStatus(CurationUtil.capitalizeFirstLetterOnly(entry.verificationStatus()))
                        .criticality(CurationUtil.capitalizeFirstLetterOnly(entry.criticality()))
                        .build())
                .map(curation::curateIntolerance)
                .collect(Collectors.toList());
    }

    @NotNull
    private List<Surgery> extractSurgeries(@NotNull String subject) {
        return feed.uniqueSurgeryEntries(subject)
                .stream()
                .map(entry -> ImmutableSurgery.builder()
                        .endDate(entry.periodEnd())
                        .status(resolveSurgeryStatus(entry.encounterStatus()))
                        .build())
                .collect(Collectors.toList());
    }

    @NotNull
    private List<TreatmentHistoryEntry> extractSurgicalTreatments(@NotNull String subject) {
        return feed.uniqueSurgeryEntries(subject)
                .stream()
                .map(encounterEntry -> ImmutableTreatmentHistoryEntry.builder()
                        .treatments(Collections.singleton(ImmutableSurgicalTreatment.builder().name("extracted surgery").build()))
                        .surgeryHistoryDetails(ImmutableSurgeryHistoryDetails.builder()
                                .endDate(encounterEntry.periodEnd())
                                .status(resolveSurgeryStatus(encounterEntry.encounterStatus()))
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    @NotNull
    private static SurgeryStatus resolveSurgeryStatus(@NotNull String status) {
        String valueToFind = status.trim().replaceAll("-", "_");
        for (SurgeryStatus option : SurgeryStatus.values()) {
            if (option.toString().equalsIgnoreCase(valueToFind)) {
                return option;
            }
        }

        LOGGER.warn("Could not resolve surgery status '{}'", status);
        return SurgeryStatus.UNKNOWN;
    }

    @NotNull
    private List<BodyWeight> extractBodyWeights(@NotNull String subject) {
        return feed.uniqueBodyWeightEntries(subject)
                .stream()
                .map(entry -> ImmutableBodyWeight.builder()
                        .date(entry.effectiveDateTime())
                        .value(entry.valueQuantityValue())
                        .unit(entry.valueQuantityUnit())
                        .build())
                .collect(Collectors.toList());
    }

    @NotNull
    private List<VitalFunction> extractVitalFunctions(@NotNull String subject) {
        return feed.vitalFunctionEntries(subject)
                .stream()
                .map(entry -> ImmutableVitalFunction.builder()
                        .date(entry.effectiveDateTime())
                        .category(VitalFunctionExtraction.determineCategory(entry.codeDisplayOriginal()))
                        .subcategory(entry.componentCodeDisplay())
                        .value(entry.quantityValue())
                        .unit(entry.quantityUnit())
                        .build())
                .collect(Collectors.toList());
    }

    @NotNull
    private List<BloodTransfusion> extractBloodTransfusions(@NotNull String subject) {
        return feed.bloodTransfusionEntries(subject)
                .stream()
                .map(entry -> ImmutableBloodTransfusion.builder()
                        .date(entry.authored())
                        .product(entry.itemAnswerValueValueString())
                        .build())
                .map(curation::translateBloodTransfusion)
                .collect(Collectors.toList());
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
            if (name.isEmpty()) {
                String input = CurationUtil.capitalizeFirstLetterOnly(entry.codeText());
                name = curation.curateMedicationName(input);
            }

            if (name != null && !name.isEmpty()) {
                Medication medication = builder.name(name)
                        .codeATC(curation.curateMedicationCodeATC(entry.code5ATCCode()))
                        .chemicalSubgroupAtc(entry.chemicalSubgroupDisplay())
                        .pharmacologicalSubgroupAtc(entry.pharmacologicalSubgroupDisplay())
                        .therapeuticSubgroupAtc(entry.therapeuticSubgroupDisplay())
                        .anatomicalMainGroupAtc(entry.anatomicalMainGroupDisplay())
                        .status(curation.curateMedicationStatus(entry.status()))
                        .administrationRoute(curation.translateAdministrationRoute(entry.dosageInstructionRouteDisplay()))
                        .startDate(entry.periodOfUseValuePeriodStart())
                        .stopDate(entry.periodOfUseValuePeriodEnd())
                        .build();

                medications.add(curation.annotateWithMedicationCategory(medication));
            }
        }

        medications.sort(new MedicationByNameComparator());

        return medications;
    }
}
