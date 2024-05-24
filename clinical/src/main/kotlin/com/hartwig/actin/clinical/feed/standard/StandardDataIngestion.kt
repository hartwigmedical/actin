package com.hartwig.actin.clinical.feed.standard

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.PatientIngestionResult
import com.hartwig.actin.clinical.PatientIngestionStatus
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.feed.ClinicalFeedIngestion
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.doid.DoidModel
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.name


class StandardDataIngestion(
    private val directory: String,
    private val medicationExtractor: StandardMedicationExtractor,
    private val surgeryExtractor: StandardSurgeryExtractor,
    private val intolerancesExtractor: StandardIntolerancesExtractor,
    private val vitalFunctionsExtractor: StandardVitalFunctionsExtractor,
    private val bloodTransfusionExtractor: StandardBloodTransfusionExtractor,
    private val labValuesExtractor: StandardLabValuesExtractor,
    private val toxicityExtractor: StandardToxicityExtractor,
    private val complicationExtractor: StandardComplicationExtractor,
    private val priorOtherConditionsExtractor: StandardPriorOtherConditionsExtractor,
    private val treatmentHistoryExtractor: StandardTreatmentHistoryExtractor,
    private val clinicalStatusExtractor: StandardClinicalStatusExtractor,
    private val tumorDetailsExtractor: StandardTumorDetailsExtractor,
    private val secondPrimaryExtractor: StandardPriorPrimariesExtractor,
    private val patientDetailsExtractor: StandardPatientDetailsExtractor,
    private val bodyWeightExtractor: StandardBodyWeightExtractor,
    private val bodyHeightExtractor: StandardBodyHeightExtractor,
    private val molecularTestExtractor: StandardMolecularTestExtractor,
    private val dataQualityMask: DataQualityMask
) : ClinicalFeedIngestion {
    private val mapper = ObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        registerModule(JavaTimeModule())
        registerModule(KotlinModule.Builder().build())
    }

    override fun ingest(): List<Pair<PatientIngestionResult, CurationExtractionEvaluation>> {
        return Files.list(Paths.get(directory)).filter { it.name.endsWith("json") }.map {
            val ehrPatientRecord = dataQualityMask.apply(mapper.readValue(Files.readString(it), ProvidedPatientRecord::class.java))
            val patientDetails = patientDetailsExtractor.extract(ehrPatientRecord)
            val tumorDetails = tumorDetailsExtractor.extract(ehrPatientRecord)
            val secondPrimaries = secondPrimaryExtractor.extract(ehrPatientRecord)
            val clinicalStatus = clinicalStatusExtractor.extract(ehrPatientRecord)
            val treatmentHistory = treatmentHistoryExtractor.extract(ehrPatientRecord)
            val priorOtherCondition = priorOtherConditionsExtractor.extract(ehrPatientRecord)
            val complications = complicationExtractor.extract(ehrPatientRecord)
            val toxicities = toxicityExtractor.extract(ehrPatientRecord)
            val medications = medicationExtractor.extract(ehrPatientRecord)
            val labValues = labValuesExtractor.extract(ehrPatientRecord)
            val bloodTransfusions = bloodTransfusionExtractor.extract(ehrPatientRecord)
            val vitalFunctions = vitalFunctionsExtractor.extract(ehrPatientRecord)
            val intolerances = intolerancesExtractor.extract(ehrPatientRecord)
            val surgeries = surgeryExtractor.extract(ehrPatientRecord)
            val bodyWeights = bodyWeightExtractor.extract(ehrPatientRecord)
            val bodyHeights = bodyHeightExtractor.extract(ehrPatientRecord)
            val molecularTests = molecularTestExtractor.extract(ehrPatientRecord)

            val patientEvaluation = listOf(
                patientDetails,
                tumorDetails,
                clinicalStatus,
                treatmentHistory,
                priorOtherCondition,
                complications,
                toxicities,
                medications,
                labValues,
                bloodTransfusions,
                vitalFunctions,
                intolerances,
                surgeries,
                bodyWeights,
                bodyHeights,
                secondPrimaries,
                molecularTests
            )
                .map { e -> e.evaluation }
                .fold(CurationExtractionEvaluation()) { acc, evaluation -> acc + evaluation }

            Pair(
                patientEvaluation,
                ClinicalRecord(
                    patientId = ehrPatientRecord.patientDetails.hashedId,
                    patient = patientDetails.extracted,
                    tumor = tumorDetails.extracted,
                    clinicalStatus = clinicalStatus.extracted,
                    oncologicalHistory = treatmentHistory.extracted,
                    priorOtherConditions = priorOtherCondition.extracted,
                    complications = complications.extracted,
                    toxicities = toxicities.extracted,
                    medications = medications.extracted,
                    labValues = labValues.extracted,
                    bloodTransfusions = bloodTransfusions.extracted,
                    vitalFunctions = vitalFunctions.extracted,
                    intolerances = intolerances.extracted,
                    surgeries = surgeries.extracted,
                    bodyWeights = bodyWeights.extracted,
                    bodyHeights = bodyHeights.extracted,
                    priorSecondPrimaries = secondPrimaries.extracted,
                    priorMolecularTests = molecularTests.extracted
                )
            )

        }.map {
            Pair(
                PatientIngestionResult(
                    it.second.patientId,
                    if (it.first.warnings.isEmpty()) PatientIngestionStatus.PASS else PatientIngestionStatus.WARN_CURATION_REQUIRED,
                    it.second,
                    PatientIngestionResult.curationResults(it.first.warnings.toList()),
                    emptySet(),
                    emptySet()
                ), it.first
            )
        }.collect(Collectors.toList())
    }

    companion object {
        fun create(
            directory: String,
            curationDatabaseContext: CurationDatabaseContext,
            atcModel: AtcModel,
            doidModel: DoidModel
        ) = StandardDataIngestion(
            directory,
            StandardMedicationExtractor(
                atcModel,
                curationDatabaseContext.qtProlongingCuration,
                curationDatabaseContext.cypInteractionCuration
            ),
            StandardSurgeryExtractor(),
            StandardIntolerancesExtractor(
                atcModel,
                curationDatabaseContext.intoleranceCuration
            ),
            StandardVitalFunctionsExtractor(),
            StandardBloodTransfusionExtractor(),
            StandardLabValuesExtractor(curationDatabaseContext.laboratoryTranslation),
            StandardToxicityExtractor(curationDatabaseContext.toxicityCuration),
            StandardComplicationExtractor(curationDatabaseContext.complicationCuration),
            StandardPriorOtherConditionsExtractor(
                curationDatabaseContext.nonOncologicalHistoryCuration,
                curationDatabaseContext.treatmentHistoryEntryCuration
            ),
            StandardTreatmentHistoryExtractor(
                curationDatabaseContext.treatmentHistoryEntryCuration,
                curationDatabaseContext.nonOncologicalHistoryCuration
            ),
            StandardClinicalStatusExtractor(),
            StandardTumorDetailsExtractor(
                curationDatabaseContext.primaryTumorCuration, curationDatabaseContext.lesionLocationCuration,
                TumorStageDeriver.create(doidModel)
            ),
            StandardPriorPrimariesExtractor(curationDatabaseContext.secondPrimaryCuration),
            StandardPatientDetailsExtractor(),
            StandardBodyWeightExtractor(),
            StandardBodyHeightExtractor(),
            StandardMolecularTestExtractor(curationDatabaseContext.molecularTestIhcCuration),
            DataQualityMask()
        )
    }
}