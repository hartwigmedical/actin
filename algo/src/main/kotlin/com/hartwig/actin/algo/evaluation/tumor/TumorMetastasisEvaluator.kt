package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation

object TumorMetastasisEvaluator {
    fun evaluate(hasMetastases: Boolean?, hasSuspectedMetastases: Boolean?, metastasisType: String): Evaluation {
        val capitalizedType = metastasisType.replaceFirstChar { it.uppercase() }
        return when {
            hasMetastases == true -> {
                EvaluationFactory.pass("$capitalizedType metastases are present", "$capitalizedType metastases")
            }

            hasSuspectedMetastases == true -> {
                val message = "$capitalizedType metastases present but only suspected lesions"
                EvaluationFactory.warn(message, message)
            }

            hasMetastases == null -> EvaluationFactory.undetermined(
                "Data regarding presence of $metastasisType metastases is missing",
                "Missing $metastasisType metastasis data"
            )

            else -> {
                EvaluationFactory.fail("No $metastasisType metastases present", "No $metastasisType metastases")
            }
        }
    }
}