package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest

class IhcTestEvaluation(val filteredTests: Set<IhcTest>) {

    fun hasCertainPositiveResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && filteredTests.all {
            it.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS && !it.impliesPotentialIndeterminateStatus
        }

    fun hasPossiblePositiveResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && !filteredTests.all { test ->
            (test.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS || testValueZero(test)) &&
                    !test.impliesPotentialIndeterminateStatus
        }

    fun hasCertainNegativeResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && filteredTests.all {
            it.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS && !it.impliesPotentialIndeterminateStatus
        }

    fun hasPossibleNegativeResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && !filteredTests.all { test ->
            (test.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS || testValueAboveZero(test)) &&
                    !test.impliesPotentialIndeterminateStatus
        }

    fun hasCertainLossResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && filteredTests.all {
            it.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_LOSS_TERMS && !it.impliesPotentialIndeterminateStatus
        }

    fun hasPossibleLossResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && !filteredTests.all { test ->
            (test.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_NO_LOSS_TERMS) && !test.impliesPotentialIndeterminateStatus
        }

    fun hasCertainWildtypeResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && filteredTests.all {
            it.scoreText?.lowercase() in IhcTestEvaluationConstants.WILD_TYPE_TERMS && !it.impliesPotentialIndeterminateStatus
        }

    private fun testValueAboveZero(ihcTest: IhcTest) = ihcTest.scoreValue?.let { scoreValue ->
        ValueComparison.evaluateVersusMinValue(scoreValue, ihcTest.scoreValuePrefix, 0.0)
    } == EvaluationResult.PASS

    private fun testValueZero(ihcTest: IhcTest) = ihcTest.scoreValue?.let { scoreValue ->
        ValueComparison.evaluateVersusMaxValue(scoreValue, ihcTest.scoreValuePrefix, 0.0)
    } == EvaluationResult.PASS

    companion object {
        fun create(item: String, ihcTests: List<IhcTest>): IhcTestEvaluation {
            return IhcTestEvaluation(IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem(item = item, ihcTests = ihcTests))
        }
    }
}