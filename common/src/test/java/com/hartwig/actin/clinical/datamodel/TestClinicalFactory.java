package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestClinicalFactory {

    private static final LocalDate TODAY = LocalDate.now();

    private static final int DAYS_SINCE_QUESTIONNAIRE = 10;
    private static final int DAYS_SINCE_REGISTRATION = 15;
    private static final int DAYS_SINCE_LAB_MEASUREMENT_1 = 30;
    private static final int DAYS_SINCE_LAB_MEASUREMENT_2 = 20;
    private static final int DAYS_SINCE_LAB_MEASUREMENT_3 = 10;
    private static final int DAYS_SINCE_TOXICITIES = 30;
    private static final int DAYS_SINCE_SURGERY = 30;
    private static final int DAYS_SINCE_BODY_WEIGHT_1 = 12;
    private static final int DAYS_SINCE_BODY_WEIGHT_2 = 18;
    private static final int DAYS_SINCE_BLOOD_PRESSURE = 15;
    private static final int DAYS_SINCE_BLOOD_TRANSFUSION = 15;
    private static final int DAYS_SINCE_MEDICATION_START = 30;
    private static final int DAYS_UNTIL_MEDICATION_END = 15;
    private static final int YEARS_SINCE_TREATMENT_LINE_1 = 2;
    private static final int YEARS_SINCE_TREATMENT_LINE_2 = 1;
    private static final int YEARS_SINCE_TREATMENT_LINE_3 = 0;
    private static final int YEARS_SINCE_SECOND_PRIMARY_DIAGNOSIS = 3;

    private TestClinicalFactory() {
    }

    @NotNull
    public static ClinicalRecord createMinimalTestClinicalRecord() {
        return ImmutableClinicalRecord.builder()
                .patientId(TestDataFactory.TEST_PATIENT)
                .patient(createTestPatientDetails())
                .tumor(ImmutableTumorDetails.builder().build())
                .clinicalStatus(ImmutableClinicalStatus.builder().build())
                .build();
    }

    @NotNull
    public static ClinicalRecord createProperTestClinicalRecord() {
        return ImmutableClinicalRecord.builder()
                .from(createMinimalTestClinicalRecord())
                .tumor(createTestTumorDetails())
                .clinicalStatus(createTestClinicalStatus())
                .priorTumorTreatments(createTestPriorTumorTreatments())
                .priorSecondPrimaries(createTestPriorSecondPrimaries())
                .priorOtherConditions(createTestPriorOtherConditions())
                .priorMolecularTests(createTestPriorMolecularTests())
                .complications(createTestComplications())
                .labValues(createTestLabValues())
                .toxicityEvaluations(createTestToxicityEvaluations())
                .toxicities(createTestToxicities())
                .intolerances(createTestIntolerances())
                .surgeries(createTestSurgeries())
                .surgicalTreatments(createTestSurgicalHistory())
                .bodyWeights(createTestBodyWeights())
                .vitalFunctions(createTestVitalFunctions())
                .bloodTransfusions(createTestBloodTransfusions())
                .medications(createTestMedications())
                .build();
    }

    @NotNull
    private static PatientDetails createTestPatientDetails() {
        return ImmutablePatientDetails.builder()
                .gender(Gender.MALE)
                .birthYear(1950)
                .registrationDate(TODAY.minusDays(DAYS_SINCE_REGISTRATION))
                .questionnaireDate(TODAY.minusDays(DAYS_SINCE_QUESTIONNAIRE))
                .build();
    }

    @NotNull
    private static TumorDetails createTestTumorDetails() {
        return ImmutableTumorDetails.builder()
                .primaryTumorLocation("Skin")
                .primaryTumorSubLocation(Strings.EMPTY)
                .primaryTumorType("Melanoma")
                .primaryTumorSubType(Strings.EMPTY)
                .primaryTumorExtraDetails(Strings.EMPTY)
                .addDoids("8923")
                .stage(TumorStage.IV)
                .hasMeasurableDisease(true)
                .hasBrainLesions(false)
                .hasActiveBrainLesions(false)
                .hasCnsLesions(true)
                .hasActiveCnsLesions(true)
                .hasBoneLesions(null)
                .hasLiverLesions(true)
                .hasLungLesions(false)
                .hasLymphNodeLesions(true)
                .addOtherLesions("Lymph nodes")
                .biopsyLocation("Liver")
                .build();
    }

    @NotNull
    private static ClinicalStatus createTestClinicalStatus() {
        ECG ecg = ImmutableECG.builder().hasSigAberrationLatestECG(false).build();

        InfectionStatus infectionStatus = ImmutableInfectionStatus.builder().hasActiveInfection(false).build();

        return ImmutableClinicalStatus.builder().who(1).infectionStatus(infectionStatus).ecg(ecg).build();
    }

    @NotNull
    private static List<PriorTumorTreatment> createTestPriorTumorTreatments() {
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();

        priorTumorTreatments.add(ImmutablePriorTumorTreatment.builder()
                .name("Resection")
                .startYear(TODAY.getYear() - YEARS_SINCE_TREATMENT_LINE_1)
                .addCategories(TreatmentCategory.SURGERY)
                .isSystemic(false)
                .build());

        priorTumorTreatments.add(ImmutablePriorTumorTreatment.builder()
                .name("Vemurafenib")
                .startYear(TODAY.getYear() - YEARS_SINCE_TREATMENT_LINE_2)
                .startMonth(TODAY.getMonthValue())
                .stopYear(TODAY.getYear() - YEARS_SINCE_TREATMENT_LINE_2 + 1)
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .isSystemic(true)
                .targetedType("BRAF inhibitor")
                .build());

        priorTumorTreatments.add(ImmutablePriorTumorTreatment.builder()
                .name("Ipilimumab")
                .startYear(TODAY.getYear() - YEARS_SINCE_TREATMENT_LINE_3)
                .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                .isSystemic(true)
                .immunoType("Anti-CTLA-4")
                .build());

        return priorTumorTreatments;
    }

    @NotNull
    private static List<PriorSecondPrimary> createTestPriorSecondPrimaries() {
        List<PriorSecondPrimary> priorSecondPrimaries = Lists.newArrayList();

        priorSecondPrimaries.add(ImmutablePriorSecondPrimary.builder()
                .tumorLocation("Lung")
                .tumorSubLocation(Strings.EMPTY)
                .tumorType("carcinoma")
                .tumorSubType(Strings.EMPTY)
                .addDoids("3905")
                .diagnosedYear(TODAY.getYear() - YEARS_SINCE_SECOND_PRIMARY_DIAGNOSIS)
                .diagnosedMonth(TODAY.getMonthValue())
                .treatmentHistory("Surgery")
                .isActive(false)
                .build());

        return priorSecondPrimaries;
    }

    @NotNull
    private static List<PriorOtherCondition> createTestPriorOtherConditions() {
        List<PriorOtherCondition> priorOtherConditions = Lists.newArrayList();

        priorOtherConditions.add(ImmutablePriorOtherCondition.builder()
                .name("Pancreatitis")
                .addDoids("4989")
                .category("Pancreas disease")
                .isContraindicationForTherapy(true)
                .build());

        return priorOtherConditions;
    }

    @NotNull
    private static List<PriorMolecularTest> createTestPriorMolecularTests() {
        List<PriorMolecularTest> priorMolecularTests = Lists.newArrayList();

        priorMolecularTests.add(ImmutablePriorMolecularTest.builder()
                .test("Panel NGS")
                .item("BRAF")
                .measure(null)
                .scoreText("V600E positive")
                .scoreValuePrefix(null)
                .scoreValue(null)
                .scoreValueUnit(null)
                .impliesPotentialIndeterminateStatus(false)
                .build());

        return priorMolecularTests;
    }

    @NotNull
    private static List<Complication> createTestComplications() {
        List<Complication> complications = Lists.newArrayList();

        complications.add(ImmutableComplication.builder().name("Ascites").addCategories("Ascites").build());

        return complications;
    }

    @NotNull
    private static List<LabValue> createTestLabValues() {
        List<LabValue> labValues = Lists.newArrayList();

        labValues.add(ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3))
                .code(LabMeasurement.ASPARTATE_AMINOTRANSFERASE.code())
                .name("Aspartate aminotransferase")
                .comparator(Strings.EMPTY)
                .value(36)
                .unit(LabMeasurement.ASPARTATE_AMINOTRANSFERASE.defaultUnit())
                .refLimitUp(33D)
                .isOutsideRef(true)
                .build());

        labValues.add(ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3))
                .code(LabMeasurement.HEMOGLOBIN.code())
                .name("Hemoglobin")
                .comparator(Strings.EMPTY)
                .value(5.5)
                .unit(LabMeasurement.HEMOGLOBIN.defaultUnit())
                .refLimitLow(6.5D)
                .refLimitUp(9.5D)
                .isOutsideRef(true)
                .build());

        labValues.add(ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_1))
                .code(LabMeasurement.THROMBOCYTES_ABS.code())
                .name("Thrombocytes")
                .comparator(Strings.EMPTY)
                .value(155)
                .unit(LabMeasurement.THROMBOCYTES_ABS.defaultUnit())
                .refLimitLow(155D)
                .refLimitUp(350D)
                .isOutsideRef(false)
                .build());

        labValues.add(ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_2))
                .code(LabMeasurement.THROMBOCYTES_ABS.code())
                .name("Thrombocytes")
                .comparator(Strings.EMPTY)
                .value(151)
                .unit(LabMeasurement.THROMBOCYTES_ABS.defaultUnit())
                .refLimitLow(155D)
                .refLimitUp(350D)
                .isOutsideRef(true)
                .build());

        labValues.add(ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3))
                .code(LabMeasurement.THROMBOCYTES_ABS.code())
                .name("Thrombocytes")
                .comparator(Strings.EMPTY)
                .value(150)
                .unit(LabMeasurement.THROMBOCYTES_ABS.defaultUnit())
                .refLimitLow(155D)
                .refLimitUp(350D)
                .isOutsideRef(true)
                .build());

        labValues.add(ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_1))
                .code(LabMeasurement.LEUKOCYTES_ABS.code())
                .name("Leukocytes")
                .comparator(Strings.EMPTY)
                .value(6.5)
                .unit(LabMeasurement.LEUKOCYTES_ABS.defaultUnit())
                .refLimitLow(3D)
                .refLimitUp(10D)
                .isOutsideRef(false)
                .build());

        labValues.add(ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_1))
                .code(LabMeasurement.EGFR_CKD_EPI.code())
                .name("CKD-EPI eGFR")
                .comparator(">")
                .value(100)
                .unit(LabMeasurement.EGFR_CKD_EPI.defaultUnit())
                .refLimitLow(100D)
                .isOutsideRef(false)
                .build());

        labValues.add(ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_2))
                .code(LabMeasurement.LACTATE_DEHYDROGENASE.code())
                .name("Lactate dehydrogenase")
                .comparator(Strings.EMPTY)
                .value(240)
                .unit(LabMeasurement.LACTATE_DEHYDROGENASE.defaultUnit())
                .refLimitUp(245D)
                .isOutsideRef(false)
                .build());

        return labValues;
    }

    @NotNull
    private static List<Toxicity> createTestToxicities() {
        return List.of(ImmutableToxicity.builder()
                        .name("Nausea")
                        .addCategories("Nausea")
                        .evaluatedDate(TODAY.minusDays(DAYS_SINCE_TOXICITIES))
                        .source(ToxicitySource.EHR)
                        .grade(1)
                        .build(),
                ImmutableToxicity.builder()
                        .name("Fatigue")
                        .addCategories("Fatigue")
                        .evaluatedDate(TODAY.minusDays(DAYS_SINCE_TOXICITIES))
                        .source(ToxicitySource.QUESTIONNAIRE)
                        .grade(2)
                        .build());
    }

    @NotNull
    private static List<ToxicityEvaluation> createTestToxicityEvaluations() {
        return List.of(ImmutableToxicityEvaluation.builder()
                        .evaluatedDate(TODAY.minusDays(DAYS_SINCE_TOXICITIES))
                        .source(ToxicitySource.EHR)
                        .toxicities(Set.of(ImmutableObservedToxicity.builder().name("Nausea").addCategories("Nausea").grade(1).build()))
                        .build(),
                ImmutableToxicityEvaluation.builder()
                        .evaluatedDate(TODAY.minusDays(DAYS_SINCE_TOXICITIES))
                        .source(ToxicitySource.QUESTIONNAIRE)
                        .toxicities(Set.of(ImmutableObservedToxicity.builder().name("Fatigue").addCategories("Fatigue").grade(2).build()))
                        .build());
    }

    @NotNull
    private static List<Intolerance> createTestIntolerances() {
        List<Intolerance> intolerances = Lists.newArrayList();

        intolerances.add(ImmutableIntolerance.builder()
                .name("Wasps")
                .category("Environment")
                .type("Allergy")
                .clinicalStatus("Active")
                .verificationStatus("Confirmed")
                .criticality("Unable-to-assess")
                .build());

        return intolerances;
    }

    @NotNull
    private static List<Surgery> createTestSurgeries() {
        return Collections.singletonList(ImmutableSurgery.builder()
                .endDate(TODAY.minusDays(DAYS_SINCE_SURGERY))
                .status(SurgeryStatus.FINISHED)
                .build());
    }

    @NotNull
    private static List<TreatmentHistoryEntry> createTestSurgicalHistory() {
        return Collections.singletonList(ImmutableTreatmentHistoryEntry.builder()
                .treatments(Set.of(ImmutableSurgicalTreatment.builder().name("test surgery").synonyms(Collections.emptySet()).build()))
                .surgeryHistoryDetails(ImmutableSurgeryHistoryDetails.builder()
                        .endDate(TODAY.minusDays(DAYS_SINCE_SURGERY))
                        .status(SurgeryStatus.FINISHED)
                        .build())
                .build());
    }

    @NotNull
    private static List<BodyWeight> createTestBodyWeights() {
        List<BodyWeight> bodyWeights = Lists.newArrayList();

        bodyWeights.add(ImmutableBodyWeight.builder().date(TODAY.minusDays(DAYS_SINCE_BODY_WEIGHT_1)).value(70D).unit("Kilogram").build());
        bodyWeights.add(ImmutableBodyWeight.builder().date(TODAY.minusDays(DAYS_SINCE_BODY_WEIGHT_2)).value(68D).unit("Kilogram").build());

        return bodyWeights;
    }

    @NotNull
    private static List<VitalFunction> createTestVitalFunctions() {
        List<VitalFunction> vitalFunctions = Lists.newArrayList();

        vitalFunctions.add(ImmutableVitalFunction.builder()
                .date(TODAY.minusDays(DAYS_SINCE_BLOOD_PRESSURE))
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory("Mean blood pressure")
                .value(99)
                .unit("mm[Hg]")
                .build());

        return vitalFunctions;
    }

    @NotNull
    private static List<BloodTransfusion> createTestBloodTransfusions() {
        List<BloodTransfusion> bloodTransfusions = Lists.newArrayList();

        bloodTransfusions.add(ImmutableBloodTransfusion.builder()
                .date(TODAY.minusDays(DAYS_SINCE_BLOOD_TRANSFUSION))
                .product("Thrombocyte concentrate")
                .build());

        return bloodTransfusions;
    }

    @NotNull
    private static List<Medication> createTestMedications() {
        List<Medication> medications = Lists.newArrayList();

        medications.add(TestMedicationFactory.builder()
                .name("Ibuprofen")
                .codeATC("N12")
                .addCategories("NSAIDs")
                .status(MedicationStatus.ACTIVE)
                .dosageMin(750D)
                .dosageMax(1000D)
                .dosageUnit("mg")
                .frequency(1D)
                .frequencyUnit("day")
                .ifNeeded(false)
                .startDate(TODAY.minusDays(DAYS_SINCE_MEDICATION_START))
                .stopDate(TODAY.plusDays(DAYS_UNTIL_MEDICATION_END))
                .build());

        return medications;
    }
}
