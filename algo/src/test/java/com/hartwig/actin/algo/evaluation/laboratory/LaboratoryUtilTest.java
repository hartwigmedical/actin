package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class LaboratoryUtilTest {

    @Test
    public void canDetermineWhetherLabValueIsAsExpected() {
        assertFalse(LaboratoryUtil.existsWithExpectedUnit(null, "expected unit"));

        LabValue value = LabTestFactory.builder().unit("expected unit").build();
        assertFalse(LaboratoryUtil.existsWithExpectedUnit(value, "not expected unit"));
        assertTrue(LaboratoryUtil.existsWithExpectedUnit(value, "expected unit"));
    }

    @Test
    public void canEvaluateVersusMinULN() {
        ImmutableLabValue.Builder builder = LabTestFactory.builder().refLimitLow(30D);

        assertEquals(Evaluation.UNDETERMINED, LaboratoryUtil.evaluateVersusMinULN(LabTestFactory.builder().build(), 2D));

        assertEquals(Evaluation.PASS, LaboratoryUtil.evaluateVersusMinULN(builder.value(80D).build(), 2D));
        assertEquals(Evaluation.FAIL, LaboratoryUtil.evaluateVersusMinULN(builder.value(50D).build(), 2D));
    }

    @Test
    public void canEvaluateVersusMinValue() {
        assertEquals(Evaluation.PASS, LaboratoryUtil.evaluateVersusMinValue(4D, Strings.EMPTY, 2D));
        assertEquals(Evaluation.FAIL, LaboratoryUtil.evaluateVersusMinValue(1D, Strings.EMPTY, 2D));

        assertEquals(Evaluation.PASS, LaboratoryUtil.evaluateVersusMinValue(4D, LaboratoryUtil.LARGER_THAN, 2D));
        assertEquals(Evaluation.FAIL, LaboratoryUtil.evaluateVersusMinValue(1D, LaboratoryUtil.SMALLER_THAN, 2D));

        assertEquals(Evaluation.UNDETERMINED, LaboratoryUtil.evaluateVersusMinValue(4D, LaboratoryUtil.SMALLER_THAN, 2D));
        assertEquals(Evaluation.UNDETERMINED, LaboratoryUtil.evaluateVersusMinValue(1D, LaboratoryUtil.LARGER_THAN, 2D));
    }

    @Test
    public void canEvaluateVersusMaxULN() {
        ImmutableLabValue.Builder builder = LabTestFactory.builder().refLimitUp(50D);

        assertEquals(Evaluation.UNDETERMINED, LaboratoryUtil.evaluateVersusMaxULN(LabTestFactory.builder().build(), 2D));

        assertEquals(Evaluation.PASS, LaboratoryUtil.evaluateVersusMaxULN(builder.value(70D).build(), 2D));
        assertEquals(Evaluation.FAIL, LaboratoryUtil.evaluateVersusMaxULN(builder.value(120D).build(), 2D));
    }

    @Test
    public void canUseOverridesForRefLimitUp() {
        String firstCode = LaboratoryUtil.REF_LIMIT_UP_OVERRIDES.keySet().iterator().next();
        double overrideRefLimitUp = LaboratoryUtil.REF_LIMIT_UP_OVERRIDES.get(firstCode);

        LabValue value = LabTestFactory.builder().code(firstCode).value(1.8 * overrideRefLimitUp).build();
        assertEquals(Evaluation.PASS, LaboratoryUtil.evaluateVersusMaxULN(value, 2D));
    }

    @Test
    public void canEvaluateVersusMaxValue() {
        assertEquals(Evaluation.PASS, LaboratoryUtil.evaluateVersusMaxValue(1D, Strings.EMPTY, 2D));
        assertEquals(Evaluation.FAIL, LaboratoryUtil.evaluateVersusMaxValue(4D, Strings.EMPTY, 2D));

        assertEquals(Evaluation.PASS, LaboratoryUtil.evaluateVersusMaxValue(1D, LaboratoryUtil.SMALLER_THAN, 2D));
        assertEquals(Evaluation.FAIL, LaboratoryUtil.evaluateVersusMaxValue(4D, LaboratoryUtil.LARGER_THAN, 2D));

        assertEquals(Evaluation.UNDETERMINED, LaboratoryUtil.evaluateVersusMaxValue(4D, LaboratoryUtil.SMALLER_THAN, 2D));
        assertEquals(Evaluation.UNDETERMINED, LaboratoryUtil.evaluateVersusMaxValue(1D, LaboratoryUtil.LARGER_THAN, 2D));
    }
}