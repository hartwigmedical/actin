package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter
import com.hartwig.actin.datamodel.clinical.IhcTest

object IhcTestEvaluation {

    fun hasPositiveIhcTestResultsForItem(item: String, ihcTests: List<IhcTest>): Pair<Boolean, Boolean> {
        val tests = IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem(item = item, ihcTests = ihcTests)
        val hasCertainPositiveResults =
            tests.isNotEmpty() && tests.all { test -> IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.any { it == test.scoreText?.lowercase() } }
        val hasPossiblePositiveResults =
            tests.isNotEmpty() && !tests.all { test -> IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.any { it == test.scoreText?.lowercase() }
                    || test.scoreValue?.toInt() == 0 }

        return Pair(hasCertainPositiveResults, hasPossiblePositiveResults)
    }

    fun hasNegativeIhcTestResultsForItem(item: String, ihcTests: List<IhcTest>): Pair<Boolean, Boolean> {
        val tests = IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem(item = item, ihcTests = ihcTests)
        val hasCertainNegativeResults =
            tests.isNotEmpty() && tests.all { test -> IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.any { it == test.scoreText?.lowercase() } }
        val hasPossibleNegativeResults =
            tests.isNotEmpty() && !tests.all { test -> IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.any { it == test.scoreText?.lowercase() }
                    || (test.scoreValue ?: 0.0) > 0 }

        return Pair(hasCertainNegativeResults, hasPossibleNegativeResults)
    }
}