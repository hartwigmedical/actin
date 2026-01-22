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
            matchingDrugTreatments.isEmpty() -> EvaluationFactory.fail("Patient did not have radiological response to ${drug.name} treatment")

            positiveResponses.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Patient had a response to treatment with ${drug.name} - " +
                            "it is assumed this response was radiological"
                )
            }

            otherResponses.contains(TreatmentResponse.MIXED) -> {
                EvaluationFactory.undetermined(
                    "Patient had a mixed response to treatment with ${drug.name} - " +
                            "it is undetermined if this response is considered a radiological response"
                )
            }

            otherResponses.isNotEmpty() -> {
                EvaluationFactory.fail(
                    "Patient had a ${otherResponses.joinToString(separator = " and a ") { it.display() }} response to ${drug.name} treatment - " +
                            "which is not considered a radiological response to ${drug.name}"
                )
            }

            else -> {
                EvaluationFactory.undetermined("Undetermined if patient had radiological response to ${drug.name} treatment")
            }
        }
    }
}