package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue

class HasSufficientPDL1ByIHC internal constructor(private val measure: String, private val minPDL1: Double) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val pdl1Tests = PriorMolecularTestFunctions.allPDL1Tests(record.clinical().priorMolecularTests(), measure)
        for (ihcTest in pdl1Tests) {
            val scoreValue = ihcTest.scoreValue()
            if (scoreValue != null) {
                val evaluation = evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix(), minPDL1)
                if (evaluation == EvaluationResult.PASS) {
                    return unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(
                            "PD-L1 expression measured by $measure meets at least desired level of $minPDL1"
                        )
                        .addPassGeneralMessages("PD-L1 expression exceeds $minPDL1")
                        .build()
                }
            }
        }
        val builder = unrecoverable().result(EvaluationResult.FAIL)
        if (pdl1Tests.isNotEmpty()) {
            builder.addFailSpecificMessages("No PD-L1 IHC test found where level exceeds desired level of $minPDL1")
            builder.addFailGeneralMessages("PD-L1 expression below $minPDL1")
        } else {
            builder.addFailSpecificMessages("No test result found; PD-L1 has not been tested by IHC")
            builder.addFailGeneralMessages("PD-L1 expression not tested by IHC")
        }
        return builder.build()
    }
}