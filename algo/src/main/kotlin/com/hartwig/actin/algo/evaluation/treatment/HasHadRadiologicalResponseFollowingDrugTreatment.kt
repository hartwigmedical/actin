package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse

class HasHadRadiologicalResponseFollowingDrugTreatment(private val drug: Drug) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val nameToMatch = drug.name.lowercase()

        val matchingDrugTreatments = record.oncologicalHistory.filter { entry ->
                entry.treatments.any { treatment ->
                    (treatment as? DrugTreatment)?.drugs?.any { it.name.lowercase() == nameToMatch } == true
                }
            }
        val matchingTreatmentsToResponseMap = matchingDrugTreatments.groupBy { it.treatmentHistoryDetails?.bestResponse }
        val (positiveResponses, otherResponses) = matchingTreatmentsToResponseMap.entries.partition {
            it.key in TreatmentResponse.BENEFIT_RESPONSES
        }.let { (withResponse, otherResponse) -> withResponse.flatMap { it.value } to otherResponse.mapNotNull { it.key } }

        return when {
            matchingDrugTreatments.isEmpty() -> EvaluationFactory.fail("Patient did not receive $drug during treatment")

            positiveResponses.isNotEmpty() -> {
                EvaluationFactory.pass("Patient had a positive radiological response to treatment with $drug - " +
                        "it assumed this response was radiological")
            }

            otherResponses.contains(TreatmentResponse.MIXED) -> {
                EvaluationFactory.undetermined("Patient had a mixed radiological response to treatment with $drug - " +
                        "it assumed this response was radiological")
            }

            otherResponses.isNotEmpty() -> {
                EvaluationFactory.fail("Patient had a negative radiological response to treatment with $drug - " +
                        "it assumed this response was radiological")
            }

            else -> {
                EvaluationFactory.undetermined("No radiological response available for treatment with $drug - " +
                        "it assumed this response was radiological")
            }
        }
    }
}