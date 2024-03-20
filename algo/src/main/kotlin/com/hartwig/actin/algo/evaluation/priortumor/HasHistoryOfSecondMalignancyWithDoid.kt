package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasHistoryOfSecondMalignancyWithDoid internal constructor(private val doidModel: DoidModel, private val doidToMatch: String) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm = doidModel.resolveTermForDoid(doidToMatch)
        return if (record.priorSecondPrimaries.flatMap { it.doids }.flatMap { doidModel.doidWithParents(it) }
                .contains(doidToMatch)) {
            EvaluationFactory.pass(
                "Patient has history of previous malignancy belonging to $doidTerm", "Present second primary history belonging to $doidTerm"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has no history of previous malignancy belonging to $doidTerm", "No specific history of malignancy"
            )
        }
    }
}