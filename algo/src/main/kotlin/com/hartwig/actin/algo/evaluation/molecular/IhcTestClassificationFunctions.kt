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
        val scoreLowerBound = test.scoreLowerBound?.toInt()
        val scoreUpperBound = test.scoreUpperBound?.toInt()
        val unitMatches = test.scoreValueUnit == unit

        fun bothBoundsInRange(range: IntRange): Boolean {
            return scoreLowerBound in range && scoreUpperBound in range
        }

        return when {
            test.impliesPotentialIndeterminateStatus -> IhcTestResult.UNKNOWN

            test.scoreText?.lowercase() in IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS || scoreUpperBound == 0 ||
                    (scoreUpperBound in 0 until negativeUpperBound && unitMatches) -> IhcTestResult.NEGATIVE

            test.scoreText?.lowercase() in IhcTestEvaluationConstants.LOW_TERMS ||
                    (bothBoundsInRange(lowLowerBound..lowUpperBound) && unitMatches) -> IhcTestResult.LOW

            test.scoreText?.lowercase() in IhcTestEvaluationConstants.BROAD_POSITIVE_TERMS ||
                    (scoreLowerBound in positiveLowerBound..positiveUpperBound && unitMatches) -> IhcTestResult.POSITIVE

            bothBoundsInRange(negativeUpperBound until positiveLowerBound) && unitMatches -> IhcTestResult.BORDERLINE

            else -> IhcTestResult.UNKNOWN
        }
    }
}