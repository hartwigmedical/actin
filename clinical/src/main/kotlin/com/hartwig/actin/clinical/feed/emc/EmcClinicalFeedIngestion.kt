package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ClinicalIngestionFeedAdapter
import com.hartwig.actin.clinical.DrugInteractionsDatabase
import com.hartwig.actin.clinical.QtProlongatingDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.ClinicalFeedIngestion
import com.hartwig.actin.clinical.feed.FeedModelJsonUtil.feedModelMapper
import com.hartwig.actin.clinical.feed.curationResultsFromWarnings
import com.hartwig.actin.clinical.feed.emc.extraction.BloodTransfusionsExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.ComorbidityExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.IhcTestsExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.LabValueExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.MedicationExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.OncologicalHistoryExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.PriorPrimaryExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.SequencingTestExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.SurgeryExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.TumorDetailsExtractor
import com.hartwig.actin.clinical.feed.emc.extraction.VitalFunctionsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.PathologyReportsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBodyWeightExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardPatientDetailsExtractor
import com.hartwig.actin.clinical.feed.treatment.TreatmentHistoryEntryFunctions
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.ingestion.FeedValidationWarning
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionResult
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionStatus
import com.hartwig.actin.datamodel.clinical.ingestion.QuestionnaireCurationError
import com.hartwig.actin.doid.DoidModel
import com.hartwig.feed.datamodel.FeedPatientRecord
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class EmcClinicalFeedIngestion(
    private val feedDirectory: String,
    private val tumorDetailsExtractor: TumorDetailsExtractor,
    private val comorbidityExtractor: ComorbidityExtractor,
    private val oncologicalHistoryExtractor: OncologicalHistoryExtractor,
    private val priorPrimaryExtractor: PriorPrimaryExtractor,
    private val ihcTestsExtractor: IhcTestsExtractor,
    private val sequencingTestExtractor: SequencingTestExtractor,
    private val labValueExtractor: LabValueExtractor,
    private val medicationExtractor: MedicationExtractor,
    private val bloodTransfusionsExtractor: BloodTransfusionsExtractor,
    private val surgeryExtractor: SurgeryExtractor,
    private val bodyWeightExtractor: StandardBodyWeightExtractor,
    private val vitalFunctionsExtractor: VitalFunctionsExtractor,
    private val patientDetailsExtractor: StandardPatientDetailsExtractor,
    private val pathologyReportsExtractor: PathologyReportsExtractor,

    ) : ClinicalFeedIngestion {

    override fun ingest(): List<Triple<ClinicalRecord, PatientIngestionResult, CurationExtractionEvaluation>> {
        LOGGER.info("Creating clinical model")
        return Paths.get(feedDirectory).listDirectoryEntries()
            .filter { it.name.endsWith("json") }
            .map(::recordAndEvaluationFromPath)
    }

    private fun recordAndEvaluationFromPath(file: Path): Triple<ClinicalRecord, PatientIngestionResult, CurationExtractionEvaluation> {
        val feedRecord = feedModelMapper.readValue(Files.readString(file), FeedPatientRecord::class.java)
        val patientId = feedRecord.patientDetails.patientId
        LOGGER.info(" Extracting and curating data for patient {}", patientId)

        val vitalFunctionWarnings = feedRecord.measurements
            .filterNot { it.category == "BODY_WEIGHT" }
            .flatMap { VitalFunctionValidator().validate(patientId, it).warnings }
            .toSet()

        val questionnaireCurationErrors = TumorStageValidator.validate(patientId, feedRecord.tumorDetails.stage).errors

        val tumorExtraction = tumorDetailsExtractor.extract(patientId, feedRecord.tumorDetails)
        val comorbidityExtraction = comorbidityExtractor.extract(feedRecord)
        val (comorbidities, clinicalStatus) = comorbidityExtraction.extracted
        val oncologicalHistoryExtraction = oncologicalHistoryExtractor.extract(patientId, feedRecord.treatmentHistory)
        val priorPrimaryExtraction = priorPrimaryExtractor.extract(patientId, feedRecord)
        val ihcTestsExtraction = ihcTestsExtractor.extract(patientId, feedRecord.ihcTests)
        val sequencingTestExtraction = sequencingTestExtractor.extract(patientId, feedRecord.ihcTests)
        val labValuesExtraction = labValueExtractor.extract(patientId, feedRecord.labValues)
        val bloodTransfusionsExtraction = bloodTransfusionsExtractor.extract(patientId, feedRecord.bloodTransfusions)
        val medicationExtraction = medicationExtractor.extract(patientId, feedRecord.medications)
        val surgeryExtraction = surgeryExtractor.extract(patientId, feedRecord.surgeries)
        val bodyWeightExtraction = bodyWeightExtractor.extract(feedRecord)
        val vitalFunctionsExtraction = vitalFunctionsExtractor.extract(feedRecord)
        val patientDetailsExtraction = patientDetailsExtractor.extract(feedRecord)
        val pathologyReportsExtraction = pathologyReportsExtractor.extract(feedRecord)

        val record = ClinicalRecord(
            patientId = patientId,
            patient = patientDetailsExtraction.extracted,
            tumor = tumorExtraction.extracted,
            comorbidities = comorbidities,
            clinicalStatus = clinicalStatus,
            oncologicalHistory = TreatmentHistoryEntryFunctions.setMaxStopDate(
                oncologicalHistoryExtractor.extract(
                    patientId,
                    feedRecord.treatmentHistory
                ).extracted,
                patientDetailsExtraction.extracted.questionnaireDate,
                patientDetailsExtraction.extracted.registrationDate
            ),
            priorPrimaries = priorPrimaryExtraction.extracted,
            ihcTests = ihcTestsExtraction.extracted,
            sequencingTests = sequencingTestExtraction.extracted,
            labValues = labValuesExtraction.extracted,
            surgeries = surgeryExtraction.extracted,
            bodyWeights = bodyWeightExtraction.extracted,
            bodyHeights = emptyList(),
            vitalFunctions = vitalFunctionsExtraction.extracted,
            bloodTransfusions = bloodTransfusionsExtraction.extracted,
            medications = medicationExtraction.extracted,
            pathologyReports = pathologyReportsExtraction.extracted
        )

        val patientEvaluation = listOf(
            tumorExtraction,
            comorbidityExtraction,
            oncologicalHistoryExtraction,
            priorPrimaryExtraction,
            ihcTestsExtraction,
            sequencingTestExtraction,
            labValuesExtraction,
            bloodTransfusionsExtraction,
            medicationExtraction,
            surgeryExtraction,
            bodyWeightExtraction,
            vitalFunctionsExtraction,
            patientDetailsExtraction,
            pathologyReportsExtraction
        ).fold(CurationExtractionEvaluation()) { acc, current -> acc + current.evaluation }


        return Triple(
            record,
            ingestionResult(
                record.patientId,
                record.patient.registrationDate,
                record.patient.questionnaireDate,
                patientEvaluation,
                questionnaireCurationErrors,
                vitalFunctionWarnings,
            ),
            patientEvaluation
        )
    }

    private fun ingestionResult(
        patientId: String,
        registrationDate: LocalDate,
        questionnaireDate: LocalDate?,
        patientEvaluation: CurationExtractionEvaluation,
        questionnaireCurationErrors: List<QuestionnaireCurationError>,
        validationWarnings: Set<FeedValidationWarning>
    ): PatientIngestionResult {
        val curationResults = curationResultsFromWarnings(patientEvaluation.warnings)

        val ingestionStatus =
            if (questionnaireDate == null || curationResults.isNotEmpty()) PatientIngestionStatus.WARN else PatientIngestionStatus.PASS

        return PatientIngestionResult(
            patientId,
            registrationDate,
            ingestionStatus,
            curationResults,
            questionnaireCurationErrors.toSet(),
            validationWarnings
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ClinicalIngestionFeedAdapter::class.java)

        fun create(
            feedDirectory: String,
            curationDatabaseContext: CurationDatabaseContext,
            atcModel: AtcModel,
            drugInteractionsDatabase: DrugInteractionsDatabase,
            qtProlongatingDatabase: QtProlongatingDatabase,
            doidModel: DoidModel,
            treatmentDatabase: TreatmentDatabase
        ): EmcClinicalFeedIngestion {
            return EmcClinicalFeedIngestion(
                feedDirectory = feedDirectory,
                tumorDetailsExtractor = TumorDetailsExtractor.create(curationDatabaseContext, TumorStageDeriver.create(doidModel)),
                comorbidityExtractor = ComorbidityExtractor.create(curationDatabaseContext),
                oncologicalHistoryExtractor = OncologicalHistoryExtractor.create(curationDatabaseContext),
                priorPrimaryExtractor = PriorPrimaryExtractor.create(curationDatabaseContext),
                ihcTestsExtractor = IhcTestsExtractor.create(curationDatabaseContext),
                sequencingTestExtractor = SequencingTestExtractor.create(curationDatabaseContext),
                labValueExtractor = LabValueExtractor.create(curationDatabaseContext),
                medicationExtractor = MedicationExtractor.create(
                    curationDatabaseContext,
                    atcModel,
                    drugInteractionsDatabase,
                    qtProlongatingDatabase,
                    treatmentDatabase
                ),
                bloodTransfusionsExtractor = BloodTransfusionsExtractor.create(curationDatabaseContext),
                surgeryExtractor = SurgeryExtractor.create(curationDatabaseContext),
                bodyWeightExtractor = StandardBodyWeightExtractor(),
                vitalFunctionsExtractor = VitalFunctionsExtractor(),
                patientDetailsExtractor = StandardPatientDetailsExtractor(),
                pathologyReportsExtractor = PathologyReportsExtractor(),
            )
        }
    }
}