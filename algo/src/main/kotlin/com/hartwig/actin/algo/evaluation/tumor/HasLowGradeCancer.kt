package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasLowGradeCancer(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLowGrade = TumorTermConstants.LOW_GRADE_TERMS.any { record.tumor.name.lowercase().contains(it) }
        val hasHighGrade = TumorTermConstants.HIGH_GRADE_TERMS.any { record.tumor.name.lowercase().contains(it) }

        return when {
            hasLowGrade -> EvaluationFactory.pass(labels.hasLowGradeCancerPass())
            hasHighGrade -> EvaluationFactory.fail(labels.hasLowGradeCancerFail())
            else -> EvaluationFactory.undetermined(labels.hasLowGradeCancerUndetermined())
        }
    }
}