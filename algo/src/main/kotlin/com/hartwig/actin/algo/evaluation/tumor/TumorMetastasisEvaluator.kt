package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation

internal object TumorMetastasisEvaluator {

    fun evaluate(hasLesions: Boolean?, hasSuspectedLesions: Boolean?, metastasisType: String): Evaluation {
        return when {
            hasLesions == true -> {
                EvaluationFactory.pass("$metastasisType metastases in provided lesions")
            }

            hasSuspectedLesions == true -> {
                EvaluationFactory.warn("Suspected $metastasisType metastases in provided lesions and not yet confirmed")
            }

            hasLesions == null -> EvaluationFactory.undetermined(
                "Undetermined if $metastasisType metastases based on provided lesions"
            )

            else -> {
                EvaluationFactory.fail("No $metastasisType metastases")
            }
        }
    }
}