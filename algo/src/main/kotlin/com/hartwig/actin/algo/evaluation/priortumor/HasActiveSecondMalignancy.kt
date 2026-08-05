package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStatus

class HasActiveSecondMalignancy(private val labels: EvaluationLabels.PriorTumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val statuses = record.priorPrimaries.map { it.status }.toSet()
        return when {
            TumorStatus.ACTIVE in statuses -> EvaluationFactory.pass(labels.hasActiveSecondMalignancyPass())
            TumorStatus.EXPECTATIVE in statuses -> EvaluationFactory.warn(labels.hasActiveSecondMalignancyWarn())
            else -> EvaluationFactory.fail(labels.hasActiveSecondMalignancyFail())
        }
    }
}