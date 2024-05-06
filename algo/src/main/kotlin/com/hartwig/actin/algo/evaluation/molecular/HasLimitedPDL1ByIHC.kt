package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue

class HasLimitedPDL1ByIHC(private val measure: String?, private val maxPDL1: Double) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorMolecularTests = record.molecularHistory.allIHCTests()
        val pdl1TestsWithRequestedMeasurement = PriorMolecularTestFunctions.allPDL1Tests(priorMolecularTests, measure, record.tumor.doids)

        for (ihcTest in pdl1TestsWithRequestedMeasurement) {
            val scoreValue = ihcTest.scoreValue
            if (scoreValue != null) {
                val evaluation = evaluateVersusMaxValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, maxPDL1)
                if (evaluation == EvaluationResult.PASS) {
                    val measureMessage = if (measure != null) " measured by $measure" else ""
                    return EvaluationFactory.pass(
                        "PD-L1 expression$measureMessage does not exceed maximum of $maxPDL1", "PD-L1 expression below $maxPDL1"
                    )
                }
            }
        }
        return if (pdl1TestsWithRequestedMeasurement.isNotEmpty()) {
            EvaluationFactory.fail(
                "At least one PD-L1 IHC tests measured by $measure found where level exceeds maximum of $maxPDL1",
                "PD-L1 expression exceeds $maxPDL1"
            )
        } else if (PriorMolecularTestFunctions.allPDL1Tests(priorMolecularTests).isNotEmpty()) {
            EvaluationFactory.recoverableFail(
                "No PD-L1 IHC test found with measurement type $measure", "PD-L1 tests not in correct unit ($measure)"
            )
        } else {
            EvaluationFactory.fail(
                "PD-L1 expression not tested by IHC", "PD-L1 expression not tested by IHC"
            )
        }
    }
}