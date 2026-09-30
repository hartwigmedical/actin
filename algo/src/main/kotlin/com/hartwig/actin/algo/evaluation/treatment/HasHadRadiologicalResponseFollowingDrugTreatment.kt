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

        val matchingDrugTreatments = record.oncologicalHistory.filter { entry ->
            entry.treatments.any { treatment ->
                (treatment as? DrugTreatment)?.drugs?.any { it.name.equals(drug.name, ignoreCase = true) } == true
            }
        }
        val (positiveResponses, otherResponses) = matchingDrugTreatments
            .partition { it.treatmentHistoryDetails?.bestResponse in TreatmentResponse.BENEFIT_RESPONSES }
            .let { (positiveResponses, otherResponses) -> positiveResponses.mapNotNull { it.treatmentHistoryDetails?.bestResponse } to
                    otherResponses.mapNotNull { it.treatmentHistoryDetails?.bestResponse } }

        return when {
            matchingDrugTreatments.isEmpty() -> EvaluationFactory.fail("No radiological response to ${drug.display()} treatment")

            positiveResponses.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Response to treatment with ${drug.display()} is assumed to be radiological"
                )
            }

            otherResponses.contains(TreatmentResponse.MIXED) -> {
                EvaluationFactory.undetermined(
                    "Mixed response to treatment with ${drug.display()} - " +
                            "undetermined if this response is considered a radiological response"
                )
            }

            otherResponses.isNotEmpty() -> {
                val messageStart = otherResponses.joinToString(separator = " and a ") { it.display() }
                    .replaceFirstChar { it.uppercase() }
                EvaluationFactory.fail(
                    "$messageStart response to ${drug.display()} treatment - " +
                            "not considered a radiological response to ${drug.display()}"
                )
            }

            else -> {
                EvaluationFactory.undetermined("Undetermined if there was radiological response to ${drug.display()} treatment")
            }
        }
    }
}