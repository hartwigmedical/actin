package com.hartwig.actin.clinical.ehr

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.hartwig.actin.clinical.PatientIngestionResult
import com.hartwig.actin.clinical.PatientIngestionStatus
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors


class EhrDataFeed(
    private val directory: String,
    private val medicationExtractor: EhrMedicationExtractor,
    private val surgeryExtractor: EhrSurgeryExtractor,
    private val intolerancesExtractor: EhrIntolerancesExtractor,
    private val vitalFunctionsExtractor: EhrVitalFunctionsExtractor,
    private val bloodTransfusionExtractor: EhrBloodTransfusionExtractor,
    private val labValuesExtractor: EhrLabValuesExtractor,
    private val toxicityExtractor: EhrToxicityExtractor,
    private val complicationExtractor: EhrComplicationExtractor,
    private val priorOtherConditionsExtractor: EhrPriorOtherConditionsExtractor,
    private val treatmentHistoryExtractor: EhrTreatmentHistoryExtractor,
    private val clinicalStatusExtractor: EhrClinicalStatusExtractor,
    private val tumorDetailsExtractor: EhrTumorDetailsExtractor,
    private val secondPrimaryExtractor: EhrSecondPrimariesExtractor,
    private val patientDetailsExtractor: EhrPatientDetailsExtractor,
    private val bodyWeightExtractor: EhrBodyWeightExtractor
) {
    private val mapper = ObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        registerModule(JavaTimeModule())
    }

    fun ingest(): List<PatientIngestionResult> {
        return Files.list(Paths.get(directory)).map {
            val ehrPatientRecord = mapper.readValue(Files.readString(it), EhrPatientRecord::class.java)
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
                secondPrimaries
            )
                .map { e -> e.evaluation }
                .fold(ExtractionEvaluation()) { acc, evaluation -> acc + evaluation }

            Pair(
                patientEvaluation,
                ClinicalRecord(
                    patientId = ehrPatientRecord.patientDetails.patientId,
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
                    priorSecondPrimaries = secondPrimaries.extracted,
                    priorMolecularTests = emptyList()
                )
            )

        }.map {
            PatientIngestionResult(
                it.second.patientId,
                PatientIngestionStatus.PASS,
                it.second,
                PatientIngestionResult.curationResults(it.first.warnings.toList()),
                emptySet(),
                emptySet()
            )
        }.collect(Collectors.toList())
    }
}