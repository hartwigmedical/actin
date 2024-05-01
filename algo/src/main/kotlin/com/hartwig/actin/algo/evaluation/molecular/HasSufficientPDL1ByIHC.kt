package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue

class HasSufficientPDL1ByIHC internal constructor(private val measure: String?, private val minPDL1: Double) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorMolecularTests = record.molecularHistory.allIHCTests()
        val pdl1TestsWithRequestedMeasurement = if (measure != null) {
            PriorMolecularTestFunctions.allPDL1TestsWithSpecificMeasurement(priorMolecularTests, measure)
        } else PriorMolecularTestFunctions.allPDL1Tests(priorMolecularTests)

        for (ihcTest in pdl1TestsWithRequestedMeasurement) {
            val scoreValue = ihcTest.scoreValue
            if (scoreValue != null) {
                val evaluation = evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, minPDL1)
                if (evaluation == EvaluationResult.PASS) {
                    val measureMessage = if (measure != null) " measured by $measure" else ""
                    return EvaluationFactory.pass(
                        "PD-L1 expression$measureMessage meets at least desired level of $minPDL1",
                        "PD-L1 expression exceeds $minPDL1"
                    )
                }
            }
        }
        return if (pdl1TestsWithRequestedMeasurement.isNotEmpty()) {
            EvaluationFactory.fail(
                "No PD-L1 IHC test found where level exceeds desired level of $minPDL1", "PD-L1 expression below $minPDL1"
            )
        } else if (PriorMolecularTestFunctions.allPDL1Tests(priorMolecularTests).isNotEmpty()) {
            EvaluationFactory.recoverableFail(
                "No PD-L1 IHC test found with measurement type $measure", "PD-L1 tests not in correct unit ($measure)"
            )
        } else {
            EvaluationFactory.fail("PD-L1 expression not tested by IHC", "PD-L1 expression not tested by IHC")
        }
    }
}