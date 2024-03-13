package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue

class HasSufficientPDL1ByIHC internal constructor(private val measure: String, private val minPDL1: Double) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val pdl1Tests = PriorMolecularTestFunctions.allPDL1Tests(record.molecularHistory.allPriorMolecularTests(), measure)
        for (ihcTest in pdl1Tests) {
            val scoreValue = ihcTest.scoreValue
            if (scoreValue != null) {
                val evaluation = evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, minPDL1)
                if (evaluation == EvaluationResult.PASS) {
                    return EvaluationFactory.pass(
                        "PD-L1 expression measured by $measure meets at least desired level of $minPDL1",
                        "PD-L1 expression exceeds $minPDL1"
                    )
                }
            }
        }
        return if (pdl1Tests.isNotEmpty()) {
            EvaluationFactory.fail(
                "No PD-L1 IHC test found where level exceeds desired level of $minPDL1", "PD-L1 expression below $minPDL1"
            )
        } else {
            EvaluationFactory.fail("No test result found; PD-L1 has not been tested by IHC", "PD-L1 expression not tested by IHC")
        }
    }
}