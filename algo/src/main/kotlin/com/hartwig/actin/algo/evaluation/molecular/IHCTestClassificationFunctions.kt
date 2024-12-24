package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.clinical.PriorIHCTest

object IHCTestClassificationFunctions {

    enum class TestResult {
        POSITIVE,
        NEGATIVE,
        BORDERLINE,
        UNKNOWN
    }

    fun classifyHer2Test(test: PriorIHCTest): TestResult {
        return classifyTest(test, "+", 2, 3, 3)
    }

    fun classifyPrOrErTest(test: PriorIHCTest): TestResult {
        return classifyTest(test, "%", 1, 10, 100)
    }

    fun classifyIhcTest(test: PriorIHCTest): TestResult {
        return classifyTest(test, "%", 1, 1, 100)
    }

    private fun classifyTest(
        test: PriorIHCTest, unit: String, negativeUpperBound: Int, positiveLowerBound: Int, positiveUpperBound: Int
    ): TestResult {
        val scoreValue = test.scoreValue?.toInt()
        return when {
            test.impliesPotentialIndeterminateStatus -> TestResult.UNKNOWN

            test.scoreText?.lowercase() == "negative" || (scoreValue in 0 until negativeUpperBound && test.scoreValueUnit == unit) -> {
                TestResult.NEGATIVE
            }

            test.scoreText?.lowercase() == "positive" ||
                    (scoreValue in positiveLowerBound..positiveUpperBound && test.scoreValueUnit == unit) -> {
                TestResult.POSITIVE
            }

            scoreValue in negativeUpperBound until positiveLowerBound && test.scoreValueUnit == unit -> {
                TestResult.BORDERLINE
            }

            else -> TestResult.UNKNOWN
        }
    }

}