package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.DrugTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.DrugType;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.Therapy;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

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
                .treatmentHistory(createTreatmentHistory())
                .priorSecondPrimaries(createTestPriorSecondPrimaries())
                .priorOtherConditions(createTestPriorOtherConditions())
                .priorMolecularTests(createTestPriorMolecularTests())
                .complications(createTestComplications())
                .labValues(createTestLabValues())
                .toxicityEvaluations(createTestToxicityEvaluations())
                .toxicities(createTestToxicities())
                .intolerances(createTestIntolerances())
                .surgeries(createTestSurgeries())
                .bodyWeights(createTestBodyWeights())
                .vitalFunctions(createTestVitalFunctions())
                .bloodTransfusions(createTestBloodTransfusions())
                .medications(createTestMedications())
                .build();
    }

    @NotNull
    public static ClinicalRecord createExhaustiveTestClinicalRecord() {
        return ImmutableClinicalRecord.builder()
                .from(createMinimalTestClinicalRecord())
                .tumor(createTestTumorDetails())
                .clinicalStatus(createTestClinicalStatus())
                .treatmentHistory(createExhaustiveTreatmentHistory())
                .priorSecondPrimaries(createTestPriorSecondPrimaries())
                .priorOtherConditions(createTestPriorOtherConditions())
                .priorMolecularTests(createTestPriorMolecularTests())
                .complications(createTestComplications())
                .labValues(createTestLabValues())
                .toxicityEvaluations(createTestToxicityEvaluations())
                .toxicities(createTestToxicities())
                .intolerances(createTestIntolerances())
                .surgeries(createTestSurgeries())
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
                .primaryTumorSubLocation("")
                .primaryTumorType("Melanoma")
                .primaryTumorSubType("")
                .primaryTumorExtraDetails("")
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
    private static Drug drug(@NotNull String name, @NotNull DrugType drugType, @NotNull TreatmentCategory category) {
        return ImmutableDrug.builder().name(name).addDrugTypes(drugType).category(category).build();
    }

    @NotNull
    private static TreatmentHistoryEntry therapyHistoryEntry(Set<Therapy> therapies, int startYear, Intent intent) {
        return ImmutableTreatmentHistoryEntry.builder()
                .treatments(therapies)
                .startYear(startYear)
                .addIntents(intent)
                .therapyHistoryDetails(ImmutableTherapyHistoryDetails.builder().bestResponse(TreatmentResponse.PARTIAL_RESPONSE).build())
                .build();
    }

    @NotNull
    private static List<TreatmentHistoryEntry> createTreatmentHistory() {
        Drug oxaliplatin = drug("OXALIPLATIN", DrugType.PLATINUM_COMPOUND, TreatmentCategory.CHEMOTHERAPY);
        Drug fluorouracil = drug("5-FU", DrugType.ANTIMETABOLITE, TreatmentCategory.CHEMOTHERAPY);
        Drug irinotecan = drug("IRINOTECAN", DrugType.TOPO1_INHIBITOR, TreatmentCategory.CHEMOTHERAPY);

        DrugTherapy folfirinox = ImmutableDrugTherapy.builder()
                .name("FOLFIRINOX")
                .isSystemic(true)
                .addDrugs(oxaliplatin, fluorouracil, irinotecan)
                .maxCycles(8)
                .build();

        Radiotherapy radioFolfirinox =
                ImmutableRadiotherapy.builder().name("FOLFIRINOX+RADIOTHERAPY").addAllDrugs(folfirinox.drugs()).isSystemic(true).build();

        Drug pembrolizumab = drug("PEMBROLIZUMAB", DrugType.ANTI_PD_1, TreatmentCategory.IMMUNOTHERAPY);

        DrugTherapy folfirinoxAndPembrolizumab = ImmutableDrugTherapy.builder()
                .name("FOLFIRINOX+PEMBROLIZUMAB")
                .addAllDrugs(folfirinox.drugs())
                .addDrugs(pembrolizumab)
                .isSystemic(true)
                .build();

        DrugTherapy folfirinoxLocoRegional =
                ImmutableDrugTherapy.copyOf(folfirinox).withName("FOLFIRINOX_LOCO-REGIONAL").withIsSystemic(false);

        OtherTreatment colectomy =
                ImmutableOtherTreatment.builder().name("COLECTOMY").addCategories(TreatmentCategory.SURGERY).isSystemic(true).build();

        TreatmentHistoryEntry surgeryHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
                .addTreatments(colectomy)
                .startYear(2021)
                .addIntents(Intent.MAINTENANCE)
                .isTrial(false)
                .build();

        return List.of(therapyHistoryEntry(Set.of(folfirinox), 2020, Intent.NEOADJUVANT),
                surgeryHistoryEntry,
                therapyHistoryEntry(Set.of(radioFolfirinox, folfirinoxLocoRegional), 2022, Intent.ADJUVANT),
                therapyHistoryEntry(Set.of(folfirinoxAndPembrolizumab), 2023, Intent.PALLIATIVE));
    }

    private static List<TreatmentHistoryEntry> createExhaustiveTreatmentHistory() {

        Drug irinotecan = drug("IRINOTECAN", DrugType.TOPO1_INHIBITOR, TreatmentCategory.CHEMOTHERAPY);
        TreatmentHistoryEntry emptyHistoryEntry = ImmutableTreatmentHistoryEntry.builder().build();

        TreatmentHistoryEntry hasStartYearHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
                .startYear(2020)
                .addTreatments(ImmutableDrugTherapy.builder().name("Therapy1").addDrugs(irinotecan).build())
                .build();

        TreatmentHistoryEntry hasStartYearMonthEndYearMonthHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
                .startYear(2020)
                .startMonth(8)
                .therapyHistoryDetails(ImmutableTherapyHistoryDetails.builder().stopYear(2021).stopMonth(3).build())
                .addTreatments(ImmutableDrugTherapy.builder().name("Therapy2").addDrugs(irinotecan).build())
                .build();

        TreatmentHistoryEntry namedTrialHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
                .isTrial(true)
                .startYear(2022)
                .addTreatments(ImmutableDrugTherapy.builder().name("Trial1").addDrugs(irinotecan).build())
                .build();

        TreatmentHistoryEntry unknownDetailsHistoryEntry = ImmutableTreatmentHistoryEntry.builder().isTrial(true).startYear(2022).build();

        TreatmentHistoryEntry hasCyclesStopReasonHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
                .isTrial(true)
                .startYear(2022)
                .therapyHistoryDetails(ImmutableTherapyHistoryDetails.builder().cycles(3).stopReasonDetail("toxicity").build())
                .addTreatments(ImmutableDrugTherapy.builder().name("Trial1").addDrugs(irinotecan).build())
                .build();

        TreatmentHistoryEntry hasAcronymHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
                .isTrial(true)
                .trialAcronym("tr2")
                .startYear(2022)
                .therapyHistoryDetails(ImmutableTherapyHistoryDetails.builder().cycles(3).stopReasonDetail("toxicity").build())
                .addTreatments(ImmutableDrugTherapy.builder().name("Trial2").addDrugs(irinotecan).build())
                .build();

        TreatmentHistoryEntry hasSingleIntentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
                .isTrial(true)
                .startYear(2022)
                .therapyHistoryDetails(ImmutableTherapyHistoryDetails.builder().cycles(3).stopReasonDetail("toxicity").build())
                .addTreatments(ImmutableDrugTherapy.builder().name("Trial4").addDrugs(irinotecan).build())
                .intents(Set.of(Intent.ADJUVANT))
                .build();

        TreatmentHistoryEntry hasMultipleIntentsHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
                .isTrial(true)
                .startYear(2022)
                .therapyHistoryDetails(ImmutableTherapyHistoryDetails.builder().cycles(3).stopReasonDetail("toxicity").build())
                .addTreatments(ImmutableDrugTherapy.builder().name("Trial5").addDrugs(irinotecan).build())
                .intents(Set.of(Intent.ADJUVANT, Intent.CONSOLIDATION))
                .build();

        return List.of(emptyHistoryEntry,
                hasStartYearHistoryEntry,
                hasStartYearMonthEndYearMonthHistoryEntry,
                namedTrialHistoryEntry,
                unknownDetailsHistoryEntry,
                hasCyclesStopReasonHistoryEntry,
                hasAcronymHistoryEntry,
                hasSingleIntentHistoryEntry,
                hasMultipleIntentsHistoryEntry);
    }

    @NotNull
    private static List<PriorSecondPrimary> createTestPriorSecondPrimaries() {
        List<PriorSecondPrimary> priorSecondPrimaries = Lists.newArrayList();

        priorSecondPrimaries.add(ImmutablePriorSecondPrimary.builder()
                .tumorLocation("Lung")
                .tumorSubLocation("")
                .tumorType("Carcinoma")
                .tumorSubType("Adenocarcinoma")
                .addDoids("3905")
                .diagnosedYear(TODAY.getYear() - YEARS_SINCE_SECOND_PRIMARY_DIAGNOSIS)
                .diagnosedMonth(TODAY.getMonthValue())
                .treatmentHistory("Surgery")
                .status(TumorStatus.INACTIVE)
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
                .comparator("")
                .value(36)
                .unit(LabMeasurement.ASPARTATE_AMINOTRANSFERASE.defaultUnit())
                .refLimitUp(33D)
                .isOutsideRef(true)
                .build());

        labValues.add(ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3))
                .code(LabMeasurement.HEMOGLOBIN.code())
                .name("Hemoglobin")
                .comparator("")
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
                .comparator("")
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
                .comparator("")
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
                .comparator("")
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
                .comparator("")
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
                .comparator("")
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
                .status(MedicationStatus.ACTIVE)
                .dosage(ImmutableDosage.builder()
                        .dosageMin(750D)
                        .dosageMax(1000D)
                        .dosageUnit("mg")
                        .frequency(1D)
                        .frequencyUnit("day")
                        .ifNeeded(false)
                        .build())
                .startDate(TODAY.minusDays(DAYS_SINCE_MEDICATION_START))
                .stopDate(TODAY.plusDays(DAYS_UNTIL_MEDICATION_END))
                .isSelfCare(false)
                .isTrialMedication(false)
                .build());

        medications.add(TestMedicationFactory.builder()
                .name("Prednison")
                .status(MedicationStatus.ACTIVE)
                .dosage(ImmutableDosage.builder()
                        .dosageMin(750D)
                        .dosageMax(1000D)
                        .dosageUnit("mg")
                        .frequency(1D)
                        .frequencyUnit("day")
                        .periodBetweenUnit("months")
                        .periodBetweenValue(2D)
                        .ifNeeded(false)
                        .build())
                .startDate(TODAY.minusDays(DAYS_SINCE_MEDICATION_START))
                .stopDate(TODAY.plusDays(DAYS_UNTIL_MEDICATION_END))
                .isSelfCare(false)
                .isTrialMedication(false)
                .build());

        return medications;
    }
}
