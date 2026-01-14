package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.IhcTestEvaluationConstants
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.IhcTestResult

object IhcTestClassificationFunctions {

    fun classifyHer2Test(test: IhcTest): IhcTestResult {
        return classifyTest(test, "+", 0, 1, 1, 3, 3)
    }

    fun classifyPrOrErTest(test: IhcTest): IhcTestResult {
        return classifyTest(test, "%", 1, 1, 10, 10, 100)
    }

    private fun classifyTest(
        test: IhcTest,
        unit: String,
        negativeUpperBound: Int,
        lowLowerBound: Int,
        lowUpperBound: Int,
        positiveLowerBound: Int,
        positiveUpperBound: Int
    ): IhcTestResult {
        val scoreValue = test.scoreValue?.toInt()
        return when {
            test.impliesPotentialIndeterminateStatus -> IhcTestResult.UNKNOWN

            test.scoreText?.lowercase() in IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS || scoreValue == 0 ||
                    (scoreValue in 0 until negativeUpperBound && test.scoreValueUnit == unit) -> IhcTestResult.NEGATIVE

            test.scoreText?.lowercase() in IhcTestEvaluationConstants.LOW_TERMS ||
                    (scoreValue in lowLowerBound..lowUpperBound && test.scoreValueUnit == unit) -> IhcTestResult.LOW

            test.scoreText?.lowercase() in IhcTestEvaluationConstants.BROAD_POSITIVE_TERMS ||
                    (scoreValue in positiveLowerBound..positiveUpperBound && test.scoreValueUnit == unit) -> IhcTestResult.POSITIVE

            scoreValue in negativeUpperBound until positiveLowerBound && test.scoreValueUnit == unit -> IhcTestResult.BORDERLINE

            else -> IhcTestResult.UNKNOWN
        }
    }
}