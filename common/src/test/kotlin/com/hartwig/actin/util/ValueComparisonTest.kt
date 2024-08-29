package com.hartwig.actin.util

import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class ValueComparisonTest {

    // Tests for fun evaluateVersusMinValue
    @Test
    fun `Should pass if value is equal to or larger than min value`() {
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4.0, "", 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4.0, null, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(2.0, ValueComparison.LARGER_THAN, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(2.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0))
    }

    @Test
    fun `Should fail if value is less than min value`() {
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(2.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.LARGER_THAN, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0))
    }

    @Test
    fun `Should evaluate to undetermined if value is possibly smaller than min value with uncertainty due to comparator`() {
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(2.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.LARGER_THAN, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0))
    }

    // Tests for fun evaluateVersusMaxValue
    @Test
    fun `Should pass if value is equal to or smaller than max value`() {
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1.0, "", 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1.0, null, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(2.0, ValueComparison.SMALLER_THAN, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(2.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
    }

    @Test
    fun `Should fail if value is larger than max value`() {
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4.0, "", 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4.0, null, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.LARGER_THAN, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0))
    }

    @Test
    fun `Should evaluate to undetermined if value is possibly larger than max value with uncertainty due to comparator`() {
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.SMALLER_THAN, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(1.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0))
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