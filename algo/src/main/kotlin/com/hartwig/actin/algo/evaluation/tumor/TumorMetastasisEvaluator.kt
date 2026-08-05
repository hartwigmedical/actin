package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.algo.Evaluation

internal object TumorMetastasisEvaluator {

    fun evaluate(hasLesions: Boolean?, hasSuspectedLesions: Boolean?, metastasisType: String, labels: EvaluationLabels.Tumor): Evaluation {
        return when {
            hasLesions == true -> {
                EvaluationFactory.pass(labels.tumorMetastasisEvaluatorPass(metastasisType))
            }

            hasSuspectedLesions == true -> {
                EvaluationFactory.warn(labels.tumorMetastasisEvaluatorWarn(metastasisType))
            }

            hasLesions == null -> EvaluationFactory.undetermined(labels.tumorMetastasisEvaluatorUndetermined(metastasisType))

            else -> {
                EvaluationFactory.fail(labels.tumorMetastasisEvaluatorFail(metastasisType))
            }
        }
    }
}