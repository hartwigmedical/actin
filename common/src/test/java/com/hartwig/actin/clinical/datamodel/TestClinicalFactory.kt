package com.hartwig.actin.clinical.datamodel

import com.google.common.collect.Lists
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentStage
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import java.time.LocalDate

object TestClinicalFactory {
    private val TODAY = LocalDate.now()
    private const val DAYS_SINCE_QUESTIONNAIRE = 10
    private const val DAYS_SINCE_REGISTRATION = 15
    private const val DAYS_SINCE_LAB_MEASUREMENT_1 = 30
    private const val DAYS_SINCE_LAB_MEASUREMENT_2 = 20
    private const val DAYS_SINCE_LAB_MEASUREMENT_3 = 10
    private const val DAYS_SINCE_TOXICITIES = 30
    private const val DAYS_SINCE_SURGERY = 30
    private const val DAYS_SINCE_BODY_WEIGHT_1 = 12
    private const val DAYS_SINCE_BODY_WEIGHT_2 = 18
    private const val DAYS_SINCE_BLOOD_PRESSURE = 15
    private const val DAYS_SINCE_BLOOD_TRANSFUSION = 15
    private const val DAYS_SINCE_MEDICATION_START = 30
    private const val DAYS_UNTIL_MEDICATION_END = 15
    private const val YEARS_SINCE_SECOND_PRIMARY_DIAGNOSIS = 3

    @JvmStatic
    fun createMinimalTestClinicalRecord(): ClinicalRecord {
        return ImmutableClinicalRecord.builder()
            .patientId(TestDataFactory.TEST_PATIENT)
            .patient(createTestPatientDetails())
            .tumor(ImmutableTumorDetails.builder().build())
            .clinicalStatus(ImmutableClinicalStatus.builder().build())
            .build()
    }

    @JvmStatic
    fun createProperTestClinicalRecord(): ClinicalRecord {
        return ImmutableClinicalRecord.builder()
            .from(createMinimalTestClinicalRecord())
            .tumor(createTestTumorDetails()).clinicalStatus(createTestClinicalStatus()).oncologicalHistory(createTreatmentHistory())
            .priorSecondPrimaries(createTestPriorSecondPrimaries())
            .priorOtherConditions(createTestPriorOtherConditions())
            .priorMolecularTests(createTestPriorMolecularTests())
            .complications(createTestComplications())
            .labValues(createTestLabValues())
            .toxicities(createTestToxicities())
            .intolerances(createTestIntolerances())
            .surgeries(createTestSurgeries())
            .bodyWeights(createTestBodyWeights())
            .vitalFunctions(createTestVitalFunctions())
            .bloodTransfusions(createTestBloodTransfusions())
            .medications(createTestMedications())
            .build()
    }

    fun createExhaustiveTestClinicalRecord(): ClinicalRecord {
        return ImmutableClinicalRecord.builder()
            .from(createMinimalTestClinicalRecord())
            .tumor(createTestTumorDetails())
            .clinicalStatus(createTestClinicalStatus())
            .oncologicalHistory(createExhaustiveTreatmentHistory())
            .priorSecondPrimaries(createTestPriorSecondPrimaries())
            .priorOtherConditions(createTestPriorOtherConditions())
            .priorMolecularTests(createTestPriorMolecularTests())
            .complications(createTestComplications())
            .labValues(createTestLabValues())
            .toxicities(createTestToxicities())
            .intolerances(createTestIntolerances())
            .surgeries(createTestSurgeries())
            .bodyWeights(createTestBodyWeights())
            .vitalFunctions(createTestVitalFunctions())
            .bloodTransfusions(createTestBloodTransfusions())
            .medications(createTestMedications())
            .build()
    }

    private fun createTestPatientDetails(): PatientDetails {
        return ImmutablePatientDetails.builder()
            .gender(Gender.MALE)
            .birthYear(1950)
            .registrationDate(TODAY.minusDays(DAYS_SINCE_REGISTRATION.toLong()))
            .questionnaireDate(TODAY.minusDays(DAYS_SINCE_QUESTIONNAIRE.toLong()))
            .build()
    }

    private fun createTestTumorDetails(): TumorDetails {
        return ImmutableTumorDetails.builder()
            .primaryTumorLocation("Skin")
            .primaryTumorSubLocation("")
            .primaryTumorType("Melanoma")
            .primaryTumorSubType("")
            .primaryTumorExtraDetails("")
            .addDoids("8923")
            .stage(TumorStage.IV)
            .hasMeasurableDisease(true)
            .hasBrainLesions(true)
            .hasActiveBrainLesions(false)
            .hasCnsLesions(true)
            .hasActiveCnsLesions(true)
            .hasBoneLesions(null)
            .hasLiverLesions(true)
            .hasLungLesions(true)
            .hasLymphNodeLesions(true)
            .addOtherLesions("lymph nodes cervical and supraclavicular")
            .addOtherLesions("lymph nodes abdominal")
            .addOtherLesions("lymph node")
            .addOtherLesions("Test Lesion")
            .biopsyLocation("Liver")
            .build()
    }

    private fun createTestClinicalStatus(): ClinicalStatus {
        val ecg: ECG = ImmutableECG.builder().hasSigAberrationLatestECG(false).build()
        val infectionStatus: InfectionStatus = ImmutableInfectionStatus.builder().hasActiveInfection(false).build()
        return ImmutableClinicalStatus.builder().who(1).infectionStatus(infectionStatus).ecg(ecg).build()
    }

    private fun drug(name: String, drugType: DrugType, category: TreatmentCategory): Drug {
        return ImmutableDrug.builder().name(name).addDrugTypes(drugType).category(category).build()
    }

    private fun treatmentHistoryEntry(treatments: Set<Treatment>, startYear: Int, intent: Intent): TreatmentHistoryEntry {
        return ImmutableTreatmentHistoryEntry.builder()
            .treatments(treatments)
            .startYear(startYear)
            .addIntents(intent)
            .treatmentHistoryDetails(
                ImmutableTreatmentHistoryDetails.builder()
                    .bestResponse(TreatmentResponse.PARTIAL_RESPONSE)
                    .build()
            )
            .build()
    }

    private fun createTreatmentHistory(): List<TreatmentHistoryEntry> {
        val oxaliplatin = drug("OXALIPLATIN", DrugType.PLATINUM_COMPOUND, TreatmentCategory.CHEMOTHERAPY)
        val fluorouracil = drug("5-FU", DrugType.ANTIMETABOLITE, TreatmentCategory.CHEMOTHERAPY)
        val irinotecan = drug("IRINOTECAN", DrugType.TOPO1_INHIBITOR, TreatmentCategory.CHEMOTHERAPY)
        val folfirinox: DrugTreatment = ImmutableDrugTreatment.builder()
            .name("FOLFIRINOX")
            .isSystemic(true)
            .addDrugs(oxaliplatin, fluorouracil, irinotecan)
            .maxCycles(8)
            .build()
        val radiotherapy: Radiotherapy = ImmutableRadiotherapy.builder().name("RADIOTHERAPY").isSystemic(false).build()
        val pembrolizumab = drug("PEMBROLIZUMAB", DrugType.ANTI_PD_1, TreatmentCategory.IMMUNOTHERAPY)
        val folfirinoxAndPembrolizumab: DrugTreatment = ImmutableDrugTreatment.builder()
            .name("FOLFIRINOX+PEMBROLIZUMAB")
            .addAllDrugs(folfirinox.drugs())
            .addDrugs(pembrolizumab)
            .isSystemic(true)
            .build()
        val folfirinoxLocoRegional: DrugTreatment =
            ImmutableDrugTreatment.copyOf(folfirinox).withName("FOLFIRINOX_LOCO-REGIONAL").withIsSystemic(false)
        val colectomy: OtherTreatment =
            ImmutableOtherTreatment.builder().name("COLECTOMY").addCategories(TreatmentCategory.SURGERY).isSystemic(true).build()
        val surgeryHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .addTreatments(colectomy)
            .startYear(2021)
            .addIntents(Intent.MAINTENANCE)
            .isTrial(false)
            .build()
        val folfirinoxEntry = treatmentHistoryEntry(java.util.Set.of<Treatment>(folfirinox), 2020, Intent.NEOADJUVANT)
        val switchToTreatments =
            java.util.Set.of<TreatmentStage>(ImmutableTreatmentStage.builder().treatment(folfirinoxAndPembrolizumab).cycles(3).build())
        val maintenanceTreatment: TreatmentStage = ImmutableTreatmentStage.builder().treatment(folfirinoxLocoRegional).build()
        val entryWithSwitchAndMaintenance: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(folfirinoxEntry)
            .withTreatmentHistoryDetails(
                ImmutableTreatmentHistoryDetails.copyOf(folfirinoxEntry.treatmentHistoryDetails())
                    .withCycles(4)
                    .withSwitchToTreatments(switchToTreatments)
                    .withMaintenanceTreatment(maintenanceTreatment)
            )
        return java.util.List.of(
            entryWithSwitchAndMaintenance,
            surgeryHistoryEntry,
            treatmentHistoryEntry(java.util.Set.of(radiotherapy, folfirinoxLocoRegional), 2022, Intent.ADJUVANT),
            treatmentHistoryEntry(java.util.Set.of<Treatment>(folfirinoxAndPembrolizumab), 2023, Intent.PALLIATIVE)
        )
    }

    private fun createExhaustiveTreatmentHistory(): List<TreatmentHistoryEntry> {
        val irinotecan = drug("IRINOTECAN", DrugType.TOPO1_INHIBITOR, TreatmentCategory.CHEMOTHERAPY)
        val hasNoDateHistoryEntry: TreatmentHistoryEntry =
            ImmutableTreatmentHistoryEntry.builder().addTreatments(ImmutableDrugTreatment.builder().name("Therapy").build()).build()
        val hasStartYearHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .startYear(2019)
            .addTreatments(ImmutableDrugTreatment.builder().name("Therapy1").addDrugs(irinotecan).build())
            .build()
        val hasStartYearHistoryEntry2: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .startYear(2023)
            .addTreatments(ImmutableDrugTreatment.builder().name("Therapy4").addDrugs(irinotecan).build())
            .build()
        val hasEndYearMonthHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .startYear(null)
            .startMonth(null)
            .treatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder().stopYear(2020).stopMonth(6).build())
            .addTreatments(ImmutableDrugTreatment.builder().name("Therapy2").addDrugs(irinotecan).build())
            .build()
        val hasStartYearMonthEndYearMonthHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .startYear(2020)
            .startMonth(8)
            .treatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder().stopYear(2021).stopMonth(3).build())
            .addTreatments(ImmutableDrugTreatment.builder().name("Therapy3").addDrugs(irinotecan).build())
            .build()
        val namedTrialHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .isTrial(true)
            .startYear(2022)
            .addTreatments(ImmutableDrugTreatment.builder().name("Trial1").addDrugs(irinotecan).build())
            .build()
        val unknownDetailsHistoryEntry: TreatmentHistoryEntry =
            ImmutableTreatmentHistoryEntry.builder().isTrial(true).startYear(2022).build()
        val hasCyclesStopReasonHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .isTrial(true)
            .startYear(2022)
            .treatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder().cycles(3).stopReasonDetail("toxicity").build())
            .addTreatments(ImmutableDrugTreatment.builder().name("Trial1").addDrugs(irinotecan).build())
            .build()
        val hasAcronymHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .isTrial(true)
            .trialAcronym("tr2")
            .startYear(2022)
            .treatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder().cycles(3).stopReasonDetail("toxicity").build())
            .addTreatments(ImmutableDrugTreatment.builder().name("Trial2").addDrugs(irinotecan).build())
            .build()
        val hasSingleIntentHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .isTrial(true)
            .startYear(2022)
            .treatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder().cycles(1).stopReasonDetail("toxicity").build())
            .addTreatments(ImmutableDrugTreatment.builder().name("Trial4").addDrugs(irinotecan).build())
            .intents(java.util.Set.of(Intent.ADJUVANT))
            .build()
        val hasMultipleIntentsHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
            .isTrial(true)
            .startYear(2022)
            .treatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder().cycles(null).stopReasonDetail("toxicity").build())
            .addTreatments(ImmutableDrugTreatment.builder().name("Trial5").addDrugs(irinotecan).build())
            .intents(java.util.Set.of(Intent.ADJUVANT, Intent.CONSOLIDATION))
            .build()
        return java.util.List.of(
            hasNoDateHistoryEntry,
            hasStartYearHistoryEntry,
            hasStartYearHistoryEntry2,
            hasStartYearMonthEndYearMonthHistoryEntry,
            hasEndYearMonthHistoryEntry,
            namedTrialHistoryEntry,
            unknownDetailsHistoryEntry,
            hasCyclesStopReasonHistoryEntry,
            hasAcronymHistoryEntry,
            hasSingleIntentHistoryEntry,
            hasMultipleIntentsHistoryEntry
        )
    }

    private fun createTestPriorSecondPrimaries(): List<PriorSecondPrimary> {
        val priorSecondPrimaries: MutableList<PriorSecondPrimary> = Lists.newArrayList()
        priorSecondPrimaries.add(
            ImmutablePriorSecondPrimary.builder()
                .tumorLocation("Lung")
                .tumorSubLocation("")
                .tumorType("Carcinoma")
                .tumorSubType("Adenocarcinoma")
                .addDoids("3905")
                .diagnosedYear(TODAY.year - YEARS_SINCE_SECOND_PRIMARY_DIAGNOSIS)
                .diagnosedMonth(TODAY.monthValue)
                .treatmentHistory("Surgery")
                .status(TumorStatus.INACTIVE)
                .build()
        )
        return priorSecondPrimaries
    }

    private fun createTestPriorOtherConditions(): List<PriorOtherCondition> {
        val priorOtherConditions: MutableList<PriorOtherCondition> = Lists.newArrayList()
        priorOtherConditions.add(
            ImmutablePriorOtherCondition.builder()
                .name("Pancreatitis")
                .addDoids("4989")
                .category("Pancreas disease")
                .isContraindicationForTherapy(true)
                .build()
        )
        priorOtherConditions.add(
            ImmutablePriorOtherCondition.builder()
                .name("Coronary artery bypass graft (CABG)")
                .addDoids("3393")
                .category("Heart disease")
                .isContraindicationForTherapy(true)
                .build()
        )
        return priorOtherConditions
    }

    private fun createTestPriorMolecularTests(): List<PriorMolecularTest> {
        val priorMolecularTests: MutableList<PriorMolecularTest> = Lists.newArrayList()
        priorMolecularTests.add(
            ImmutablePriorMolecularTest.builder()
                .test("")
                .item("KIT")
                .measure(null)
                .scoreText("Exon 11: c.1714_1719dup p.D572_P573dup")
                .scoreValuePrefix(null)
                .scoreValue(null)
                .scoreValueUnit(null)
                .impliesPotentialIndeterminateStatus(false)
                .build()
        )
        priorMolecularTests.add(
            ImmutablePriorMolecularTest.builder()
                .test("IHC")
                .item("PD-L1")
                .measure(null)
                .scoreText(null)
                .scoreValuePrefix(null)
                .scoreValue(90.0)
                .scoreValueUnit("%")
                .impliesPotentialIndeterminateStatus(false)
                .build()
        )
        return priorMolecularTests
    }

    private fun createTestComplications(): List<Complication> {
        val complications: MutableList<Complication> = Lists.newArrayList()
        complications.add(ImmutableComplication.builder().name("Ascites").addCategories("Ascites").build())
        return complications
    }

    private fun createTestLabValues(): List<LabValue> {
        val labValues: MutableList<LabValue> = Lists.newArrayList()
        labValues.add(
            ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3.toLong()))
                .code(LabMeasurement.ASPARTATE_AMINOTRANSFERASE.code())
                .name("Aspartate aminotransferase")
                .comparator("")
                .value(36.0)
                .unit(LabMeasurement.ASPARTATE_AMINOTRANSFERASE.defaultUnit())
                .refLimitUp(33.0)
                .isOutsideRef(true)
                .build()
        )
        labValues.add(
            ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3.toLong()))
                .code(LabMeasurement.HEMOGLOBIN.code())
                .name("Hemoglobin")
                .comparator("")
                .value(5.5)
                .unit(LabMeasurement.HEMOGLOBIN.defaultUnit())
                .refLimitLow(6.5)
                .refLimitUp(9.5)
                .isOutsideRef(true)
                .build()
        )
        labValues.add(
            ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_1.toLong()))
                .code(LabMeasurement.THROMBOCYTES_ABS.code())
                .name("Thrombocytes")
                .comparator("")
                .value(155.0)
                .unit(LabMeasurement.THROMBOCYTES_ABS.defaultUnit())
                .refLimitLow(155.0)
                .refLimitUp(350.0)
                .isOutsideRef(false)
                .build()
        )
        labValues.add(
            ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_2.toLong()))
                .code(LabMeasurement.THROMBOCYTES_ABS.code())
                .name("Thrombocytes")
                .comparator("")
                .value(151.0)
                .unit(LabMeasurement.THROMBOCYTES_ABS.defaultUnit())
                .refLimitLow(155.0)
                .refLimitUp(350.0)
                .isOutsideRef(true)
                .build()
        )
        labValues.add(
            ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3.toLong()))
                .code(LabMeasurement.THROMBOCYTES_ABS.code())
                .name("Thrombocytes")
                .comparator("")
                .value(150.0)
                .unit(LabMeasurement.THROMBOCYTES_ABS.defaultUnit())
                .refLimitLow(155.0)
                .refLimitUp(350.0)
                .isOutsideRef(true)
                .build()
        )
        labValues.add(
            ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_1.toLong()))
                .code(LabMeasurement.LEUKOCYTES_ABS.code())
                .name("Leukocytes")
                .comparator("")
                .value(6.5)
                .unit(LabMeasurement.LEUKOCYTES_ABS.defaultUnit())
                .refLimitLow(3.0)
                .refLimitUp(10.0)
                .isOutsideRef(false)
                .build()
        )
        labValues.add(
            ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_1.toLong()))
                .code(LabMeasurement.EGFR_CKD_EPI.code())
                .name("CKD-EPI eGFR")
                .comparator(">")
                .value(100.0)
                .unit(LabMeasurement.EGFR_CKD_EPI.defaultUnit())
                .refLimitLow(100.0)
                .isOutsideRef(false)
                .build()
        )
        labValues.add(
            ImmutableLabValue.builder()
                .date(TODAY.minusDays(DAYS_SINCE_LAB_MEASUREMENT_2.toLong()))
                .code(LabMeasurement.LACTATE_DEHYDROGENASE.code())
                .name("Lactate dehydrogenase")
                .comparator("")
                .value(240.0)
                .unit(LabMeasurement.LACTATE_DEHYDROGENASE.defaultUnit())
                .refLimitUp(245.0)
                .isOutsideRef(false)
                .build()
        )
        return labValues
    }

    private fun createTestToxicities(): List<Toxicity> {
        return java.util.List.of<Toxicity>(
            ImmutableToxicity.builder()
                .name("Nausea")
                .addCategories("Nausea")
                .evaluatedDate(TODAY.minusDays(DAYS_SINCE_TOXICITIES.toLong()))
                .source(ToxicitySource.EHR)
                .grade(1)
                .build(),
            ImmutableToxicity.builder()
                .name("Fatigue")
                .addCategories("Fatigue")
                .evaluatedDate(TODAY.minusDays(DAYS_SINCE_TOXICITIES.toLong()))
                .source(ToxicitySource.QUESTIONNAIRE)
                .grade(2)
                .build()
        )
    }

    private fun createTestIntolerances(): List<Intolerance> {
        val intolerances: MutableList<Intolerance> = Lists.newArrayList()
        intolerances.add(
            ImmutableIntolerance.builder()
                .name("Wasps")
                .category("Environment")
                .type("Allergy")
                .clinicalStatus("Active")
                .verificationStatus("Confirmed")
                .criticality("Unable-to-assess")
                .build()
        )
        return intolerances
    }

    private fun createTestSurgeries(): List<Surgery> {
        return listOf<Surgery>(
            ImmutableSurgery.builder()
                .endDate(TODAY.minusDays(DAYS_SINCE_SURGERY.toLong()))
                .status(SurgeryStatus.FINISHED)
                .build()
        )
    }

    private fun createTestBodyWeights(): List<BodyWeight> {
        val bodyWeights: MutableList<BodyWeight> = Lists.newArrayList()
        bodyWeights.add(
            ImmutableBodyWeight.builder().date(TODAY.minusDays(DAYS_SINCE_BODY_WEIGHT_1.toLong())).value(70.0).unit("Kilogram").build()
        )
        bodyWeights.add(
            ImmutableBodyWeight.builder().date(TODAY.minusDays(DAYS_SINCE_BODY_WEIGHT_2.toLong())).value(68.0).unit("Kilogram").build()
        )
        return bodyWeights
    }

    private fun createTestVitalFunctions(): List<VitalFunction> {
        val vitalFunctions: MutableList<VitalFunction> = Lists.newArrayList()
        vitalFunctions.add(
            ImmutableVitalFunction.builder()
                .date(TODAY.minusDays(DAYS_SINCE_BLOOD_PRESSURE.toLong()))
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory("Mean blood pressure")
                .value(99.0)
                .unit("mm[Hg]")
                .build()
        )
        return vitalFunctions
    }

    private fun createTestBloodTransfusions(): List<BloodTransfusion> {
        val bloodTransfusions: MutableList<BloodTransfusion> = Lists.newArrayList()
        bloodTransfusions.add(
            ImmutableBloodTransfusion.builder()
                .date(TODAY.minusDays(DAYS_SINCE_BLOOD_TRANSFUSION.toLong()))
                .product("Thrombocyte concentrate")
                .build()
        )
        return bloodTransfusions
    }

    private fun createTestMedications(): List<Medication> {
        val medications: MutableList<Medication> = Lists.newArrayList()
        medications.add(
            TestMedicationFactory.builder()
                .name("Ibuprofen")
                .status(MedicationStatus.ACTIVE)
                .dosage(
                    ImmutableDosage.builder()
                        .dosageMin(750.0)
                        .dosageMax(1000.0)
                        .dosageUnit("mg")
                        .frequency(1.0)
                        .frequencyUnit("day")
                        .ifNeeded(false)
                        .build()
                )
                .startDate(TODAY.minusDays(DAYS_SINCE_MEDICATION_START.toLong()))
                .stopDate(TODAY.plusDays(DAYS_UNTIL_MEDICATION_END.toLong()))
                .isSelfCare(false)
                .isTrialMedication(false)
                .build()
        )
        medications.add(
            TestMedicationFactory.builder()
                .name("Prednison")
                .status(MedicationStatus.ACTIVE)
                .dosage(
                    ImmutableDosage.builder()
                        .dosageMin(750.0)
                        .dosageMax(1000.0)
                        .dosageUnit("mg")
                        .frequency(1.0)
                        .frequencyUnit("day")
                        .periodBetweenUnit("months")
                        .periodBetweenValue(2.0)
                        .ifNeeded(false)
                        .build()
                )
                .startDate(TODAY.minusDays(DAYS_SINCE_MEDICATION_START.toLong()))
                .stopDate(TODAY.plusDays(DAYS_UNTIL_MEDICATION_END.toLong()))
                .isSelfCare(false)
                .isTrialMedication(false)
                .build()
        )
        return medications
    }
}
