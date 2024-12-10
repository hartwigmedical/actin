package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import java.time.LocalDateTime

object TestClinicalFactory {

    private val FIXED_DATETIME = LocalDateTime.of(2024, 10, 10, 0, 0)
    private val FIXED_DATE = FIXED_DATETIME.toLocalDate()
    private const val DAYS_SINCE_QUESTIONNAIRE = 10
    private const val DAYS_SINCE_REGISTRATION = 15
    private const val DAYS_SINCE_LAB_MEASUREMENT_1 = 30
    private const val DAYS_SINCE_LAB_MEASUREMENT_2 = 20
    private const val DAYS_SINCE_LAB_MEASUREMENT_3 = 10
    private const val DAYS_SINCE_TOXICITIES = 30
    private const val DAYS_SINCE_SURGERY = 30
    private const val DAYS_SINCE_SURGERY_2 = 40
    private const val DAYS_SINCE_BODY_WEIGHT_1 = 12
    private const val DAYS_SINCE_BODY_WEIGHT_2 = 18
    private const val DAYS_SINCE_BLOOD_PRESSURE = 15
    private const val DAYS_SINCE_BLOOD_TRANSFUSION = 15
    private const val DAYS_SINCE_MEDICATION_START = 30
    private const val DAYS_UNTIL_MEDICATION_END = 15
    private const val YEARS_SINCE_SECOND_PRIMARY_DIAGNOSIS = 3

    fun createMinimalTestClinicalRecord(): ClinicalRecord {
        return ClinicalRecord(
            patientId = TestPatientFactory.TEST_PATIENT,
            patient = createTestPatientDetails(),
            tumor = TumorDetails(),
            clinicalStatus = ClinicalStatus(),
            oncologicalHistory = emptyList(),
            priorSecondPrimaries = emptyList(),
            priorOtherConditions = emptyList(),
            priorIHCTests = emptyList(),
            priorSequencingTests = emptyList(),
            complications = null,
            labValues = emptyList(),
            toxicities = emptyList(),
            intolerances = emptyList(),
            surgeries = emptyList(),
            bodyWeights = emptyList(),
            bodyHeights = emptyList(),
            vitalFunctions = emptyList(),
            bloodTransfusions = emptyList(),
            medications = emptyList()
        )
    }

    fun createProperTestClinicalRecord(): ClinicalRecord {
        return createMinimalTestClinicalRecord().copy(
            tumor = createTestTumorDetails(),
            clinicalStatus = createTestClinicalStatus(),
            oncologicalHistory = createTreatmentHistory(),
            priorSecondPrimaries = createTestPriorSecondPrimaries(),
            priorOtherConditions = createTestPriorOtherConditions(),
            priorIHCTests = createTestPriorMolecularTests(),
            complications = createTestComplications(),
            labValues = createTestLabValues(),
            toxicities = createTestToxicities(),
            intolerances = createTestIntolerances(),
            surgeries = createTestSurgeries(),
            bodyWeights = createTestBodyWeights(),
            vitalFunctions = createTestVitalFunctions(),
            bloodTransfusions = createTestBloodTransfusions(),
            medications = createTestMedications()
        )
    }

    fun createExhaustiveTestClinicalRecord(): ClinicalRecord {
        return createProperTestClinicalRecord().copy(oncologicalHistory = createExhaustiveTreatmentHistory())
    }

    private fun createTestPatientDetails(): PatientDetails {
        return PatientDetails(
            gender = Gender.MALE,
            birthYear = 1950,
            registrationDate = FIXED_DATE.minusDays(DAYS_SINCE_REGISTRATION.toLong()),
            questionnaireDate = FIXED_DATE.minusDays(DAYS_SINCE_QUESTIONNAIRE.toLong()),
            hasHartwigSequencing = true
        )
    }

    private fun createTestTumorDetails(): TumorDetails {
        return TumorDetails(
            primaryTumorLocation = "Skin",
            primaryTumorSubLocation = "",
            primaryTumorType = "Melanoma",
            primaryTumorSubType = "",
            primaryTumorExtraDetails = "",
            doids = setOf("8923"),
            stage = TumorStage.IV,
            hasMeasurableDisease = true,
            hasBrainLesions = true,
            hasActiveBrainLesions = false,
            hasCnsLesions = true,
            hasActiveCnsLesions = true,
            hasBoneLesions = null,
            hasLiverLesions = true,
            hasLungLesions = true,
            hasLymphNodeLesions = true,
            otherLesions = listOf("lymph nodes cervical and supraclavicular", "lymph nodes abdominal", "lymph node", "Test Lesion"),
            biopsyLocation = "Liver"
        )
    }

    private fun createTestClinicalStatus(): ClinicalStatus {
        return ClinicalStatus(
            who = 1,
            infectionStatus = InfectionStatus(hasActiveInfection = false, description = null),
            ecg = ECG(hasSigAberrationLatestECG = false, aberrationDescription = null, jtcMeasure = null, qtcfMeasure = null)
        )
    }

    private fun drug(name: String, drugType: DrugType, category: TreatmentCategory): Drug {
        return Drug(name = name, drugTypes = setOf(drugType), category = category)
    }

    private fun createTreatmentHistory(): List<TreatmentHistoryEntry> {
        val oxaliplatin = drug("OXALIPLATIN", DrugType.PLATINUM_COMPOUND, TreatmentCategory.CHEMOTHERAPY)
        val fluorouracil = drug("5-FU", DrugType.ANTIMETABOLITE, TreatmentCategory.CHEMOTHERAPY)
        val irinotecan = drug("IRINOTECAN", DrugType.TOPO1_INHIBITOR, TreatmentCategory.CHEMOTHERAPY)
        val folfirinox = DrugTreatment(
            name = "FOLFIRINOX",
            drugs = setOf(oxaliplatin, fluorouracil, irinotecan),
            maxCycles = 8
        )
        val radiotherapy = Radiotherapy(name = "RADIOTHERAPY")
        val pembrolizumab = drug("PEMBROLIZUMAB", DrugType.ANTI_PD_1, TreatmentCategory.IMMUNOTHERAPY)
        val folfirinoxAndPembrolizumab = DrugTreatment(
            name = "FOLFIRINOX+PEMBROLIZUMAB",
            drugs = folfirinox.drugs + pembrolizumab,
        )
        val folfirinoxLocoRegional = folfirinox.copy(name = "FOLFIRINOX_LOCO-REGIONAL", isSystemic = false)
        val colectomy = OtherTreatment(name = "COLECTOMY", isSystemic = false, categories = setOf(TreatmentCategory.SURGERY))
        val surgeryHistoryEntry = treatmentHistoryEntry(
            setOf(colectomy),
            startYear = 2021,
            intents = setOf(Intent.MAINTENANCE),
            isTrial = false
        )
        val folfirinoxEntry = treatmentHistoryEntry(
            treatments = setOf(folfirinox),
            startYear = 2020,
            intents = setOf(Intent.NEOADJUVANT),
            bestResponse = TreatmentResponse.PARTIAL_RESPONSE
        )
        val switchToTreatments = listOf(treatmentStage(treatment = folfirinoxAndPembrolizumab, cycles = 3))
        val maintenanceTreatment = treatmentStage(treatment = folfirinoxLocoRegional)
        val entryWithSwitchAndMaintenance = folfirinoxEntry.copy(
            treatmentHistoryDetails = folfirinoxEntry.treatmentHistoryDetails!!.copy(
                cycles = 4,
                switchToTreatments = switchToTreatments,
                maintenanceTreatment = maintenanceTreatment,
            )
        )
        return listOf(
            entryWithSwitchAndMaintenance,
            surgeryHistoryEntry,
            treatmentHistoryEntry(
                treatments = setOf(radiotherapy, folfirinoxLocoRegional),
                startYear = 2022,
                intents = setOf(Intent.ADJUVANT),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE
            ),
            treatmentHistoryEntry(
                treatments = setOf(folfirinoxAndPembrolizumab),
                startYear = 2023,
                intents = setOf(Intent.PALLIATIVE),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE
            )
        )
    }

    private fun createExhaustiveTreatmentHistory(): List<TreatmentHistoryEntry> {
        val hasNoDateHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("Therapy", TreatmentCategory.CHEMOTHERAPY)))
        val hasStartYearHistoryEntry = treatmentHistoryEntry(
            startYear = 2019, treatments = drugTreatmentSet("Therapy1")
        )
        val hasStartYearHistoryEntry2 = treatmentHistoryEntry(
            startYear = 2023, treatments = drugTreatmentSet("Therapy4")
        )
        val hasEndYearMonthHistoryEntry = treatmentHistoryEntry(
            stopYear = 2020, stopMonth = 6, treatments = drugTreatmentSet("Therapy2")
        )
        val hasStartYearMonthEndYearMonthHistoryEntry = treatmentHistoryEntry(
            startYear = 2020,
            startMonth = 8,
            stopYear = 2021,
            stopMonth = 3,
            treatments = drugTreatmentSet("Therapy3")
        )
        val namedTrialHistoryEntry = treatmentHistoryEntry(isTrial = true, startYear = 2022, treatments = drugTreatmentSet("Trial1"))
        val unknownDetailsHistoryEntry = treatmentHistoryEntry(isTrial = true, startYear = 2022)
        val hasCyclesStopReasonHistoryEntry = treatmentHistoryEntry(
            isTrial = true,
            startYear = 2022,
            numCycles = 3,
            stopReasonDetail = "toxicity",
            treatments = drugTreatmentSet("Trial1")
        )
        val hasAcronymHistoryEntry = treatmentHistoryEntry(
            isTrial = true,
            trialAcronym = "tr2",
            startYear = 2022,
            numCycles = 3,
            stopReasonDetail = "toxicity",
            treatments = drugTreatmentSet("Trial2")
        )
        val hasSingleIntentHistoryEntry = treatmentHistoryEntry(
            isTrial = true,
            startYear = 2022,
            numCycles = 1,
            stopReasonDetail = "toxicity",
            treatments = drugTreatmentSet("Trial4"),
            intents = setOf(Intent.ADJUVANT)
        )
        val hasMultipleIntentsHistoryEntry = treatmentHistoryEntry(
            isTrial = true,
            startYear = 2022,
            stopReasonDetail = "toxicity",
            treatments = drugTreatmentSet("Trial5"),
            intents = setOf(Intent.ADJUVANT, Intent.CONSOLIDATION)
        )
        return listOf(
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

    private fun drugTreatmentSet(name: String): Set<DrugTreatment> {
        return setOf(
            DrugTreatment(
                name = name, drugs = setOf(drug("IRINOTECAN", DrugType.TOPO1_INHIBITOR, TreatmentCategory.CHEMOTHERAPY))
            )
        )
    }

    private fun createTestPriorSecondPrimaries(): List<PriorSecondPrimary> {
        return listOf(
            PriorSecondPrimary(
                tumorLocation = "Lung",
                tumorSubLocation = "",
                tumorType = "Carcinoma",
                tumorSubType = "Adenocarcinoma",
                doids = setOf("3905"),
                diagnosedYear = FIXED_DATE.year - YEARS_SINCE_SECOND_PRIMARY_DIAGNOSIS,
                diagnosedMonth = FIXED_DATE.monthValue,
                treatmentHistory = "Surgery",
                status = TumorStatus.INACTIVE,
                lastTreatmentYear = null,
                lastTreatmentMonth = null
            )
        )
    }

    private fun createTestPriorOtherConditions(): List<PriorOtherCondition> {
        return listOf(
            PriorOtherCondition(
                name = "pancreatitis",
                doids = setOf("4989"),
                category = "Pancreas disease",
                icdMainCode = "DC31",
                icdExtensionCode = null,
                isContraindicationForTherapy = true,
                year = null,
                month = null
            ),
            PriorOtherCondition(
                name = "Coronary artery bypass graft (CABG)",
                doids = setOf("3393"),
                category = "Heart disease",
                icdMainCode = "QB50.1",
                icdExtensionCode = null,
                isContraindicationForTherapy = true,
                year = 2023,
                month = 10
            )
        )
    }

    fun createTestPriorMolecularTests(): List<PriorIHCTest> {
        return listOf(
            PriorIHCTest(
                item = "EGFR",
                measure = null,
                scoreText = "c.2240_2254del",
                scoreValuePrefix = null,
                scoreValue = null,
                scoreValueUnit = null,
                impliesPotentialIndeterminateStatus = false
            ),
            PriorIHCTest(
                item = "Something",
                measure = null,
                scoreText = "GEEN mutaties aangetoond",
                scoreValuePrefix = null,
                scoreValue = null,
                scoreValueUnit = null,
                impliesPotentialIndeterminateStatus = false
            ),
            PriorIHCTest(
                item = "PD-L1",
                measure = null,
                scoreText = null,
                scoreValuePrefix = null,
                scoreValue = 90.0,
                scoreValueUnit = "%",
                impliesPotentialIndeterminateStatus = false
            ),
            PriorIHCTest(
                item = "HER2",
                measure = null,
                scoreText = "Positive",
                scoreValuePrefix = null,
                scoreValue = null,
                scoreValueUnit = null,
                impliesPotentialIndeterminateStatus = false
            ),
            PriorIHCTest(
                item = "FGFR3::TACC3",
                measure = null,
                scoreText = "Positive",
                scoreValuePrefix = null,
                scoreValue = null,
                scoreValueUnit = null,
                impliesPotentialIndeterminateStatus = false
            )
        )
    }

    private fun createTestComplications(): List<Complication> {
        return listOf(Complication(name = "Ascites", categories = setOf("Ascites"), icdCode = "1A01", year = null, month = null))
    }

    private fun createTestLabValues(): List<LabValue> {
        return listOf(
            LabValue(
                date = FIXED_DATE.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3.toLong()),
                code = "ASAT",
                name = "Aspartate aminotransferase",
                comparator = "",
                value = 36.0,
                unit = LabUnit.UNITS_PER_LITER,
                refLimitUp = 33.0,
                isOutsideRef = true,
                refLimitLow = null
            ),
            LabValue(
                date = FIXED_DATE.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3.toLong()),
                code = "Hb",
                name = "Hemoglobin",
                comparator = "",
                value = 5.5,
                unit = LabUnit.MILLIMOLES_PER_LITER,
                refLimitLow = 6.5,
                refLimitUp = 9.5,
                isOutsideRef = true
            ),
            LabValue(
                date = FIXED_DATE.minusDays(DAYS_SINCE_LAB_MEASUREMENT_1.toLong()),
                code = "THROMBO-ABS",
                name = "Thrombocytes",
                comparator = "",
                value = 155.0,
                unit = LabUnit.BILLIONS_PER_LITER,
                refLimitLow = 155.0,
                refLimitUp = 350.0,
                isOutsideRef = false
            ),
            LabValue(
                date = FIXED_DATE.minusDays(DAYS_SINCE_LAB_MEASUREMENT_2.toLong()),
                code = "THROMBO-ABS",
                name = "Thrombocytes",
                comparator = "",
                value = 151.0,
                unit = LabUnit.BILLIONS_PER_LITER,
                refLimitLow = 155.0,
                refLimitUp = 350.0,
                isOutsideRef = true
            ),
            LabValue(
                date = FIXED_DATE.minusDays(DAYS_SINCE_LAB_MEASUREMENT_3.toLong()),
                code = "THROMBO-ABS",
                name = "Thrombocytes",
                comparator = "",
                value = 150.0,
                unit = LabUnit.BILLIONS_PER_LITER,
                refLimitLow = 155.0,
                refLimitUp = 350.0,
                isOutsideRef = true
            ),
            LabValue(
                date = FIXED_DATE.minusDays(DAYS_SINCE_LAB_MEASUREMENT_1.toLong()),
                code = "LEUKO-ABS",
                name = "Leukocytes",
                comparator = "",
                value = 6.5,
                unit = LabUnit.BILLIONS_PER_LITER,
                refLimitLow = 3.0,
                refLimitUp = 10.0,
                isOutsideRef = false
            ),
            LabValue(
                date = FIXED_DATE.minusDays(DAYS_SINCE_LAB_MEASUREMENT_1.toLong()),
                code = "CKD-EPIeGFR",
                name = "CKD-EPI eGFR",
                comparator = ">",
                value = 100.0,
                unit = LabUnit.MILLILITERS_PER_MINUTE,
                refLimitLow = 100.0,
                refLimitUp = null,
                isOutsideRef = false
            ),
            LabValue(
                date = FIXED_DATE.minusDays(DAYS_SINCE_LAB_MEASUREMENT_2.toLong()),
                code = "LDH",
                name = "Lactate dehydrogenase",
                comparator = "",
                value = 240.0,
                unit = LabUnit.UNITS_PER_LITER,
                refLimitUp = 245.0,
                refLimitLow = null,
                isOutsideRef = false
            ),
        )
    }

    private fun createTestToxicities(): List<Toxicity> {
        return listOf(
            Toxicity(
                name = "Nausea",
                categories = setOf("Nausea"),
                icdCode = "A01",
                evaluatedDate = FIXED_DATE.minusDays(DAYS_SINCE_TOXICITIES.toLong()),
                source = ToxicitySource.EHR,
                grade = 1
            ),
            Toxicity(
                name = "Fatigue",
                categories = setOf("Fatigue"),
                icdCode = "A02",
                evaluatedDate = FIXED_DATE.minusDays(DAYS_SINCE_TOXICITIES.toLong()),
                source = ToxicitySource.QUESTIONNAIRE,
                grade = 2
            )
        )
    }

    private fun createTestIntolerances(): List<Intolerance> {
        return listOf(
            Intolerance(
                name = "Wasps",
                icdCode = "icdCode",
                category = "Environment",
                type = "Allergy",
                clinicalStatus = "Active",
                verificationStatus = "Confirmed",
                criticality = "Unable-to-assess",
                doids = emptySet(),
                subcategories = emptySet(),
                treatmentCategories = emptySet()
            )
        )
    }

    private fun createTestSurgeries(): List<Surgery> {
        return listOf(
            Surgery(name = "Surgery 1", endDate = FIXED_DATE.minusDays(DAYS_SINCE_SURGERY.toLong()), status = SurgeryStatus.FINISHED),
            Surgery(name = "Surgery 2", endDate = FIXED_DATE.minusDays(DAYS_SINCE_SURGERY_2.toLong()), status = SurgeryStatus.FINISHED)
        )
    }

    private fun createTestBodyWeights(): List<BodyWeight> {
        return listOf(
            BodyWeight(date = FIXED_DATETIME.minusDays(DAYS_SINCE_BODY_WEIGHT_1.toLong()), value = 70.0, unit = "Kilogram", valid = true),
            BodyWeight(date = FIXED_DATETIME.minusDays(DAYS_SINCE_BODY_WEIGHT_2.toLong()), value = 68.0, unit = "Kilogram", valid = true)
        )
    }

    private fun createTestVitalFunctions(): List<VitalFunction> {
        return listOf(
            VitalFunction(
                date = FIXED_DATETIME.minusDays(DAYS_SINCE_BLOOD_PRESSURE.toLong()),
                category = VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE,
                subcategory = "Mean blood pressure",
                value = 99.0,
                unit = "mm[Hg]",
                valid = true
            )
        )
    }

    private fun createTestBloodTransfusions(): List<BloodTransfusion> {
        return listOf(BloodTransfusion(date = FIXED_DATE.minusDays(DAYS_SINCE_BLOOD_TRANSFUSION.toLong()), product = "Thrombocyte concentrate"))
    }

    private fun createTestMedications(): List<Medication> {
        return listOf(
            TestMedicationFactory.createMinimal().copy(
                name = "Ibuprofen",
                status = MedicationStatus.ACTIVE,
                dosage = Dosage(
                    dosageMin = 750.0,
                    dosageMax = 1000.0,
                    dosageUnit = "mg",
                    frequency = 1.0,
                    frequencyUnit = "day",
                    ifNeeded = false
                ),
                startDate = FIXED_DATE.minusDays(DAYS_SINCE_MEDICATION_START.toLong()),
                stopDate = FIXED_DATE.plusDays(DAYS_UNTIL_MEDICATION_END.toLong()),
                isSelfCare = false,
                isTrialMedication = false,
            ),
            TestMedicationFactory.createMinimal().copy(
                name = "Prednison",
                status = MedicationStatus.ACTIVE,
                dosage = Dosage(
                    dosageMin = 750.0,
                    dosageMax = 1000.0,
                    dosageUnit = "mg",
                    frequency = 1.0,
                    frequencyUnit = "day",
                    periodBetweenUnit = "months",
                    periodBetweenValue = 2.0,
                    ifNeeded = false
                ),
                startDate = FIXED_DATE.minusDays(DAYS_SINCE_MEDICATION_START.toLong()),
                stopDate = FIXED_DATE.plusDays(DAYS_UNTIL_MEDICATION_END.toLong()),
                isSelfCare = false,
                isTrialMedication = false,
            )
        )
    }
}
