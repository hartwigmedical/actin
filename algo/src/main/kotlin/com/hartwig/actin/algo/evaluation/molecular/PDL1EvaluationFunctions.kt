package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
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
                } else {
                    evaluateVersusMinValue(roundedScore, ihcTest.scoreValuePrefix, pdl1Reference)
                }
            } ?: evaluateNegativeOrPositiveTestScore(ihcTest, pdl1Reference, evaluateMaxPDL1)
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
                EvaluationFactory.recoverableUndetermined(
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
                EvaluationFactory.undetermined(
                    "PD-L1 expression (IHC) not tested", "PD-L1 expression (IHC) not tested", missingGenesForEvaluation = true
                )
            }
        }
    }

    private fun evaluateNegativeOrPositiveTestScore(
        ihcTest: PriorIHCTest,
        pdl1Reference: Double,
        evaluateMaxPDL1: Boolean,
    ): EvaluationResult? {
        val result = classifyIhcTest(ihcTest)
        return when {
            evaluateMaxPDL1 && result == IHCTestClassificationFunctions.TestResult.NEGATIVE &&
                    ((ihcTest.measure == "TPS" && pdl1Reference >= 1) || (ihcTest.measure == "CPS" && pdl1Reference >= 10)) -> EvaluationResult.PASS

            !evaluateMaxPDL1 && result == IHCTestClassificationFunctions.TestResult.POSITIVE && pdl1Reference == 1.0 &&
                    (ihcTest.measure == "TPS" || ihcTest.measure == "CPS") -> EvaluationResult.PASS

            !evaluateMaxPDL1 && result == IHCTestClassificationFunctions.TestResult.NEGATIVE &&
                    ((ihcTest.measure == "TPS" && pdl1Reference >= 1) || (ihcTest.measure == "CPS" && pdl1Reference >= 10)) -> EvaluationResult.FAIL

            else -> null
        }
    }

    private fun classifyIhcTest(test: PriorIHCTest): IHCTestClassificationFunctions.TestResult {
        return when {
            test.scoreText?.lowercase()?.contains("negative") == true -> {
                IHCTestClassificationFunctions.TestResult.NEGATIVE
            }

            test.scoreText?.lowercase()?.contains("positive") == true -> {
                IHCTestClassificationFunctions.TestResult.POSITIVE
            }

            else -> IHCTestClassificationFunctions.TestResult.UNKNOWN
        }
    }
}