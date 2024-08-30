package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.DoidModel

object PDL1EvaluationFunctions {

    fun evaluatePDL1byIHC(
        record: PatientRecord, measure: String?, pdl1Reference: Double, doidModel: DoidModel?, evaluateMaxPDL1: Boolean
    ): Evaluation {
        val priorMolecularTests = record.priorIHCTests
        val isLungCancer = doidModel?.let { DoidEvaluationFunctions.isOfDoidType(it, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID) }
        val pdl1TestsWithRequestedMeasurement = PriorIHCTestFunctions.allPDL1Tests(priorMolecularTests, measure, isLungCancer)

        val testEvaluations = pdl1TestsWithRequestedMeasurement.mapNotNull { ihcTest ->
            ihcTest.scoreValue?.let { scoreValue ->
                val roundedScore = Math.round(scoreValue).toDouble()
                if (evaluateMaxPDL1) {
                    evaluateVersusMaxValue(roundedScore, ihcTest.scoreValuePrefix, pdl1Reference)
                }
                else {
                    evaluateVersusMinValue(roundedScore, ihcTest.scoreValuePrefix, pdl1Reference)
                }
            }
        }.toSet()

        val measureMessage = measure?.let { " measured by $it" } ?: ""
        val comparatorMessage = if (evaluateMaxPDL1) "below maximum of" else "above minimum of"

        return when {
            EvaluationResult.PASS in testEvaluations && EvaluationResult.FAIL in testEvaluations -> {
                EvaluationFactory.undetermined(
                    "Undetermined if PD-L1 expression $comparatorMessage $pdl1Reference - conflicting PD-L1 results"
                )
            }
            EvaluationResult.PASS in testEvaluations -> {
                EvaluationFactory.pass(
                    "PD-L1 expression$measureMessage $comparatorMessage $pdl1Reference",
                    "PD-L1 expression $comparatorMessage $pdl1Reference"
                )
            }
            EvaluationResult.FAIL in testEvaluations -> {
                val messageEnding = (if (evaluateMaxPDL1) "exceeds " else "below ") + pdl1Reference
                EvaluationFactory.fail(
                    "PD-L1 expression$measureMessage $messageEnding",
                    "PD-L1 expression $messageEnding"
                )
            }
            EvaluationResult.UNDETERMINED in testEvaluations -> {
                val testMessage = pdl1TestsWithRequestedMeasurement
                    .joinToString(", ") { "${it.scoreValuePrefix} ${it.scoreValue}" }
                EvaluationFactory.undetermined(
                    "Undetermined if PD-L1 expression ($testMessage) $comparatorMessage $pdl1Reference"
                )
            }
            pdl1TestsWithRequestedMeasurement.isNotEmpty() && pdl1TestsWithRequestedMeasurement.any { test -> test.scoreValue == null } -> {
                EvaluationFactory.recoverableFail(
                    "No PD-L1 IHC test found with score value - only neg/pos status available",
                    "No score value available for PD-L1 IHC test"
                )
            }
            PriorIHCTestFunctions.allPDL1Tests(priorMolecularTests).isNotEmpty() -> {
                EvaluationFactory.recoverableFail(
                    "No PD-L1 IHC test found with measurement type $measure", "PD-L1 tests not in correct unit ($measure)"
                )
            }
            else -> {
                EvaluationFactory.recoverableFail(
                    "PD-L1 expression (IHC) not tested", "PD-L1 expression (IHC) not tested"
                )
            }
        }
    }
}