package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.PriorMolecularTestFunctions.allPDL1Tests
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.doid.DoidModel

class HasSufficientPDL1ByIHC (
    private val measure: String?, private val minPDL1: Double, private val doidModel: DoidModel? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorMolecularTests = record.molecularHistory.allIHCTests()
        val isLungCancer = doidModel?.let { DoidEvaluationFunctions.isOfDoidType(it, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID) }
        val pdl1TestsWithRequestedMeasurement = allPDL1Tests(priorMolecularTests, measure, isLungCancer)

        for (ihcTest in pdl1TestsWithRequestedMeasurement) {
            val scoreValue = ihcTest.scoreValue
            if (scoreValue != null) {
                val evaluation = evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, minPDL1)
                val measureMessage = if (measure != null) " measured by $measure" else ""
                when (evaluation) {
                    EvaluationResult.PASS -> {
                        return EvaluationFactory.pass(
                            "PD-L1 expression$measureMessage meets at least desired level of $minPDL1",
                            "PD-L1 expression exceeds $minPDL1"
                        )
                    }
                    EvaluationResult.UNDETERMINED -> {
                        return EvaluationFactory.undetermined(
                            "Undetermined if PD-L1 expression (${ihcTest.let { "${it.scoreValuePrefix} " }}$scoreValue) " +
                                    "above minimum of $minPDL1"
                        )
                    }
                    else -> {
                        return EvaluationFactory.fail(
                            "PD-L1 expression$measureMessage below minimum of $minPDL1", "PD-L1 expression below $minPDL1"
                        )
                    }
                }
            }
        }

        return when {
            pdl1TestsWithRequestedMeasurement.isNotEmpty() && pdl1TestsWithRequestedMeasurement.any { test -> test.scoreValue == null } -> EvaluationFactory.fail(
                "No PD-L1 IHC test found with score value, only neg/pos status available",
                "No value score available for PD-L1 IHC test"
            )
            pdl1TestsWithRequestedMeasurement.isNotEmpty() -> EvaluationFactory.fail(
                "No PD-L1 IHC test found where level exceeds desired level of $minPDL1",
                "PD-L1 expression below $minPDL1"
            )
            allPDL1Tests(priorMolecularTests).isNotEmpty() -> EvaluationFactory.recoverableFail(
                "No PD-L1 IHC test found with measurement type $measure",
                "PD-L1 tests not in correct unit ($measure)"
            )
            else -> EvaluationFactory.fail("PD-L1 expression not tested by IHC",
                "PD-L1 expression not tested by IHC")
        }
    }
}