package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ClinicalIngestionFeedAdapter
import com.hartwig.actin.clinical.DrugInteractionsDatabase
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionResult
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionStatus
import com.hartwig.actin.clinical.QtProlongatingDatabase
import com.hartwig.actin.clinical.correction.QuestionnaireCorrection
import com.hartwig.actin.clinical.correction.QuestionnaireRawEntryMapper
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.ClinicalFeedIngestion
import com.hartwig.actin.clinical.feed.curationResultsFromWarnings
import com.hartwig.actin.clinical.feed.emc.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.emc.extraction.BloodTransfusionsExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.ComorbidityExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.LabValueExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.MedicationExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.OncologicalHistoryExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.PriorMolecularTestsExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.PriorSecondPrimaryExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.SurgeryExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.TumorDetailsExtractor
import com.hartwig.actin.clinical.feed.emc.patient.PatientEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.ingestion.QuestionnaireCurationError
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireExtraction
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionExtraction
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.HEART_RATE
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.SPO2
import com.hartwig.actin.doid.DoidModel
import org.apache.logging.log4j.LogManager

class EmcClinicalFeedIngestor(
    private val feed: FeedModel,
    private val tumorDetailsExtractor: TumorDetailsExtractor,
    private val comorbidityExtractor: ComorbidityExtractor,
    private val oncologicalHistoryExtractor: OncologicalHistoryExtractor,
    private val priorSecondPrimaryExtractor: PriorSecondPrimaryExtractor,
    private val priorMolecularTestsExtractor: PriorMolecularTestsExtractor,
    private val labValueExtractor: LabValueExtractor,
    private val medicationExtractor: MedicationExtractor,
    private val bloodTransfusionsExtractor: BloodTransfusionsExtractor,
    private val surgeryExtractor: SurgeryExtractor
) : ClinicalFeedIngestion {

    override fun ingest(): List<Pair<PatientIngestionResult, CurationExtractionEvaluation>> {
        LOGGER.info("Creating clinical model")
        return feed.read().map { feedRecord ->
            val patientId = feedRecord.patientEntry.subject
            LOGGER.info(" Extracting and curating data for patient {}", patientId)

            val (questionnaire, questionnaireCurationErrors) = QuestionnaireExtraction.extract(feedRecord.latestQuestionnaireEntry)
            val tumorExtraction = tumorDetailsExtractor.extract(patientId, questionnaire)
            val comorbidityExtraction =
                comorbidityExtractor.extract(patientId, questionnaire, feedRecord.toxicityEntries, feedRecord.intoleranceEntries)
            val (comorbidities, clinicalStatus) = comorbidityExtraction.extracted
            val oncologicalHistoryExtraction = oncologicalHistoryExtractor.extract(patientId, questionnaire)
            val priorSecondPrimaryExtraction = priorSecondPrimaryExtractor.extract(patientId, questionnaire)
            val priorMolecularTestsExtraction = priorMolecularTestsExtractor.extract(patientId, questionnaire)
            val labValuesExtraction = labValueExtractor.extract(patientId, feedRecord.labEntries)
            val bloodTransfusionsExtraction = bloodTransfusionsExtractor.extract(patientId, feedRecord.bloodTransfusionEntries)
            val medicationExtraction = medicationExtractor.extract(patientId, feedRecord.medicationEntries)
            val surgeryExtraction = surgeryExtractor.extract(patientId, feedRecord.uniqueSurgeryEntries)

            val record = ClinicalRecord(
                patientId = patientId,
                patient = extractPatientDetails(feedRecord.patientEntry, questionnaire),
                tumor = tumorExtraction.extracted,
                comorbidities = comorbidities,
                clinicalStatus = clinicalStatus,
                oncologicalHistory = oncologicalHistoryExtraction.extracted,
                priorSecondPrimaries = priorSecondPrimaryExtraction.extracted,
                priorIHCTests = priorMolecularTestsExtraction.extracted,
                priorSequencingTests = emptyList(),
                labValues = labValuesExtraction.extracted,
                surgeries = surgeryExtraction.extracted,
                bodyWeights = extractBodyWeights(feedRecord),
                bodyHeights = emptyList(),
                vitalFunctions = extractVitalFunctions(feedRecord),
                bloodTransfusions = bloodTransfusionsExtraction.extracted,
                medications = medicationExtraction.extracted
            )

            val patientEvaluation = listOf(
                tumorExtraction,
                comorbidityExtraction,
                oncologicalHistoryExtraction,
                priorSecondPrimaryExtraction,
                priorMolecularTestsExtraction,
                labValuesExtraction,
                bloodTransfusionsExtraction,
                medicationExtraction,
                surgeryExtraction
            ).fold(CurationExtractionEvaluation()) { acc, current -> acc + current.evaluation }

            ingestionResult(questionnaire, record, patientEvaluation, questionnaireCurationErrors, feedRecord) to patientEvaluation
        }
    }

    private fun extractPatientDetails(patient: PatientEntry, questionnaire: Questionnaire?): PatientDetails {
        return PatientDetails(
            gender = patient.gender,
            birthYear = patient.birthYear,
            registrationDate = patient.periodStart,
            questionnaireDate = questionnaire?.date,
            hasHartwigSequencing = true
        )
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

    private fun ingestionResult(
        questionnaire: Questionnaire?,
        record: ClinicalRecord,
        patientEvaluation: CurationExtractionEvaluation,
        questionnaireCurationErrors: List<QuestionnaireCurationError>,
        feedRecord: FeedRecord
    ): PatientIngestionResult {
        val curationResults = curationResultsFromWarnings(patientEvaluation.warnings)

        val ingestionStatus = when {
            questionnaire == null -> PatientIngestionStatus.WARN_NO_QUESTIONNAIRE
            curationResults.isNotEmpty() -> PatientIngestionStatus.WARN_CURATION_REQUIRED
            else -> PatientIngestionStatus.PASS
        }

        return PatientIngestionResult(
            record.patientId,
            ingestionStatus,
            record,
            curationResults,
            questionnaireCurationErrors.toSet(),
            feedRecord.validationWarnings
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ClinicalIngestionFeedAdapter::class.java)

        fun create(
            feedDirectory: String,
            curationDirectory: String,
            curationDatabaseContext: CurationDatabaseContext,
            atcModel: AtcModel,
            drugInteractionsDatabase: DrugInteractionsDatabase,
            qtProlongatingDatabase: QtProlongatingDatabase,
            doidModel: DoidModel,
            treatmentDatabase: TreatmentDatabase
        ): EmcClinicalFeedIngestor {
            val feed = ClinicalFeedReader.read(feedDirectory)
            val correctedQuestionnaireEntries = QuestionnaireCorrection.correctQuestionnaires(
                feed.questionnaireEntries, QuestionnaireRawEntryMapper.createFromCurationDirectory(curationDirectory)
            )
            return EmcClinicalFeedIngestor(
                feed = FeedModel(feed.copy(questionnaireEntries = correctedQuestionnaireEntries)),
                tumorDetailsExtractor = TumorDetailsExtractor.create(curationDatabaseContext, TumorStageDeriver.create(doidModel)),
                comorbidityExtractor = ComorbidityExtractor.create(curationDatabaseContext),
                oncologicalHistoryExtractor = OncologicalHistoryExtractor.create(curationDatabaseContext),
                priorSecondPrimaryExtractor = PriorSecondPrimaryExtractor.create(curationDatabaseContext),
                priorMolecularTestsExtractor = PriorMolecularTestsExtractor.create(curationDatabaseContext),
                labValueExtractor = LabValueExtractor.create(curationDatabaseContext),
                medicationExtractor = MedicationExtractor.create(
                    curationDatabaseContext,
                    atcModel,
                    drugInteractionsDatabase,
                    qtProlongatingDatabase,
                    treatmentDatabase
                ),
                bloodTransfusionsExtractor = BloodTransfusionsExtractor.create(curationDatabaseContext),
                surgeryExtractor = SurgeryExtractor.create(curationDatabaseContext)
            )
        }

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