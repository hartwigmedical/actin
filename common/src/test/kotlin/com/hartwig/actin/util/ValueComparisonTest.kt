package com.hartwig.actin.util

import com.hartwig.actin.algo.evaluation.util.ValueComparison
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test

class ValueComparisonTest {

    // Tests for fun evaluateVersusMinValue
    @Test
    fun `Should pass if value is equal to or larger than min value`() {
        assertThat(ValueComparison.evaluateVersusMinValue(4.0, Strings.EMPTY, 2.0)).isTrue()
        assertThat(ValueComparison.evaluateVersusMinValue(4.0, null, 2.0)).isTrue()
        assertThat(ValueComparison.evaluateVersusMinValue(2.0, ValueComparison.LARGER_THAN, 2.0)).isTrue()
        assertThat(ValueComparison.evaluateVersusMinValue(2.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0)).isTrue()
    }

    @Test
    fun `Should fail if value is less than min value`() {
        assertThat(ValueComparison.evaluateVersusMinValue(1.0, Strings.EMPTY, 2.0)).isFalse()
        assertThat(ValueComparison.evaluateVersusMinValue(1.0, null, 2.0)).isFalse()
        assertThat(ValueComparison.evaluateVersusMinValue(2.0, ValueComparison.SMALLER_THAN, 2.0)).isFalse()
        assertThat(ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0)).isFalse()
    }

    @Test
    fun `Should evaluate to undetermined if value is possibly smaller than min value with uncertainty due to comparator`() {
        assertThat(ValueComparison.evaluateVersusMinValue(2.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0)).isNull()
        assertThat(ValueComparison.evaluateVersusMinValue(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0)).isNull()
        assertThat(ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.LARGER_THAN, 2.0)).isNull()
        assertThat(ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0)).isNull()
    }

    // Tests for fun evaluateVersusMaxValue
    @Test
    fun `Should pass if value is equal to or smaller than max value`() {
        assertThat(ValueComparison.evaluateVersusMaxValue(1.0, Strings.EMPTY, 2.0)).isTrue()
        assertThat(ValueComparison.evaluateVersusMaxValue(1.0, null, 2.0)).isTrue()
        assertThat(ValueComparison.evaluateVersusMaxValue(2.0, ValueComparison.SMALLER_THAN, 2.0)).isTrue()
        assertThat(ValueComparison.evaluateVersusMaxValue(2.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0)).isTrue()
    }

    @Test
    fun `Should fail if value is larger than max value`() {
        assertThat(ValueComparison.evaluateVersusMaxValue(4.0, Strings.EMPTY, 2.0)).isFalse()
        assertThat(ValueComparison.evaluateVersusMaxValue(4.0, null, 2.0)).isFalse()
        assertThat(ValueComparison.evaluateVersusMaxValue(2.0, ValueComparison.LARGER_THAN, 2.0)).isFalse()
        assertThat(ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0)).isFalse()
    }

    @Test
    fun `Should evaluate to undetermined if value is possibly larger than max value with uncertainty due to comparator`() {
        assertThat(ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.SMALLER_THAN, 2.0)).isNull()
        assertThat(ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0)).isNull()
        assertThat(ValueComparison.evaluateVersusMaxValue(1.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0)).isNull()
    }

    @Test
    fun `Should return true if String in collection matches value`() {
        Assert.assertTrue(
            ValueComparison.stringCaseInsensitivelyMatchesQueryCollection(
                "HAYneedleSTACK",
                listOf("Missing", "Unknown", "Needle", "Another")
            )
        )
    }

    @Test
    fun `Should return false if no String in collection matches value`() {
        Assert.assertFalse(
            ValueComparison.stringCaseInsensitivelyMatchesQueryCollection(
                "HAYneedleSTACK",
                listOf("Missing", "Unknown", "Another")
            )
        )
    }

    @Test
    fun `Should return false for empty value`() {
        Assert.assertFalse(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("", listOf("Missing", "Unknown", "Another")))
    }

    @Test
    fun `Should return false for empty collection`() {
        Assert.assertFalse(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("HAYneedleSTACK", emptyList()))
    }
}