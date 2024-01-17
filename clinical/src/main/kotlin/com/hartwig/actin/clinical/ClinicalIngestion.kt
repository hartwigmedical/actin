package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.extraction.BloodTransfusionsExtractor
import com.hartwig.actin.clinical.curation.extraction.ClinicalStatusExtractor
import com.hartwig.actin.clinical.curation.extraction.ComplicationsExtractor
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
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
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.HEART_RATE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.SPO2
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.lab.LabExtraction
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionExtraction
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator
import org.apache.logging.log4j.LogManager

data class ExtractionResult<T>(val extracted: T, val evaluation: ExtractionEvaluation)

class ClinicalIngestion(
    private val feed: FeedModel,
    private val curationDatabaseContext: CurationDatabaseContext,
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
) {

    fun run(): IngestionResult {
        val processedPatientIds: MutableSet<String> = HashSet()

        LOGGER.info("Creating clinical model")
        val records = feed.subjects().map { subject ->
            val patientId = subject.replace("-".toRegex(), "")
            check(!processedPatientIds.contains(patientId)) { "Cannot create clinical records. Duplicate patientId: $patientId" }
            processedPatientIds.add(patientId)
            LOGGER.info(" Extracting and curating data for patient {}", patientId)

            val (questionnaire, questionnaireCurationErrors) = QuestionnaireExtraction.extract(feed.latestQuestionnaireEntry(subject))
            val tumorExtraction = tumorDetailsExtractor.extract(patientId, questionnaire)
            val complicationsExtraction = complicationsExtractor.extract(patientId, questionnaire)
            val clinicalStatusExtraction =
                clinicalStatusExtractor.extract(patientId, questionnaire, complicationsExtraction.extracted?.isNotEmpty())
            val oncologicalHistoryExtraction = oncologicalHistoryExtractor.extract(patientId, questionnaire)
            val priorSecondPrimaryExtraction = priorSecondPrimaryExtractor.extract(patientId, questionnaire)
            val priorOtherConditionsExtraction = priorOtherConditionExtractor.extract(patientId, questionnaire)
            val priorMolecularTestsExtraction = priorMolecularTestsExtractor.extract(patientId, questionnaire)
            val labValuesExtraction = labValueExtractor.extract(patientId, feed.labEntries(subject).map { LabExtraction.extract(it) })
            val toxicityExtraction = toxicityExtractor.extract(patientId, feed.toxicityEntries(subject), questionnaire)
            val intoleranceExtraction = intoleranceExtractor.extract(patientId, feed.intoleranceEntries(subject))
            val bloodTransfusionsExtraction = bloodTransfusionsExtractor.extract(patientId, feed.bloodTransfusionEntries(subject))
            val medicationExtraction = medicationExtractor.extract(patientId, feed.medicationEntries(subject))

            val record = ImmutableClinicalRecord.builder()
                .patientId(patientId)
                .patient(extractPatientDetails(subject, questionnaire))
                .tumor(tumorExtraction.extracted)
                .complications(complicationsExtraction.extracted)
                .clinicalStatus(clinicalStatusExtraction.extracted)
                .oncologicalHistory(oncologicalHistoryExtraction.extracted)
                .priorSecondPrimaries(priorSecondPrimaryExtraction.extracted)
                .priorOtherConditions(priorOtherConditionsExtraction.extracted)
                .priorMolecularTests(priorMolecularTestsExtraction.extracted)
                .labValues(labValuesExtraction.extracted)
                .toxicities(toxicityExtraction.extracted)
                .intolerances(intoleranceExtraction.extracted)
                .surgeries(extractSurgeries(subject))
                .bodyWeights(extractBodyWeights(subject))
                .vitalFunctions(extractVitalFunctions(subject))
                .bloodTransfusions(bloodTransfusionsExtraction.extracted)
                .medications(medicationExtraction.extracted)
                .build()

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
                .fold(ExtractionEvaluation()) { acc, evaluation -> acc + evaluation }

            Pair(
                PatientIngestionResult.create(
                    questionnaire,
                    record,
                    patientEvaluation.warnings.toList(),
                    questionnaireCurationErrors.toSet(),
                    feed.validationWarnings(subject)
                ), patientEvaluation
            )
        }.sortedWith { (result1, _), (result2, _) ->
            ClinicalRecordComparator().compare(
                result1.clinicalRecord,
                result2.clinicalRecord
            )
        }
        return IngestionResult(
            unusedConfigs = curationDatabaseContext.allUnusedConfig(records.map { it.second }),
            patientResults = records.map { it.first }
        )
    }

    private fun extractPatientDetails(subject: String, questionnaire: Questionnaire?): PatientDetails {
        val patient: PatientEntry = feed.patientEntry(subject)
        return ImmutablePatientDetails.builder()
            .gender(patient.gender)
            .birthYear(patient.birthYear)
            .registrationDate(patient.periodStart)
            .questionnaireDate(questionnaire?.date)
            .otherMolecularPatientId(questionnaire?.genayaSubjectNumber)
            .build()
    }

    private fun extractSurgeries(subject: String): List<Surgery> {
        return feed.uniqueSurgeryEntries(subject)
            .map { ImmutableSurgery.builder().endDate(it.periodEnd).status(resolveSurgeryStatus(it.encounterStatus)).build() }
    }

    private fun extractBodyWeights(subject: String): List<BodyWeight> {
        return feed.uniqueBodyWeightEntries(subject).map { entry: BodyWeightEntry ->
            ImmutableBodyWeight.builder()
                .date(entry.effectiveDateTime)
                .value(entry.valueQuantityValue)
                .unit(entry.valueQuantityUnit)
                .valid(bodyWeightIsValid(entry))
                .build()
        }
    }

    private fun bodyWeightIsValid(entry: BodyWeightEntry): Boolean {
        return entry.valueQuantityUnit.lowercase() == "kilogram" && entry.valueQuantityValue in BODY_WEIGHT_MIN..BODY_WEIGHT_MAX
    }

    private fun extractVitalFunctions(subject: String): List<VitalFunction> {
        return feed.vitalFunctionEntries(subject).map { entry: VitalFunctionEntry ->
            val category = VitalFunctionExtraction.determineCategory(entry.codeDisplayOriginal)
            ImmutableVitalFunction.builder()
                .date(entry.effectiveDateTime)
                .category(category)
                .subcategory(entry.componentCodeDisplay)
                .value(entry.quantityValue ?: Double.NaN)
                .unit(entry.quantityUnit)
                .valid(vitalFunctionIsValid(entry))
                .build()
        }
    }

    private fun vitalFunctionIsValid(entry: VitalFunctionEntry): Boolean {
        return when (VitalFunctionExtraction.determineCategory(entry.codeDisplayOriginal)) {
            NON_INVASIVE_BLOOD_PRESSURE, ARTERIAL_BLOOD_PRESSURE -> {
                entry.quantityValue in BLOOD_PRESSURE_MIN..BLOOD_PRESSURE_MAX && entry.quantityUnit.lowercase() == BLOOD_PRESSURE_EXPECTED_UNIT
            }

            HEART_RATE -> {
                entry.quantityValue in HEART_RATE_MIN..HEART_RATE_MAX && entry.quantityUnit.lowercase() == HEART_RATE_EXPECTED_UNIT
            }

            SPO2 -> {
                entry.quantityValue in SPO2_MIN..SPO2_MAX && entry.quantityUnit.lowercase() == SPO2_EXPECTED_UNIT
            }
            else -> {
                false
            }
        }
    }

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
        private val LOGGER = LogManager.getLogger(ClinicalIngestion::class.java)

        fun create(
            feedModel: FeedModel,
            curationDatabaseContext: CurationDatabaseContext,
            atcModel: WhoAtcModel
        ) = ClinicalIngestion(
            feed = feedModel,
            curationDatabaseContext = curationDatabaseContext,
            priorSecondPrimaryExtractor = PriorSecondPrimaryExtractor.create(curationDatabaseContext),
            tumorDetailsExtractor = TumorDetailsExtractor.create(curationDatabaseContext),
            complicationsExtractor = ComplicationsExtractor.create(curationDatabaseContext),
            clinicalStatusExtractor = ClinicalStatusExtractor.create(curationDatabaseContext),
            oncologicalHistoryExtractor = OncologicalHistoryExtractor.create(curationDatabaseContext),
            bloodTransfusionsExtractor = BloodTransfusionsExtractor.create(curationDatabaseContext),
            priorMolecularTestsExtractor = PriorMolecularTestsExtractor.create(curationDatabaseContext),
            toxicityExtractor = ToxicityExtractor.create(curationDatabaseContext),
            intoleranceExtractor = IntoleranceExtractor.create(curationDatabaseContext, atcModel),
            priorOtherConditionExtractor = PriorOtherConditionsExtractor.create(curationDatabaseContext),
            medicationExtractor = MedicationExtractor.create(curationDatabaseContext, atcModel),
            labValueExtractor = LabValueExtractor.create(curationDatabaseContext)
        )

        const val BODY_WEIGHT_MIN = 20.0
        const val BODY_WEIGHT_MAX = 300.0
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