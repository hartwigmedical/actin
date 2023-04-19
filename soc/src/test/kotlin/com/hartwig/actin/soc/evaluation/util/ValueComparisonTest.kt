package com.hartwig.actin.soc.evaluation.util

import com.hartwig.actin.algo.datamodel.EvaluationResult
import junit.framework.TestCase.assertEquals
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class ValueComparisonTest {
    @Test
    fun canEvaluateVersusMinValue() {
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4.0, Strings.EMPTY, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4.0, null, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMinValue(1.0, Strings.EMPTY, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMinValue(1.0, null, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4.0, ValueComparison.LARGER_THAN, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.SMALLER_THAN, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(4.0, ValueComparison.SMALLER_THAN, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.LARGER_THAN, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(1.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0))
    }

    @Test
    fun canEvaluateVersusMaxValue() {
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1.0, Strings.EMPTY, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1.0, null, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4.0, Strings.EMPTY, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4.0, null, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1.0, ValueComparison.SMALLER_THAN, 2.0))
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.LARGER_THAN, 2.0))
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.SMALLER_THAN, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(4.0, ValueComparison.SMALLER_THAN_OR_EQUAL, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(1.0, ValueComparison.LARGER_THAN, 2.0))
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(1.0, ValueComparison.LARGER_THAN_OR_EQUAL, 2.0))
    }

    @Test
    fun shouldReturnTrueIfStringInCollectionMatchesValue() {
        Assert.assertTrue(
            ValueComparison.stringCaseInsensitivelyMatchesQueryCollection(
                "HAYneedleSTACK",
                listOf("Missing", "Unknown", "Needle", "Another")
            )
        )
    }

    @Test
    fun shouldReturnFalseIfNoStringInCollectionMatchesValue() {
        Assert.assertFalse(
            ValueComparison.stringCaseInsensitivelyMatchesQueryCollection(
                "HAYneedleSTACK",
                listOf("Missing", "Unknown", "Another")
            )
        )
    }

    @Test
    fun shouldReturnFalseForEmptyValue() {
        Assert.assertFalse(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("", listOf("Missing", "Unknown", "Another")))
    }

    @Test
    fun shouldReturnFalseForEmptyCollection() {
        Assert.assertFalse(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("HAYneedleSTACK", emptyList()))
    }
}