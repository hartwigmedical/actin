package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation

object TumorMetastasisEvaluator {

    fun evaluate(hasLesions: Boolean?, hasSuspectedLesions: Boolean?, metastasisType: String): Evaluation {
        return when {
            hasLesions == true -> {
                EvaluationFactory.pass("Has $metastasisType metastases")
            }

            hasSuspectedLesions == true -> {
                EvaluationFactory.warn("Has suspected $metastasisType metastases hence uncertain if actually has $metastasisType metastases")
            }

            hasLesions == null -> EvaluationFactory.undetermined("Undetermined if patient has $metastasisType metastases (missing lesion data)")

            else -> {
                EvaluationFactory.fail("No $metastasisType metastases")
            }
        }
    }
}