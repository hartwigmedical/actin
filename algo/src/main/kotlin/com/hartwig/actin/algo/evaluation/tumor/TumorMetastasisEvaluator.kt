package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.util.ApplicationConfig

object TumorMetastasisEvaluator {
    fun evaluate(hasMetastases: Boolean?, metastasisType: String): Evaluation {
        return if (hasMetastases == null) {
            EvaluationFactory.undetermined(
                "Data regarding presence of $metastasisType metastases is missing",
                "Missing $metastasisType metastasis data"
            )
        } else if (hasMetastases) {
            val capitalizedType = metastasisType.substring(0, 1).uppercase(ApplicationConfig.LOCALE) + metastasisType.substring(1)
            EvaluationFactory.pass("$capitalizedType metastases are present", "$capitalizedType metastases")
        } else {
            EvaluationFactory.fail("No $metastasisType metastases present", "No $metastasisType metastases")
        }
    }
}