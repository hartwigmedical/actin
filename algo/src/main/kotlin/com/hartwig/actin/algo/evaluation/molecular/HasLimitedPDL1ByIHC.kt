package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.doid.DoidModel

class HasLimitedPDL1ByIHC(
    private val measure: String?, private val maxPDL1: Double, private val doidModel: DoidModel? = null
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorMolecularTests = record.molecularHistory.allIHCTests()
        val isLungCancer = doidModel?.let { DoidEvaluationFunctions.isOfDoidType(it, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID) }
        val pdl1TestsWithRequestedMeasurement = PriorMolecularTestFunctions.allPDL1Tests(priorMolecularTests, measure, isLungCancer)

        for (ihcTest in pdl1TestsWithRequestedMeasurement) {
            val scoreValue = ihcTest.scoreValue
            if (scoreValue != null) {
                val isBelowMaxValue = evaluateVersusMaxValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, maxPDL1)
                if (isBelowMaxValue == true) {
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