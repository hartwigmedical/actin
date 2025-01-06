package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation

object TumorMetastasisEvaluator {

    fun evaluate(hasMetastases: Boolean?, hasSuspectedMetastases: Boolean?, metastasisType: String): Evaluation {
        val capitalizedType = metastasisType.replaceFirstChar { it.uppercase() }
        return when {
            hasMetastases == true -> {
                EvaluationFactory.pass("Has ${metastasisType.lowercase()} metastases")
            }

            hasSuspectedMetastases == true -> {
                val message = "$capitalizedType metastases present but only suspected lesions"
                EvaluationFactory.warn(message)
            }

            hasMetastases == null -> EvaluationFactory.undetermined("Missing $metastasisType metastasis data")

            else -> {
                EvaluationFactory.fail("No $metastasisType metastases")
            }
        }
    }
}