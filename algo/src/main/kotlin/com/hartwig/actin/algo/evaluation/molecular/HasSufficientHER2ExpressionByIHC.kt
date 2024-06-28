package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.ReceptorType

class HasSufficientHER2ExpressionByIHC(private val minHER2: Double) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val receptorType = ReceptorType.HER2
        val targetPriorMolecularTests =
            record.molecularHistory.allIHCTests().filter { it.item == receptorType.display() }

        for (ihcTest in targetPriorMolecularTests) {
            val scoreValue = ihcTest.scoreValue
            if (scoreValue != null) {
                val evaluation = evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, minHER2)
                if (evaluation == EvaluationResult.PASS && !ihcTest.impliesPotentialIndeterminateStatus) {
                    return EvaluationFactory.pass(
                        "HER2 expression meets the desired level of $minHER2",
                        "HER2 expression exceeds $minHER2"
                    )
                }
            }
        }

        return when {
            targetPriorMolecularTests.isNotEmpty() && targetPriorMolecularTests.any { test -> test.impliesPotentialIndeterminateStatus } -> EvaluationFactory.undetermined(
                "HER2 IHC test found but result is potentially indeterminate",
                "HER2 IHC test inconclusive"
            )
            targetPriorMolecularTests.isNotEmpty() && targetPriorMolecularTests.any { test -> test.scoreValue == null} -> EvaluationFactory.fail(
                "Value of HER2 IHC test not available, only positive/negative state present",
                "No value score HER2 IHC test available"
            )
            else -> EvaluationFactory.fail(
                "HER2 expression not tested by IHC",
                "No HER2 IHC test available"
            )
        }
    }
}
