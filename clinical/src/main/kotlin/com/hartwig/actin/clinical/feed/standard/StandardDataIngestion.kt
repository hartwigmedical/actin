package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.DrugInteractionsDatabase
import com.hartwig.actin.clinical.QtProlongatingDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.ClinicalFeedIngestion
import com.hartwig.actin.clinical.feed.FeedModelJsonUtil.feedModelMapper
import com.hartwig.actin.clinical.feed.curationResultsFromWarnings
import com.hartwig.actin.clinical.feed.standard.extraction.PathologyReportsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBloodTransfusionExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBodyHeightExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBodyWeightExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardClinicalStatusExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardComorbidityExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardIhcTestExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardLabValuesExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardMedicationExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardOncologicalHistoryExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardPatientDetailsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardPriorPrimariesExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSurgeryExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardTumorDetailsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardVitalFunctionsExtractor
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionResult
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionStatus
import com.hartwig.actin.doid.DoidModel
import com.hartwig.feed.datamodel.FeedPatientRecord
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class StandardDataIngestion(
    private val directory: String,
    private val medicationExtractor: StandardMedicationExtractor,
    private val surgeryExtractor: StandardSurgeryExtractor,
    private val vitalFunctionsExtractor: StandardVitalFunctionsExtractor,
    private val bloodTransfusionExtractor: StandardBloodTransfusionExtractor,
    private val labValuesExtractor: StandardLabValuesExtractor,
    private val comorbidityExtractor: StandardComorbidityExtractor,
    private val treatmentHistoryExtractor: StandardOncologicalHistoryExtractor,
    private val clinicalStatusExtractor: StandardClinicalStatusExtractor,
    private val tumorDetailsExtractor: StandardTumorDetailsExtractor,
    private val secondPrimaryExtractor: StandardPriorPrimariesExtractor,
    private val patientDetailsExtractor: StandardPatientDetailsExtractor,
    private val bodyWeightExtractor: StandardBodyWeightExtractor,
    private val bodyHeightExtractor: StandardBodyHeightExtractor,
    private val ihcTestExtractor: StandardIhcTestExtractor,
    private val sequencingTestExtractor: StandardSequencingTestExtractor,
    private val pathologyReportsExtractor: PathologyReportsExtractor
) : ClinicalFeedIngestion {

    override fun ingest(): List<Triple<ClinicalRecord, PatientIngestionResult, CurationExtractionEvaluation>> {
        return Paths.get(directory).listDirectoryEntries()
            .filter { it.name.endsWith("json") }
            .map(::recordAndEvaluationFromPath)
            .map { (record, evaluation) ->
                Triple(
                    record,
                    PatientIngestionResult(
                        record.patientId,
                        record.patient.registrationDate,
                        if (evaluation.warnings.isEmpty()) PatientIngestionStatus.PASS else PatientIngestionStatus.WARN,
                        curationResultsFromWarnings(evaluation.warnings),
                        emptySet(),
                        emptySet()
                    ),
                    evaluation
                )
            }
    }

    private fun recordAndEvaluationFromPath(file: Path): Pair<ClinicalRecord, CurationExtractionEvaluation> {
        val ehrPatientRecord = feedModelMapper.readValue(Files.readString(file), FeedPatientRecord::class.java)

        val patientDetails = patientDetailsExtractor.extract(ehrPatientRecord)
        val tumorDetails = tumorDetailsExtractor.extract(ehrPatientRecord)
        val secondPrimaries = secondPrimaryExtractor.extract(ehrPatientRecord)
        val clinicalStatus = clinicalStatusExtractor.extract(ehrPatientRecord)
        val treatmentHistory = treatmentHistoryExtractor.extract(ehrPatientRecord)
        val comorbidities = comorbidityExtractor.extract(ehrPatientRecord)
        val medications = medicationExtractor.extract(ehrPatientRecord)
        val labValues = labValuesExtractor.extract(ehrPatientRecord)
        val bloodTransfusions = bloodTransfusionExtractor.extract(ehrPatientRecord)
        val vitalFunctions = vitalFunctionsExtractor.extract(ehrPatientRecord)
        val surgeries = surgeryExtractor.extract(ehrPatientRecord)
        val bodyWeights = bodyWeightExtractor.extract(ehrPatientRecord)
        val bodyHeights = bodyHeightExtractor.extract(ehrPatientRecord)
        val ihcTests = ihcTestExtractor.extract(ehrPatientRecord)
        val sequencingTests = sequencingTestExtractor.extract(ehrPatientRecord)
        val pathologyReports = pathologyReportsExtractor.extract(ehrPatientRecord)

        val patientEvaluation = listOf(
            patientDetails,
            tumorDetails,
            clinicalStatus,
            treatmentHistory,
            comorbidities,
            medications,
            labValues,
            bloodTransfusions,
            vitalFunctions,
            surgeries,
            bodyWeights,
            bodyHeights,
            secondPrimaries,
            ihcTests,
            sequencingTests,
            pathologyReports
        )
            .map { e -> e.evaluation }
            .fold(CurationExtractionEvaluation()) { acc, evaluation -> acc + evaluation }

        return ClinicalRecord(
            patientId = ehrPatientRecord.patientDetails.patientId,
            patient = patientDetails.extracted,
            tumor = tumorDetails.extracted,
            clinicalStatus = clinicalStatus.extracted,
            oncologicalHistory = treatmentHistory.extracted,
            comorbidities = comorbidities.extracted,
            medications = medications.extracted,
            labValues = labValues.extracted,
            bloodTransfusions = bloodTransfusions.extracted,
            vitalFunctions = vitalFunctions.extracted,
            surgeries = surgeries.extracted,
            bodyWeights = bodyWeights.extracted,
            bodyHeights = bodyHeights.extracted,
            priorPrimaries = secondPrimaries.extracted,
            ihcTests = ihcTests.extracted,
            sequencingTests = sequencingTests.extracted,
            pathologyReports = pathologyReports.extracted
        ) to patientEvaluation
    }

    companion object {
        fun create(
            directory: String,
            curationDatabaseContext: CurationDatabaseContext,
            atcModel: AtcModel,
            drugInteractionDatabase: DrugInteractionsDatabase,
            qtProlongatingDatabase: QtProlongatingDatabase,
            doidModel: DoidModel,
            treatmentDatabase: TreatmentDatabase
        ) = StandardDataIngestion(
            directory,
            StandardMedicationExtractor(atcModel, drugInteractionDatabase, qtProlongatingDatabase, treatmentDatabase),
            StandardSurgeryExtractor(curationDatabaseContext.surgeryNameCuration),
            StandardVitalFunctionsExtractor(),
            StandardBloodTransfusionExtractor(),
            StandardLabValuesExtractor(curationDatabaseContext.labMeasurementCuration),
            StandardComorbidityExtractor(curationDatabaseContext.comorbidityCuration),
            StandardOncologicalHistoryExtractor(curationDatabaseContext.treatmentHistoryEntryCuration),
            StandardClinicalStatusExtractor(),
            StandardTumorDetailsExtractor(curationDatabaseContext.primaryTumorCuration, TumorStageDeriver.create(doidModel)),
            StandardPriorPrimariesExtractor(curationDatabaseContext.priorPrimaryCuration),
            StandardPatientDetailsExtractor(),
            StandardBodyWeightExtractor(),
            StandardBodyHeightExtractor(),
            StandardIhcTestExtractor(curationDatabaseContext.molecularTestIhcCuration),
            StandardSequencingTestExtractor(
                curationDatabaseContext.sequencingTestCuration,
                curationDatabaseContext.sequencingTestResultCuration
            ),
            PathologyReportsExtractor()
        )
    }
}