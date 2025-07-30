package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest

class IhcTestEvaluation(private val filteredTests: Set<IhcTest>) {

    fun hasCertainPositiveIhcTestResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && filteredTests.all { it.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS }

    fun hasPossiblePositiveTestResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && !filteredTests.all { test ->
            IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.any { it == test.scoreText?.lowercase() } || testValueZero(test)
        }

    fun hasPositiveIhcTestResultsForItem(): Pair<Boolean, Boolean> =
        Pair(hasCertainPositiveIhcTestResultsForItem(), hasPossiblePositiveTestResultsForItem())

    fun hasCertainNegativeIhcTestResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && filteredTests.all { it.scoreText?.lowercase() in IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS }

    fun hasPossibleNegativeIhcTestResultsForItem(): Boolean =
        filteredTests.isNotEmpty() && !filteredTests.all { test ->
            IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.any { it == test.scoreText?.lowercase() } || testValueAboveZero(test)
        }

    fun hasNegativeIhcTestResultsForItem(): Pair<Boolean, Boolean> =
        Pair(hasCertainNegativeIhcTestResultsForItem(), hasPossibleNegativeIhcTestResultsForItem())

    private fun testValueAboveZero(ihcTest: IhcTest) = ihcTest.scoreValue?.let { scoreValue ->
        ValueComparison.evaluateVersusMinValue(scoreValue, ihcTest.scoreValuePrefix, 0.0)
    } == EvaluationResult.PASS

    private fun testValueZero(ihcTest: IhcTest) = ihcTest.scoreValue?.let { scoreValue ->
        ValueComparison.evaluateVersusMaxValue(scoreValue, ihcTest.scoreValuePrefix, 0.0)
    } == EvaluationResult.PASS

    companion object {
        fun create(item: String, ihcTests: List<IhcTest>): IhcTestEvaluation {
            val selectedTests = IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem(item = item, ihcTests = ihcTests)
            return IhcTestEvaluation(selectedTests)
        }
    }
}