package com.hartwig.actin.util

import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ValueComparisonTest {

    @Test
    fun `Should pass if value is equal to or larger than min value`() {
        assertThat(evaluateAgainstMinValue2(4.0, "")).isEqualTo(EvaluationResult.PASS)
        assertThat(evaluateAgainstMinValue2(4.0, null)).isEqualTo(EvaluationResult.PASS)
        assertThat(evaluateAgainstMinValue2(2.0, ValueComparison.LARGER_THAN)).isEqualTo(EvaluationResult.PASS)
        assertThat(evaluateAgainstMinValue2(2.0, ValueComparison.LARGER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should be undetermined if value is less than min value`() {
        assertThat(evaluateAgainstMinValue2(4.0, ValueComparison.SMALLER_THAN)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluateAgainstMinValue2(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluateAgainstMinValue2(1.0, ValueComparison.LARGER_THAN)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluateAgainstMinValue2(1.0, ValueComparison.LARGER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @Test
    fun `Should evaluate to undetermined if value is possibly smaller than min value with uncertainty due to comparator`() {
        assertThat(evaluateAgainstMinValue2(2.0, ValueComparison.SMALLER_THAN_OR_EQUAL)).isEqualTo(EvaluationResult.UNDETERMINED)
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

    private fun evaluateAgainstMinValue2(value: Double, comparator: String?): EvaluationResult {
        return ValueComparison.evaluateVersusMinValue(value, comparator, 2.0)
    }

    private fun evaluateAgainstMaxValue2(value: Double, comparator: String?): EvaluationResult {
        return ValueComparison.evaluateVersusMaxValue(value, comparator, 2.0)
    }
}