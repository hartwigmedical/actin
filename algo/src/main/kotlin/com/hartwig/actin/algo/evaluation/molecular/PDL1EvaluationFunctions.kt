package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.doid.DoidModel

private enum class TestResult {
    POSITIVE,
    NEGATIVE,
    UNKNOWN
}

object PDL1EvaluationFunctions {

    fun evaluatePDL1byIhc(
        record: PatientRecord, measure: String?, pdl1Reference: Double, doidModel: DoidModel?, evaluateMaxPDL1: Boolean
    ): Evaluation {
        val ihcTests = record.ihcTests
        val isLungCancer = doidModel?.let { DoidEvaluationFunctions.isOfDoidType(it, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID) }
        val pdl1TestsWithRequestedMeasurement = IhcTestFilter.allPDL1Tests(ihcTests, measure, isLungCancer)

        val testEvaluations = pdl1TestsWithRequestedMeasurement.mapNotNull { ihcTest ->
            ihcTest.scoreValue?.let { scoreValue ->
                val roundedScore = Math.round(scoreValue).toDouble()
                if (evaluateMaxPDL1) {
                    evaluateVersusMaxValue(roundedScore, ihcTest.scoreValuePrefix, pdl1Reference)
                } else {
                    evaluateVersusMinValue(roundedScore, ihcTest.scoreValuePrefix, pdl1Reference)
                }
            } ?: evaluateNegativeOrPositiveTestScore(ihcTest, pdl1Reference, evaluateMaxPDL1, isLungCancer)
        }.toSet()

        val comparatorMessage = if (evaluateMaxPDL1) "below maximum of" else "above minimum of"

        return when {
            EvaluationResult.PASS in testEvaluations && (EvaluationResult.FAIL in testEvaluations || EvaluationResult.UNDETERMINED in testEvaluations) -> {
                EvaluationFactory.undetermined(
                    "Undetermined if PD-L1 expression $comparatorMessage $pdl1Reference (conflicting PD-L1 results)"
                )
            }

            EvaluationResult.PASS in testEvaluations -> {
                EvaluationFactory.pass("PD-L1 expression $comparatorMessage $pdl1Reference")
            }

            EvaluationResult.FAIL in testEvaluations -> {
                val messageEnding = (if (evaluateMaxPDL1) "exceeds " else "below ") + pdl1Reference
                EvaluationFactory.fail("PD-L1 expression $messageEnding")
            }

            EvaluationResult.UNDETERMINED in testEvaluations -> {
                val testMessage = pdl1TestsWithRequestedMeasurement
                    .joinToString(", ") { "${it.scoreValuePrefix} ${it.scoreValue}" }
                EvaluationFactory.undetermined("Undetermined if PD-L1 expression ($testMessage) $comparatorMessage $pdl1Reference")
            }

            pdl1TestsWithRequestedMeasurement.isNotEmpty() && pdl1TestsWithRequestedMeasurement.any { test -> test.scoreValue == null } -> {
                val status = pdl1TestsWithRequestedMeasurement.joinToString(", ") { it.scoreText ?: "unknown" }
                EvaluationFactory.undetermined(
                    "Unclear if IHC PD-L1 status available ($status) is considered $comparatorMessage $pdl1Reference"
                )
            }

            IhcTestFilter.allPDL1Tests(ihcTests).isNotEmpty() -> {
                EvaluationFactory.recoverableFail("PD-L1 tests not in correct unit ($measure)")
            }

            else -> {
                EvaluationFactory.undetermined("PD-L1 expression (IHC) not tested", isMissingMolecularResultForEvaluation = true)
            }
        }
    }

    private fun evaluateNegativeOrPositiveTestScore(
        ihcTest: IhcTest,
        pdl1Reference: Double,
        evaluateMaxPDL1: Boolean,
        isLungCancer: Boolean?
    ): EvaluationResult? {
        val result = classifyIhcTest(ihcTest)
        val cpsWithRefEqualAbove10 = ihcTest.measure == "CPS" && pdl1Reference >= 10
        val hasTPSTest = ihcTest.measure == "TPS" || isLungCancer == true
        val tpsWithRefEqualAbove1 = hasTPSTest && pdl1Reference >= 1

        return when {
            evaluateMaxPDL1 && result == TestResult.NEGATIVE && (tpsWithRefEqualAbove1 || cpsWithRefEqualAbove10) -> EvaluationResult.PASS

            !evaluateMaxPDL1 && result == TestResult.POSITIVE && pdl1Reference == 1.0 &&
                    (hasTPSTest || ihcTest.measure == "CPS") -> EvaluationResult.PASS

            !evaluateMaxPDL1 && result == TestResult.NEGATIVE && (tpsWithRefEqualAbove1 || cpsWithRefEqualAbove10) -> EvaluationResult.FAIL

            else -> null
        }
    }

    private fun classifyIhcTest(test: IhcTest): TestResult {
        return when {
            test.scoreText?.lowercase()?.contains("negative") == true -> {
                TestResult.NEGATIVE
            }

            test.scoreText?.lowercase()?.contains("positive") == true -> {
                TestResult.POSITIVE
            }

            else -> TestResult.UNKNOWN
        }
    }
}