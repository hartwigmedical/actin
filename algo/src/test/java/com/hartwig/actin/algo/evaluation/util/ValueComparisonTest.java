package com.hartwig.actin.algo.evaluation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class ValueComparisonTest {

    @Test
    public void canEvaluateVersusMinValue() {
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4D, Strings.EMPTY, 2D));
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4D, null, 2D));
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMinValue(1D, Strings.EMPTY, 2D));
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMinValue(1D, null, 2D));

        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4D, ValueComparison.LARGER_THAN, 2D));
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMinValue(4D, ValueComparison.LARGER_THAN_OR_EQUAL, 2D));
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMinValue(1D, ValueComparison.SMALLER_THAN, 2D));
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMinValue(1D, ValueComparison.SMALLER_THAN_OR_EQUAL, 2D));

        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(4D, ValueComparison.SMALLER_THAN, 2D));
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(4D, ValueComparison.SMALLER_THAN_OR_EQUAL, 2D));
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(1D, ValueComparison.LARGER_THAN, 2D));
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMinValue(1D, ValueComparison.LARGER_THAN_OR_EQUAL, 2D));
    }

    @Test
    public void canEvaluateVersusMaxValue() {
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1D, Strings.EMPTY, 2D));
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1D, null, 2D));
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4D, Strings.EMPTY, 2D));
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4D, null, 2D));

        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1D, ValueComparison.SMALLER_THAN, 2D));
        assertEquals(EvaluationResult.PASS, ValueComparison.evaluateVersusMaxValue(1D, ValueComparison.SMALLER_THAN_OR_EQUAL, 2D));
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4D, ValueComparison.LARGER_THAN, 2D));
        assertEquals(EvaluationResult.FAIL, ValueComparison.evaluateVersusMaxValue(4D, ValueComparison.LARGER_THAN_OR_EQUAL, 2D));

        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(4D, ValueComparison.SMALLER_THAN, 2D));
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(4D, ValueComparison.SMALLER_THAN_OR_EQUAL, 2D));
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(1D, ValueComparison.LARGER_THAN, 2D));
        assertEquals(EvaluationResult.UNDETERMINED, ValueComparison.evaluateVersusMaxValue(1D, ValueComparison.LARGER_THAN_OR_EQUAL, 2D));
    }

    @Test
    public void shouldReturnTrueIfStringInCollectionMatchesValue() {
        assertTrue(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("HAYneedleSTACK",
                List.of("Missing", "Unknown", "Needle", "Another")));
    }

    @Test
    public void shouldReturnFalseIfNoStringInCollectionMatchesValue() {
        assertFalse(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("HAYneedleSTACK",
                List.of("Missing", "Unknown", "Another")));
    }

    @Test
    public void shouldReturnFalseForEmptyValue() {
        assertFalse(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("", List.of("Missing", "Unknown", "Another")));
    }

    @Test
    public void shouldReturnFalseForEmptyCollection() {
        assertFalse(ValueComparison.stringCaseInsensitivelyMatchesQueryCollection("HAYneedleSTACK", Collections.emptyList()));
    }
}