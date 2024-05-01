package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_NON_SMALL_CELL_CANCER_DOID_SET
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.PriorMolecularTestFunctions.allPDL1Tests
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue

class HasSufficientPDL1ByIHC internal constructor(private val measure: String, private val minPDL1: Double) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorMolecularTests = record.molecularHistory.allIHCTests()
        val pdl1TestsWithSpecificMeasurement = PriorMolecularTestFunctions.allPDL1TestsWithSpecificMeasurement(priorMolecularTests, measure)
        val pdl1TestsWithRequestedMeasurement =
            if (measure == "TPS" && record.tumor.doids?.any { it in LUNG_NON_SMALL_CELL_CANCER_DOID_SET } == true
                && allPDL1Tests(priorMolecularTests).all { it.measure == null }) {
                allPDL1Tests(priorMolecularTests)
            } else {
                pdl1TestsWithSpecificMeasurement
            }

        for (ihcTest in pdl1TestsWithRequestedMeasurement) {
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
        return if (pdl1TestsWithRequestedMeasurement.isNotEmpty()) {
            EvaluationFactory.fail(
                "No PD-L1 IHC test found where level exceeds desired level of $minPDL1", "PD-L1 expression below $minPDL1"
            )
        } else if (allPDL1Tests(priorMolecularTests).isNotEmpty()) {
            EvaluationFactory.recoverableFail(
                "No PD-L1 IHC test found with measurement type $measure", "PD-L1 tests not in correct unit ($measure)"
            )
        } else {
            EvaluationFactory.fail("PD-L1 expression not tested by IHC", "PD-L1 expression not tested by IHC")
        }
    }
}