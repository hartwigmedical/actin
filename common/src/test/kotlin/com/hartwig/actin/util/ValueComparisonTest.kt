package com.hartwig.actin.util

import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ValueComparisonTest {

    @Test
    fun `Should pass if value is equal to or larger than min value`() {
        assertThat(evaluateAgainstMinValue2(4.0, "")).isEqualTo(EvaluationResult.PASS)
        assertThat(evaluateAgainstMinValue2(4.0, null)).isEqualTo(EvaluationResult.PASS)
        assertThat(evaluateAgainstMinValue2(2.0, ValueComparison.LARGER_THAN)).isEqualTo(EvaluationResult.PASS)
        assertThat(evaluateAgainstMinValue2(2.0, ValueComparison.LARGER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should fail if value is less than min value`() {
        assertThat(evaluateAgainstMinValue2(2.0, ValueComparison.SMALLER_THAN)).isEqualTo(EvaluationResult.FAIL)
        assertThat(evaluateAgainstMinValue2(1.0, ValueComparison.SMALLER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.FAIL)
        assertThat(evaluateAgainstMinValue2(1.0, "")).isEqualTo(EvaluationResult.FAIL)
        assertThat(evaluateAgainstMinValue2(1.0, null)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should evaluate to undetermined if value is possibly smaller than min value with uncertainty due to comparator`() {
        assertThat(evaluateAgainstMinValue2(2.0, ValueComparison.SMALLER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluateAgainstMinValue2(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluateAgainstMinValue2(1.0, ValueComparison.LARGER_THAN)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluateAgainstMinValue2(1.0, ValueComparison.LARGER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @Test
    fun `Should pass if value is equal to or smaller than max value`() {
        assertThat(evaluateAgainstMaxValue2(1.0, "")).isEqualTo(EvaluationResult.PASS)
        assertThat(evaluateAgainstMaxValue2(1.0, null)).isEqualTo(EvaluationResult.PASS)
        assertThat(evaluateAgainstMaxValue2(2.0, ValueComparison.SMALLER_THAN)).isEqualTo(EvaluationResult.PASS)
        assertThat(evaluateAgainstMaxValue2(2.0, ValueComparison.SMALLER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should fail if value is larger than max value`() {
        assertThat(evaluateAgainstMaxValue2(4.0, "")).isEqualTo(EvaluationResult.FAIL)
        assertThat(evaluateAgainstMaxValue2(4.0, null)).isEqualTo(EvaluationResult.FAIL)
        assertThat(evaluateAgainstMaxValue2(4.0, ValueComparison.LARGER_THAN)).isEqualTo(EvaluationResult.FAIL)
        assertThat(evaluateAgainstMaxValue2(4.0, ValueComparison.LARGER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should evaluate to undetermined if value is possibly larger than max value with uncertainty due to comparator`() {
        assertThat(evaluateAgainstMaxValue2(4.0, ValueComparison.SMALLER_THAN)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluateAgainstMaxValue2(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluateAgainstMaxValue2(1.0, ValueComparison.LARGER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @Test
    fun `Should return true if String in collection matches value`() {
        assertThat(
            ValueComparison.stringCaseInsensitivelyMatchesQueryCollection(
                "HAYneedleSTACK",
                listOf("Missing", "Unknown", "Needle", "Another")
            )
        ).isTrue()
    }

    @Test
    fun `Should return false if no String in collection matches value`() {
        assertThat(
            ValueComparison.stringCaseInsensitivelyMatchesQueryCollection(
                "HAYneedleSTACK",
                listOf("Missing", "Unknown", "Another")
            )
        ).isFalse()
    }

    @Test
    fun `Should return false for empty value`() {
        assertThat(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("", listOf("Missing", "Unknown", "Another"))).isFalse()
    }

    @Test
    fun `Should return false for empty collection`() {
        assertThat(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("HAYneedleSTACK", emptyList())).isFalse()
    }

    @ParameterizedTest
    @CsvSource(
        "4.0, 8.0",
        "2.0, 2.0",
        "3.0, NULL",
        nullValues = ["NULL"],
        )
    fun `evaluateBoundsVersusMinValue should pass when lower bound is at or above min value`(lower: Double?, upper: Double?) {
        assertThat(ValueComparison.evaluateBoundsVersusMinValue(lower, upper, 2.0)).isEqualTo(EvaluationResult.PASS)
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, 1.0",
        "NULL, 1.0",
        nullValues = ["NULL"]
    )
    fun `evaluateBoundsVersusMinValue should fail when upper bound is below min value`(lower: Double?, upper: Double?) {
        assertThat(ValueComparison.evaluateBoundsVersusMinValue(lower, upper, 2.0)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `evaluateBoundsVersusMinValue should be undetermined when bounds are null`() {
        assertThat(ValueComparison.evaluateBoundsVersusMinValue(null, null, 2.0)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 3.0",
        "NULL, 3.0",
        "1.0, NULL",
        nullValues = ["NULL"]
    )
    fun `evaluateBoundsVersusMinValue should be undetermined when range include min value`(lower: Double?, upper: Double?) {
        assertThat(ValueComparison.evaluateBoundsVersusMinValue(lower, upper, 2.0)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 1.0",
        "2.0, 2.0",
        "NULL, 1.0",
        nullValues = ["NULL"]
    )
    fun `evaluateBoundsVersusMaxValue should pass when upper bound is at or below max value`(lower: Double?, upper: Double?) {
        assertThat(ValueComparison.evaluateBoundsVersusMaxValue(lower, upper, 2.0)).isEqualTo(EvaluationResult.PASS)
    }

    @ParameterizedTest
    @CsvSource(
        "4.0, 4.0",
        "3.0, NULL",
        nullValues = ["NULL"]
    )
    fun `evaluateBoundsVersusMaxValue should fail when lower bound is above max value`(lower: Double?, upper: Double?) {
        assertThat(ValueComparison.evaluateBoundsVersusMaxValue(lower, upper, 2.0)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `evaluateBoundsVersusMaxValue should be undetermined when bounds are null`() {
        assertThat(ValueComparison.evaluateBoundsVersusMaxValue(null, null, 2.0)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 3.0",
        "NULL, 3.0",
        "1.0, NULL",
        nullValues = ["NULL"]
    )
    fun `evaluateBoundsVersusMaxValue should be undetermined when range include max value`(lower: Double?, upper: Double?) {
        assertThat(ValueComparison.evaluateBoundsVersusMaxValue(lower, upper, 2.0)).isEqualTo(EvaluationResult.UNDETERMINED)
    }


    private fun evaluateAgainstMinValue2(value: Double, comparator: String?): EvaluationResult {
        return ValueComparison.evaluateVersusMinValue(value, comparator, 2.0)
    }

    private fun evaluateAgainstMaxValue2(value: Double, comparator: String?): EvaluationResult {
        return ValueComparison.evaluateVersusMaxValue(value, comparator, 2.0)
    }
}