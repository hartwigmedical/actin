package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.clinical.IhcTest
import org.junit.Test
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat

class IhcTestEvaluationTest {

    private val item = "item X"
    private val date = LocalDate.of(2024, 2, 2)
    private val moreRecentDate = LocalDate.of(2024, 4, 2)

    @Test
    fun `Should set both booleans to true if only positive results present for positive results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item")
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4)).hasPositiveIhcTestResultsForItem()

        assertThat(certain).isTrue
        assertThat(possible).isTrue
    }

    @Test
    fun `Should set certain boolean to false and possible boolean to true if only non-certain positive results present for positive results function`() {
        val test1 = ihcTest(item = item, scoreText = "Some", measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first())
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4)).hasPositiveIhcTestResultsForItem()

        assertThat(certain).isFalse
        assertThat(possible).isTrue
    }

    @Test
    fun `Should set both booleans to false if only negative results present for positive results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first())
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4)).hasPositiveIhcTestResultsForItem()

        assertThat(certain).isFalse
        assertThat(possible).isFalse
    }

    @Test
    fun `Should set certain boolean to false and possible boolean to true if recent measure is positive but without date is not for positive results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2, test3)).hasPositiveIhcTestResultsForItem()

        assertThat(certain).isFalse
        assertThat(possible).isTrue
    }

    @Test
    fun `Should set both booleans to true if recent measure is positive for positive results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = moreRecentDate)
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2)).hasPositiveIhcTestResultsForItem()

        assertThat(certain).isTrue
        assertThat(possible).isTrue
    }

    @Test
    fun `Should set both booleans to false if recent measure has score value of 0 for positive results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = null, scoreValue = 0.0, measureDate = moreRecentDate)
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2)).hasPositiveIhcTestResultsForItem()

        assertThat(certain).isFalse
        assertThat(possible).isFalse
    }

    @Test
    fun `Should set both booleans to false if there are no tests for that item for positive results function`() {
        val test1 = ihcTest(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.last())
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2)).hasPositiveIhcTestResultsForItem()

        assertThat(certain).isFalse
        assertThat(possible).isFalse
    }

    @Test
    fun `Should set both booleans to true if only negative results present for negative results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item")
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4)).hasNegativeIhcTestResultsForItem()

        assertThat(certain).isTrue
        assertThat(possible).isTrue
    }

    @Test
    fun `Should set certain boolean to false and possible boolean to true if only non-certain negative results present for negative results function`() {
        val test1 = ihcTest(item = item, scoreText = "Some", measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first())
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4)).hasNegativeIhcTestResultsForItem()

        assertThat(certain).isFalse
        assertThat(possible).isTrue
    }

    @Test
    fun `Should set both booleans to false if only positive results present for negative results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first())
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4)).hasNegativeIhcTestResultsForItem()

        assertThat(certain).isFalse
        assertThat(possible).isFalse
    }

    @Test
    fun `Should set certain boolean to false and possible boolean to true if recent measure is negative but without date is not for negative results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2, test3)).hasNegativeIhcTestResultsForItem()

        assertThat(certain).isFalse
        assertThat(possible).isTrue
    }

    @Test
    fun `Should set both booleans to true if recent measure is negative for negative results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = moreRecentDate)
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2)).hasNegativeIhcTestResultsForItem()

        assertThat(certain).isTrue
        assertThat(possible).isTrue
    }

    @Test
    fun `Should set both booleans to false if recent measure has score value above 0 for negative results function`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = null, scoreValue = 2.0, measureDate = moreRecentDate)
        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2)).hasNegativeIhcTestResultsForItem()

        assertThat(certain).isFalse
        assertThat(possible).isFalse
    }

    @Test
    fun `Should set both booleans to false if there are no tests for that item for negative results function`() {
        val test1 = ihcTest(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.last())

        val (certain, possible) = IhcTestEvaluation.create(item, listOf(test1, test2)).hasNegativeIhcTestResultsForItem()
        assertThat(certain).isFalse
        assertThat(possible).isFalse
    }

    private fun ihcTest(item: String = "", scoreText: String? = null, scoreValue: Double? = null, measureDate: LocalDate? = null): IhcTest {
        return IhcTest(item = item, scoreText = scoreText, scoreValue = scoreValue, measureDate = measureDate)
    }
}