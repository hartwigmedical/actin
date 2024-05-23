package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ClinicalIngestionFeedAdapter
import com.hartwig.actin.clinical.PatientIngestionResult
import com.hartwig.actin.clinical.correction.QuestionnaireCorrection
import com.hartwig.actin.clinical.correction.QuestionnaireRawEntryMapper
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.extraction.BloodTransfusionsExtractor
import com.hartwig.actin.clinical.curation.extraction.ClinicalStatusExtractor
import com.hartwig.actin.clinical.curation.extraction.ComplicationsExtractor
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.extraction.IntoleranceExtractor
import com.hartwig.actin.clinical.curation.extraction.LabValueExtractor
import com.hartwig.actin.clinical.curation.extraction.MedicationExtractor
import com.hartwig.actin.clinical.curation.extraction.OncologicalHistoryExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorMolecularTestsExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorOtherConditionsExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorSecondPrimaryExtractor
import com.hartwig.actin.clinical.curation.extraction.ToxicityExtractor
import com.hartwig.actin.clinical.curation.extraction.TumorDetailsExtractor
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.HEART_RATE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.SPO2
import com.hartwig.actin.clinical.feed.ClinicalFeedIngestion
import com.hartwig.actin.clinical.feed.emc.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.emc.lab.LabExtraction
import com.hartwig.actin.clinical.feed.emc.patient.PatientEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireExtraction
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionExtraction
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.doid.DoidModel
import org.apache.logging.log4j.LogManager

class EmcClinicalFeedIngestor(
    private val feed: FeedModel,
    private val tumorDetailsExtractor: TumorDetailsExtractor,
    private val complicationsExtractor: ComplicationsExtractor,
    private val clinicalStatusExtractor: ClinicalStatusExtractor,
    private val oncologicalHistoryExtractor: OncologicalHistoryExtractor,
    private val priorSecondPrimaryExtractor: PriorSecondPrimaryExtractor,
    private val priorOtherConditionExtractor: PriorOtherConditionsExtractor,
    private val priorMolecularTestsExtractor: PriorMolecularTestsExtractor,
    private val labValueExtractor: LabValueExtractor,
    private val toxicityExtractor: ToxicityExtractor,
    private val intoleranceExtractor: IntoleranceExtractor,
    private val medicationExtractor: MedicationExtractor,
    private val bloodTransfusionsExtractor: BloodTransfusionsExtractor,
) : ClinicalFeedIngestion {

    override fun ingest(): List<Pair<PatientIngestionResult, CurationExtractionEvaluation>> {
        LOGGER.info("Creating clinical model")
        return feed.read().map { feedRecord ->
            val patientId = feedRecord.patientEntry.subject
            LOGGER.info(" Extracting and curating data for patient {}", patientId)

            val (questionnaire, questionnaireCurationErrors) = QuestionnaireExtraction.extract(feedRecord.latestQuestionnaireEntry)
            val tumorExtraction = tumorDetailsExtractor.extract(patientId, questionnaire)
            val complicationsExtraction = complicationsExtractor.extract(patientId, questionnaire)
            val clinicalStatusExtraction =
                clinicalStatusExtractor.extract(patientId, questionnaire, complicationsExtraction.extracted?.isNotEmpty())
            val oncologicalHistoryExtraction = oncologicalHistoryExtractor.extract(patientId, questionnaire)
            val priorSecondPrimaryExtraction = priorSecondPrimaryExtractor.extract(patientId, questionnaire)
            val priorOtherConditionsExtraction = priorOtherConditionExtractor.extract(patientId, questionnaire)
            val priorMolecularTestsExtraction = priorMolecularTestsExtractor.extract(patientId, questionnaire)
            val labValuesExtraction = labValueExtractor.extract(patientId, feedRecord.labEntries.map { LabExtraction.extract(it) })
            val toxicityExtraction = toxicityExtractor.extract(patientId, feedRecord.toxicityEntries, questionnaire)
            val intoleranceExtraction = intoleranceExtractor.extract(patientId, feedRecord.intoleranceEntries)
            val bloodTransfusionsExtraction = bloodTransfusionsExtractor.extract(patientId, feedRecord.bloodTransfusionEntries)
            val medicationExtraction = medicationExtractor.extract(patientId, feedRecord.medicationEntries)

            val record = ClinicalRecord(
                patientId = patientId,
                patient = extractPatientDetails(feedRecord.patientEntry, questionnaire),
                tumor = tumorExtraction.extracted,
                complications = complicationsExtraction.extracted,
                clinicalStatus = clinicalStatusExtraction.extracted,
                oncologicalHistory = oncologicalHistoryExtraction.extracted,
                priorSecondPrimaries = priorSecondPrimaryExtraction.extracted,
                priorOtherConditions = priorOtherConditionsExtraction.extracted,
                priorMolecularTests = priorMolecularTestsExtraction.extracted,
                labValues = labValuesExtraction.extracted,
                toxicities = toxicityExtraction.extracted,
                intolerances = intoleranceExtraction.extracted,
                surgeries = extractSurgeries(feedRecord),
                bodyWeights = extractBodyWeights(feedRecord),
                bodyHeights = emptyList(),
                vitalFunctions = extractVitalFunctions(feedRecord),
                bloodTransfusions = bloodTransfusionsExtraction.extracted,
                medications = medicationExtraction.extracted
            )

            val patientEvaluation = listOf(
                tumorExtraction,
                complicationsExtraction,
                clinicalStatusExtraction,
                oncologicalHistoryExtraction,
                priorSecondPrimaryExtraction,
                priorOtherConditionsExtraction,
                priorMolecularTestsExtraction,
                labValuesExtraction,
                toxicityExtraction,
                intoleranceExtraction,
                bloodTransfusionsExtraction,
                medicationExtraction
            )
                .map { it.evaluation }
                .fold(CurationExtractionEvaluation()) { acc, evaluation -> acc + evaluation }

            Pair(
                PatientIngestionResult.create(
                    questionnaire,
                    record,
                    patientEvaluation.warnings.toList(),
                    questionnaireCurationErrors.toSet(),
                    feedRecord.validationWarnings
                ), patientEvaluation
            )
        }
    }

    private fun extractPatientDetails(patient: PatientEntry, questionnaire: Questionnaire?): PatientDetails {
        return PatientDetails(
            gender = patient.gender,
            birthYear = patient.birthYear,
            registrationDate = patient.periodStart,
            questionnaireDate = questionnaire?.date
        )
    }

    private fun extractSurgeries(feedRecord: FeedRecord): List<Surgery> {
        return feedRecord.uniqueSurgeryEntries
            .map { Surgery(endDate = it.periodEnd, status = resolveSurgeryStatus(it.encounterStatus)) }
    }

    private fun extractBodyWeights(feedRecord: FeedRecord): List<BodyWeight> {
        return feedRecord.uniqueBodyWeightEntries.map { entry: BodyWeightEntry ->
            BodyWeight(
                date = entry.effectiveDateTime,
                value = entry.valueQuantityValue,
                unit = entry.valueQuantityUnit,
                valid = bodyWeightIsValid(entry)
            )
        }
    }

    private fun bodyWeightIsValid(entry: BodyWeightEntry): Boolean {
        return entry.valueQuantityUnit.lowercase() in BODY_WEIGHT_EXPECTED_UNIT
                && entry.valueQuantityValue in BODY_WEIGHT_MIN..BODY_WEIGHT_MAX
    }

    private fun extractVitalFunctions(feedRecord: FeedRecord): List<VitalFunction> {
        return feedRecord.uniqueVitalFunctionEntries.map { entry ->
            VitalFunction(
                date = entry.effectiveDateTime,
                category = VitalFunctionExtraction.determineCategory(entry.codeDisplayOriginal),
                subcategory = entry.componentCodeDisplay,
                value = safeQuantityValue(entry),
                unit = entry.quantityUnit,
                valid = vitalFunctionIsValid(entry)
            )
        }
    }

    private fun vitalFunctionIsValid(entry: VitalFunctionEntry): Boolean {
        return when (VitalFunctionExtraction.determineCategory(entry.codeDisplayOriginal)) {
            NON_INVASIVE_BLOOD_PRESSURE, ARTERIAL_BLOOD_PRESSURE -> {
                safeQuantityValue(entry) in BLOOD_PRESSURE_MIN..BLOOD_PRESSURE_MAX && entry.quantityUnit.lowercase() == BLOOD_PRESSURE_EXPECTED_UNIT
            }

            HEART_RATE -> {
                safeQuantityValue(entry) in HEART_RATE_MIN..HEART_RATE_MAX && entry.quantityUnit.lowercase() == HEART_RATE_EXPECTED_UNIT
            }

            SPO2 -> {
                safeQuantityValue(entry) in SPO2_MIN..SPO2_MAX && entry.quantityUnit.lowercase() == SPO2_EXPECTED_UNIT
            }

            else -> {
                false
            }
        }
    }

    private fun safeQuantityValue(entry: VitalFunctionEntry) = (entry.quantityValue
        ?: Double.NaN)

    private fun resolveSurgeryStatus(status: String): SurgeryStatus {
        val valueToFind = status.trim { it <= ' ' }.replace("-".toRegex(), "_")
        for (option in SurgeryStatus.values()) {
            if (option.toString().equals(valueToFind, ignoreCase = true)) {
                return option
            }
        }
        LOGGER.warn("Could not resolve surgery status '{}'", status)
        return SurgeryStatus.UNKNOWN
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ClinicalIngestionFeedAdapter::class.java)

        fun create(
            feedDirectory: String,
            curationDirectory: String,
            curationDatabaseContext: CurationDatabaseContext,
            atcModel: AtcModel,
            doidModel: DoidModel
        ) = EmcClinicalFeedIngestor(
            feed = FeedModel(
                ClinicalFeedReader.read(feedDirectory).copy(
                    questionnaireEntries = QuestionnaireCorrection.correctQuestionnaires(
                        ClinicalFeedReader.read(feedDirectory).questionnaireEntries,
                        QuestionnaireRawEntryMapper.createFromCurationDirectory(curationDirectory)
                    )
                )
            ),
            tumorDetailsExtractor = TumorDetailsExtractor.create(curationDatabaseContext, TumorStageDeriver.create(doidModel)),
            complicationsExtractor = ComplicationsExtractor.create(curationDatabaseContext),
            clinicalStatusExtractor = ClinicalStatusExtractor.create(curationDatabaseContext),
            oncologicalHistoryExtractor = OncologicalHistoryExtractor.create(curationDatabaseContext),
            priorSecondPrimaryExtractor = PriorSecondPrimaryExtractor.create(curationDatabaseContext),
            priorOtherConditionExtractor = PriorOtherConditionsExtractor.create(curationDatabaseContext),
            priorMolecularTestsExtractor = PriorMolecularTestsExtractor.create(curationDatabaseContext),
            labValueExtractor = LabValueExtractor.create(curationDatabaseContext),
            toxicityExtractor = ToxicityExtractor.create(curationDatabaseContext),
            intoleranceExtractor = IntoleranceExtractor.create(curationDatabaseContext, atcModel),
            medicationExtractor = MedicationExtractor.create(curationDatabaseContext, atcModel),
            bloodTransfusionsExtractor = BloodTransfusionsExtractor.create(curationDatabaseContext),
        )

        const val BODY_WEIGHT_MIN = 20.0
        const val BODY_WEIGHT_MAX = 300.0
        internal val BODY_WEIGHT_EXPECTED_UNIT = listOf("kilogram", "kilograms")
        const val HEART_RATE_MIN = 10.0
        const val HEART_RATE_MAX = 300.0
        const val HEART_RATE_EXPECTED_UNIT = "bpm"
        const val BLOOD_PRESSURE_MIN = 10.0
        const val BLOOD_PRESSURE_MAX = 300.0
        const val BLOOD_PRESSURE_EXPECTED_UNIT = "mmhg"
        const val SPO2_MIN = 10.0
        const val SPO2_MAX = 100.0
        const val SPO2_EXPECTED_UNIT = "percent"
    }
}