package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.ReceptorType

class HasPositiveHER2ExpressionByIHC(): EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val receptorType = ReceptorType.HER2
        val positiveValue = 3.0
        val indeterminateValue = 2.0
        val targetPriorMolecularTests =
            record.molecularHistory.allIHCTests().filter { it.item == receptorType.display() }

        for (ihcTest in targetPriorMolecularTests) {
            val scoreValue = ihcTest.scoreValue
            if (scoreValue != null) {
                val evaluation =
                    evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, positiveValue)
                if (evaluation == EvaluationResult.PASS && !ihcTest.impliesPotentialIndeterminateStatus) {
                    return EvaluationFactory.pass(
                        "IHC HER2 expression positive because it exceeds lower limit of $positiveValue",
                        "Positive HER2 expression determined by IHC"
                    )
                }
            }
        }


        return EvaluationFactory.fail(
            "HER2 expression not tested by IHC",
            "No HER2 IHC test available"
        )
    }
}