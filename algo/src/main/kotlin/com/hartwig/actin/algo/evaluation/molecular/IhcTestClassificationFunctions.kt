package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.IhcTestEvaluationConstants
import com.hartwig.actin.datamodel.clinical.IhcTest

object IhcTestClassificationFunctions {

    enum class TestResult {
        POSITIVE,
        NEGATIVE,
        BORDERLINE,
        UNKNOWN
    }

    fun classifyHer2Test(test: IhcTest): TestResult {
        return classifyTest(test, "+", 2, 3, 3)
    }

    fun classifyPrOrErTest(test: IhcTest): TestResult {
        return classifyTest(test, "%", 1, 10, 100)
    }

    private fun classifyTest(
        test: IhcTest,
        unit: String,
        negativeUpperBound: Int,
        positiveLowerBound: Int,
        positiveUpperBound: Int
    ): TestResult {
        val scoreValue = test.scoreValue?.toInt()
        return when {
            test.impliesPotentialIndeterminateStatus -> TestResult.UNKNOWN

            test.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS || scoreValue == 0 ||
                    (scoreValue in 0 until negativeUpperBound && test.scoreValueUnit == unit) -> TestResult.NEGATIVE

            test.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS ||
                    (scoreValue in positiveLowerBound..positiveUpperBound && test.scoreValueUnit == unit) -> TestResult.POSITIVE

            scoreValue in negativeUpperBound until positiveLowerBound && test.scoreValueUnit == unit -> TestResult.BORDERLINE

            else -> TestResult.UNKNOWN
        }
    }
}