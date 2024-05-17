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


class StandardProvidedDataIngestion(
    private val directory: String,
    private val medicationExtractor: ProvidedMedicationExtractor,
    private val surgeryExtractor: ProvidedSurgeryExtractor,
    private val intolerancesExtractor: ProvidedIntolerancesExtractor,
    private val vitalFunctionsExtractor: ProvidedVitalFunctionsExtractor,
    private val bloodTransfusionExtractor: ProvidedBloodTransfusionExtractor,
    private val labValuesExtractor: ProvidedLabValuesExtractor,
    private val toxicityExtractor: ProvidedToxicityExtractor,
    private val complicationExtractor: ProvidedComplicationExtractor,
    private val priorOtherConditionsExtractor: ProvidedPriorOtherConditionsExtractor,
    private val treatmentHistoryExtractor: ProvidedTreatmentHistoryExtractor,
    private val clinicalStatusExtractor: ProvidedClinicalStatusExtractor,
    private val tumorDetailsExtractor: HospitalProvidedTumorDetailsExtractor,
    private val secondPrimaryExtractor: ProvidedPriorPrimariesExtractor,
    private val patientDetailsExtractor: ProvidedPatientDetailsExtractor,
    private val bodyWeightExtractor: ProvidedBodyWeightExtractor,
    private val bodyHeightExtractor: ProvidedBodyHeightExtractor,
    private val molecularTestExtractor: ProvidedMolecularTestExtractor,
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
        ) = StandardProvidedDataIngestion(
            directory,
            ProvidedMedicationExtractor(
                atcModel,
                curationDatabaseContext.qtProlongingCuration,
                curationDatabaseContext.cypInteractionCuration
            ),
            ProvidedSurgeryExtractor(),
            ProvidedIntolerancesExtractor(
                atcModel,
                curationDatabaseContext.intoleranceCuration
            ),
            ProvidedVitalFunctionsExtractor(),
            ProvidedBloodTransfusionExtractor(),
            ProvidedLabValuesExtractor(curationDatabaseContext.laboratoryTranslation),
            ProvidedToxicityExtractor(curationDatabaseContext.toxicityCuration),
            ProvidedComplicationExtractor(curationDatabaseContext.complicationCuration),
            ProvidedPriorOtherConditionsExtractor(
                curationDatabaseContext.nonOncologicalHistoryCuration,
                curationDatabaseContext.treatmentHistoryEntryCuration
            ),
            ProvidedTreatmentHistoryExtractor(
                curationDatabaseContext.treatmentHistoryEntryCuration,
                curationDatabaseContext.nonOncologicalHistoryCuration
            ),
            ProvidedClinicalStatusExtractor(),
            HospitalProvidedTumorDetailsExtractor(
                curationDatabaseContext.primaryTumorCuration, curationDatabaseContext.lesionLocationCuration,
                TumorStageDeriver.create(doidModel)
            ),
            ProvidedPriorPrimariesExtractor(curationDatabaseContext.secondPrimaryCuration),
            ProvidedPatientDetailsExtractor(),
            ProvidedBodyWeightExtractor(),
            ProvidedBodyHeightExtractor(),
            ProvidedMolecularTestExtractor(curationDatabaseContext.molecularTestIhcCuration),
            DataQualityMask()
        )
    }
}