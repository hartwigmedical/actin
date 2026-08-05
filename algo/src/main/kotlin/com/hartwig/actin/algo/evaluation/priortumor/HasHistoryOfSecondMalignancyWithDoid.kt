package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasHistoryOfSecondMalignancyWithDoid(
    private val doidModel: DoidModel,
    private val doidToMatch: String,
    private val labels: EvaluationLabels.PriorTumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm = doidModel.resolveTermForDoid(doidToMatch)
        return if (record.priorPrimaries.flatMap { it.doids }.flatMap { doidModel.doidWithParents(it) }.contains(doidToMatch)) {
            EvaluationFactory.pass(labels.hasHistoryOfSecondMalignancyWithDoidPass(doidTerm.toString()))
        } else {
            EvaluationFactory.fail(labels.hasHistoryOfSecondMalignancyWithDoidFail(doidTerm.toString()))
        }
    }
}