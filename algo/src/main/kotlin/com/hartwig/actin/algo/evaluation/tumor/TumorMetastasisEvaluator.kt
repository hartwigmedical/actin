package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation

object TumorMetastasisEvaluator {
    fun evaluate(hasMetastases: Boolean?, hasSuspectedMetastases: Boolean?, metastasisType: String): Evaluation {
        return when {
            hasMetastases == true -> {
                val capitalizedType = metastasisType.substring(0, 1).uppercase() + metastasisType.substring(1)
                EvaluationFactory.pass("$capitalizedType metastases are present", "$capitalizedType metastases")
            }

            hasSuspectedMetastases == true -> {
                val capitalizedType = metastasisType.substring(0, 1).uppercase() + metastasisType.substring(1)
                EvaluationFactory.undetermined(
                    "Undetermined if $capitalizedType metastases present (only suspected lesions)",
                    "Undetermined $capitalizedType metastases (suspected lesions only)"
                )
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