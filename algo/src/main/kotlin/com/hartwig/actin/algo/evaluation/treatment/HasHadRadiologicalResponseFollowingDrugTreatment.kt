package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse

class HasHadRadiologicalResponseFollowingDrugTreatment(
    private val drug: Drug,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

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
            matchingDrugTreatments.isEmpty() ->
                EvaluationFactory.fail(labels.hasHadRadiologicalResponseFollowingDrugTreatmentFailNoMatch(drug.display()))

            positiveResponses.isNotEmpty() -> {
                EvaluationFactory.pass(labels.hasHadRadiologicalResponseFollowingDrugTreatmentPass(drug.display()))
            }

            otherResponses.contains(TreatmentResponse.MIXED) -> {
                EvaluationFactory.undetermined(labels.hasHadRadiologicalResponseFollowingDrugTreatmentUndeterminedMixed(drug.display()))
            }

            otherResponses.isNotEmpty() -> {
                EvaluationFactory.fail(
                    labels.hasHadRadiologicalResponseFollowingDrugTreatmentFailOtherResponses(
                        otherResponses.joinToString(separator = " and a ") { it.display() },
                        drug.display()
                    )
                )
            }

            else -> {
                EvaluationFactory.undetermined(labels.hasHadRadiologicalResponseFollowingDrugTreatmentUndeterminedDefault(drug.display()))
            }
        }
    }
}
