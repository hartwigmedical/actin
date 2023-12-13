package com.hartwig.actin.clinical

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.extraction.BloodTransfusionsExtractor
import com.hartwig.actin.clinical.curation.extraction.ClinicalStatusExtractor
import com.hartwig.actin.clinical.curation.extraction.ComplicationsExtractor
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.extraction.IntoleranceExtractor
import com.hartwig.actin.clinical.curation.extraction.LabValueExtractor
import com.hartwig.actin.clinical.curation.extraction.MedicationExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorMolecularTestsExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorOtherConditionsExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorSecondPrimaryExtractor
import com.hartwig.actin.clinical.curation.extraction.ToxicityExtractor
import com.hartwig.actin.clinical.curation.extraction.TreatmentHistoryExtractor
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

class ClinicalIngestion(private val feed: FeedModel, private val curation: CurationDatabase, atc: AtcModel) {
    private val tumorDetailsExtractor = TumorDetailsExtractor(curation)
    private val complicationsExtractor = ComplicationsExtractor(curation)
    private val clinicalStatusExtractor = ClinicalStatusExtractor(curation)
    private val treatmentHistoryExtractor = TreatmentHistoryExtractor(curation)
    private val priorSecondPrimaryExtractor = PriorSecondPrimaryExtractor(curation)
    private val priorOtherConditionExtractor = PriorOtherConditionsExtractor(curation)
    private val priorMolecularTestsExtractor = PriorMolecularTestsExtractor(curation)
    private val labValueExtractor = LabValueExtractor(curation)
    private val toxicityExtractor = ToxicityExtractor(curation)
    private val intoleranceExtractor = IntoleranceExtractor(curation, atc)
    private val medicationExtractor = MedicationExtractor(curation, atc)
    private val bloodTransfusionsExtractor = BloodTransfusionsExtractor(curation)

    fun run(): List<IngestionResult> {
        val processedPatientIds: MutableSet<String> = HashSet()

        LOGGER.info("Creating clinical model")
        val records = feed.subjects().map { subject ->
            val patientId = toPatientId(subject)
            check(!processedPatientIds.contains(patientId)) { "Cannot create clinical records. Duplicate patientId: $patientId" }
            processedPatientIds.add(patientId)
            LOGGER.info(" Extracting and curating data for patient {}", patientId)

            val questionnaire = feed.latestQuestionnaireEntry(subject)?.let { QuestionnaireExtraction.extract(it) }
            val tumorExtraction = tumorDetailsExtractor.extract(patientId, questionnaire)
            val complicationsExtraction = complicationsExtractor.extract(patientId, questionnaire)
            val clinicalStatusExtraction =
                clinicalStatusExtractor.extract(patientId, questionnaire, complicationsExtraction.extracted?.isNotEmpty())
            val treatmentHistoryExtraction = treatmentHistoryExtractor.extract(patientId, questionnaire)
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
                .clinicalStatus(clinicalStatusExtraction.extracted)
                .treatmentHistory(treatmentHistoryExtraction.extracted)
                .priorSecondPrimaries(priorSecondPrimaryExtraction.extracted)
                .priorOtherConditions(priorOtherConditionsExtraction.extracted)
                .priorMolecularTests(priorMolecularTestsExtraction.extracted)
                .complications(complicationsExtraction.extracted)
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
                treatmentHistoryExtraction,
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

            Pair(IngestionResult.create(questionnaire, record, patientEvaluation.warnings.toList()), patientEvaluation)
        }.sortedWith { (result1, _), (result2, _) ->
            ClinicalRecordComparator().compare(
                result1.clinicalRecord,
                result2.clinicalRecord
            )
        }

        LOGGER.info("Evaluating curation database")
        curation.evaluate(records.fold(ExtractionEvaluation()) { acc, (_, eval) -> acc + eval }, LOGGER)

        return records.map { it.first }
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
                .build()
        }
    }

    private fun extractVitalFunctions(subject: String): List<VitalFunction> {
        return feed.vitalFunctionEntries(subject).map { entry: VitalFunctionEntry ->
            ImmutableVitalFunction.builder()
                .date(entry.effectiveDateTime)
                .category(VitalFunctionExtraction.determineCategory(entry.codeDisplayOriginal))
                .subcategory(entry.componentCodeDisplay)
                .value(entry.quantityValue)
                .unit(entry.quantityUnit)
                .build()
        }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ClinicalIngestion::class.java)

        @VisibleForTesting
        fun toPatientId(subject: String): String {
            var adjusted = subject
            // Subjects have been passed with unexpected subject IDs in the past (e.g. without ACTN prefix)
            if (subject.length == 10 && !subject.startsWith("ACTN")) {
                LOGGER.warn("Suspicious subject detected. Pre-fixing with 'ACTN': {}", subject)
                adjusted = "ACTN$subject"
            }
            return adjusted.replace("-".toRegex(), "")
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
    }
}