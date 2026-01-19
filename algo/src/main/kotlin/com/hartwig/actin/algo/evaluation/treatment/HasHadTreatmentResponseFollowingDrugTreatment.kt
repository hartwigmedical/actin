package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse

class HasHadTreatmentResponseFollowingDrugTreatment(private val drug: Drug) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val nameToMatch = drug.name.lowercase()

        val matchingDrugTreatments = record.oncologicalHistory
            .filter { entry ->
                entry.treatments.any { treatment ->
                    (treatment as? DrugTreatment)?.drugs?.any { it.name.lowercase() in nameToMatch } == true
                }
            }
        val matchingTreatmentsToResponseMap =
            matchingDrugTreatments.groupBy { it.treatmentHistoryDetails?.bestResponse }
        val (positiveResponses, otherResponses) = matchingTreatmentsToResponseMap.entries.partition {
            it.key in TreatmentResponse.BENEFIT_RESPONSES
        }
            .let { (withResponse, otherResponse) -> withResponse.flatMap { it.value } to otherResponse.mapNotNull { it.key } }

        return when {
            matchingDrugTreatments.isEmpty() ->
                EvaluationFactory.fail("Patient did not receive $drug during treatment")

            positiveResponses.isNotEmpty() && otherResponses.isNotEmpty() ->
                EvaluationFactory.undetermined("Patient had both a positive and a negative response to treatment with $drug")

            positiveResponses.isNotEmpty() ->
                EvaluationFactory.pass("Patient had both a positive response to treatment with $drug")

            otherResponses.contains(TreatmentResponse.MIXED) ->
                EvaluationFactory.undetermined("Patient had a mixed response to treatment with $drug")

            otherResponses.isNotEmpty() ->
                EvaluationFactory.fail("Patient had a negative response to treatment with $drug")

            else ->
                EvaluationFactory.undetermined("No response available for treatment with $drug")

        }
    }
}