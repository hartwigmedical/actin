package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue

class HasLimitedPDL1ByIHC(private val measure: String, private val maxPDL1: Double) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val pdl1Tests = PriorMolecularTestFunctions.allPDL1Tests(record.priorMolecularTests, measure)
        for (ihcTest in pdl1Tests) {
            val scoreValue = ihcTest.scoreValue
            if (scoreValue != null) {
                val evaluation = evaluateVersusMaxValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, maxPDL1)
                if (evaluation == EvaluationResult.PASS) {
                    return EvaluationFactory.pass(
                        "PD-L1 expression measured by $measure does not exceed maximum of $maxPDL1", "PD-L1 expression below $maxPDL1"
                    )
                }
            }
        }
        return if (pdl1Tests.isNotEmpty()) {
            EvaluationFactory.fail(
                "At least one PD-L1 IHC tests measured by $measure found where level exceeds maximum of $maxPDL1",
                "PD-L1 expression exceeds $maxPDL1"
            )
        } else {
            EvaluationFactory.fail(
                "No IHC test result found; PD-L1 has not been measured by $measure", "PD-L1 expression not tested by IHC"
            )
        }
    }
}