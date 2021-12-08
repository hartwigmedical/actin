package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class LabValueEvaluationTest {

    @Test
    public void canDetermineWhetherLabValueIsAsExpected() {
        assertFalse(LabValueEvaluation.existsWithExpectedUnit(null, "expected unit"));

        LabValue value = LaboratoryTestUtil.builder().unit("expected unit").build();
        assertFalse(LabValueEvaluation.existsWithExpectedUnit(value, "not expected unit"));
        assertTrue(LabValueEvaluation.existsWithExpectedUnit(value, "expected unit"));
    }

    @Test
    public void canEvaluateVersusMinULN() {
        ImmutableLabValue.Builder builder = LaboratoryTestUtil.builder().refLimitLow(30D);

        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMinULN(LaboratoryTestUtil.builder().build(), 2D));

        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMinULN(builder.value(80D).build(), 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMinULN(builder.value(50D).build(), 2D));
    }

    @Test
    public void canEvaluateVersusMinValue() {
        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMinValue(4D, Strings.EMPTY, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMinValue(1D, Strings.EMPTY, 2D));

        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMinValue(4D, LabValueEvaluation.LARGER_THAN, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMinValue(1D, LabValueEvaluation.SMALLER_THAN, 2D));

        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMinValue(4D, LabValueEvaluation.SMALLER_THAN, 2D));
        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMinValue(1D, LabValueEvaluation.LARGER_THAN, 2D));
    }

    @Test
    public void canEvaluateVersusMaxULN() {
        ImmutableLabValue.Builder builder = LaboratoryTestUtil.builder().refLimitUp(50D);

        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMaxULN(LaboratoryTestUtil.builder().build(), 2D));

        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMaxULN(builder.value(70D).build(), 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMaxULN(builder.value(120D).build(), 2D));
    }

    @Test
    public void canEvaluateVersusMaxValue() {
        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMaxValue(1D, Strings.EMPTY, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMaxValue(4D, Strings.EMPTY, 2D));

        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMaxValue(1D, LabValueEvaluation.SMALLER_THAN, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMaxValue(4D, LabValueEvaluation.LARGER_THAN, 2D));

        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMaxValue(4D, LabValueEvaluation.SMALLER_THAN, 2D));
        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMaxValue(1D, LabValueEvaluation.LARGER_THAN, 2D));
    }
}