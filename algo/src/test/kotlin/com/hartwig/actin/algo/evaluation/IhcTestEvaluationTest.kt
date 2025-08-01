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
    fun `Should set positive certain and positive possible function to true if only positive results present`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item")
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainPositiveResultsForItem()).isTrue
        assertThat(ihcEvaluation.hasPossiblePositiveResultsForItem()).isTrue
    }

    @Test
    fun `Should set positive certain function to false and positive possible function to true if only positive results present but results are indeterminate`() {
        val test1 = ihcTest(
            item = item,
            scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(),
            measureDate = date,
            indeterminateStatus = true
        )
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item")
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainPositiveResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossiblePositiveResultsForItem()).isTrue
    }

    @Test
    fun `Should set positive certain function to false and positive possible function to true if only non-certain positive results present`() {
        val test1 = ihcTest(item = item, scoreText = "Some", measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainPositiveResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossiblePositiveResultsForItem()).isTrue
    }

    @Test
    fun `Should set both positive functions to false if only negative results with indeterminate status false`() {
        val test1 = ihcTest(
            item = item,
            scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(),
            measureDate = date,
            indeterminateStatus = false
        )
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainPositiveResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossiblePositiveResultsForItem()).isFalse
    }

    @Test
    fun `Should set positive certain function to false and positive possible function to true if only negative results but with indeterminate status true`() {
        val test1 = ihcTest(
            item = item,
            scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(),
            measureDate = date,
            indeterminateStatus = true
        )
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isTrue
    }

    @Test
    fun `Should set positive certain function to false and positive possible function to true if recent measure is positive but without date is not`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3))

        assertThat(ihcEvaluation.hasCertainPositiveResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossiblePositiveResultsForItem()).isTrue
    }

    @Test
    fun `Should set both positive functions to true if recent measure is positive`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = moreRecentDate)
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2))

        assertThat(ihcEvaluation.hasCertainPositiveResultsForItem()).isTrue
        assertThat(ihcEvaluation.hasPossiblePositiveResultsForItem()).isTrue
    }

    @Test
    fun `Should set both positive functions to false if recent measure has score value of 0`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = null, scoreValue = 0.0, measureDate = moreRecentDate)
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2))

        assertThat(ihcEvaluation.hasCertainPositiveResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossiblePositiveResultsForItem()).isFalse
    }

    @Test
    fun `Should set both both positive functions to false if there are no tests for that item for positive results function`() {
        val test1 = ihcTest(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.last())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2))

        assertThat(ihcEvaluation.hasCertainPositiveResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossiblePositiveResultsForItem()).isFalse
    }

    @Test
    fun `Should set negative certain and positive possible function to true if only negative results present`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item")
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isTrue
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isTrue
    }

    @Test
    fun `Should set negative certain function to false and negative possible function to true if only negative results present but results are indeterminate`() {
        val test1 = ihcTest(
            item = item,
            scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(),
            measureDate = date,
            indeterminateStatus = true
        )
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item")
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isTrue
    }

    @Test
    fun `Should set negative certain function to false and negative possible function to true if only non-certain negative results present`() {
        val test1 = ihcTest(item = item, scoreText = "Some", measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isTrue
    }

    @Test
    fun `Should set both negative functions to false if only positive results with indeterminate status false`() {
        val test1 = ihcTest(
            item = item,
            scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(),
            measureDate = date,
            indeterminateStatus = false
        )
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isFalse
    }

    @Test
    fun `Should set negative certain function to false and negative possible function to true if only positive results but with indeterminate status true`() {
        val test1 = ihcTest(
            item = item,
            scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(),
            measureDate = date,
            indeterminateStatus = true
        )
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isTrue
    }

    @Test
    fun `Should set negative certain function to false and negative possible function to true if recent measure is negative but without date is not`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isTrue
    }

    @Test
    fun `Should set both negative functions to true if recent measure is negative`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = moreRecentDate)
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isTrue
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isTrue
    }

    @Test
    fun `Should set both negative functions to false if recent measure has score value above 0 `() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = null, scoreValue = 2.0, measureDate = moreRecentDate)
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isFalse
    }

    @Test
    fun `Should set both negative functons to false if there are no tests for that item`() {
        val test1 = ihcTest(item = "other item", scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.last())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2))

        assertThat(ihcEvaluation.hasCertainNegativeResultsForItem()).isFalse
        assertThat(ihcEvaluation.hasPossibleNegativeResultsForItem()).isFalse
    }

    @Test
    fun `Should set wildtype certain function to true if only wildtype results present`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.WILD_TYPE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(measureDate = moreRecentDate)
        val test3 = test1.copy(measureDate = null)
        val test4 = test1.copy(item = "other item")
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2, test3, test4))

        assertThat(ihcEvaluation.hasCertainWildtypeResultsForItem()).isTrue
    }

    @Test
    fun `Should set wildtype certain function to false if positive results present`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.WILD_TYPE_TERMS.first())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2))

        assertThat(ihcEvaluation.hasCertainWildtypeResultsForItem()).isFalse
    }

    @Test
    fun `Should set wildtype certain function to false if negative results present`() {
        val test1 = ihcTest(item = item, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first(), measureDate = date)
        val test2 = test1.copy(item = "other item", scoreText = IhcTestEvaluationConstants.WILD_TYPE_TERMS.first())
        val ihcEvaluation = IhcTestEvaluation.create(item, listOf(test1, test2))

        assertThat(ihcEvaluation.hasCertainWildtypeResultsForItem()).isFalse
    }

    private fun ihcTest(
        item: String = "",
        scoreText: String? = null,
        scoreValue: Double? = null,
        measureDate: LocalDate? = null,
        indeterminateStatus: Boolean = false
    ): IhcTest {
        return IhcTest(
            item = item,
            scoreText = scoreText,
            scoreValue = scoreValue,
            measureDate = measureDate,
            impliesPotentialIndeterminateStatus = indeterminateStatus
        )
    }
}