package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasHistoryOfSecondMalignancyWithDoid(private val doidModel: DoidModel, private val doidToMatch: String) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm = doidModel.resolveTermForDoid(doidToMatch)
        return if (record.priorPrimaries.flatMap { it.doids }.flatMap { doidModel.doidWithParents(it) }
                .contains(doidToMatch)) {
            EvaluationFactory.pass("Has history of previous malignancy belonging to $doidTerm")
        } else {
            EvaluationFactory.fail("No history of previous malignancy belonging to $doidTerm")
        }
    }
}