package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class LaboratoryUtilTest {

    @Test
    public void canEvaluateVersusMinULN() {
        ImmutableLabValue.Builder builder = LabTestFactory.builder().refLimitLow(30D);

        assertEquals(EvaluationResult.UNDETERMINED, LaboratoryUtil.evaluateVersusMinULN(LabTestFactory.builder().build(), 2D).result());

        assertEquals(EvaluationResult.PASS, LaboratoryUtil.evaluateVersusMinULN(builder.value(80D).build(), 2D).result());
        assertEquals(EvaluationResult.FAIL, LaboratoryUtil.evaluateVersusMinULN(builder.value(50D).build(), 2D).result());
    }

    @Test
    public void canEvaluateVersusMinValue() {
        assertEquals(EvaluationResult.PASS, LaboratoryUtil.evaluateVersusMinValue(Strings.EMPTY, 4D, Strings.EMPTY, 2D).result());
        assertEquals(EvaluationResult.FAIL, LaboratoryUtil.evaluateVersusMinValue(Strings.EMPTY, 1D, Strings.EMPTY, 2D).result());

        assertEquals(EvaluationResult.PASS,
                LaboratoryUtil.evaluateVersusMinValue(Strings.EMPTY, 4D, LaboratoryUtil.LARGER_THAN, 2D).result());
        assertEquals(EvaluationResult.FAIL,
                LaboratoryUtil.evaluateVersusMinValue(Strings.EMPTY, 1D, LaboratoryUtil.SMALLER_THAN, 2D).result());

        assertEquals(EvaluationResult.UNDETERMINED,
                LaboratoryUtil.evaluateVersusMinValue(Strings.EMPTY, 4D, LaboratoryUtil.SMALLER_THAN, 2D).result());
        assertEquals(EvaluationResult.UNDETERMINED,
                LaboratoryUtil.evaluateVersusMinValue(Strings.EMPTY, 1D, LaboratoryUtil.LARGER_THAN, 2D).result());
    }

    @Test
    public void canEvaluateVersusMaxULN() {
        ImmutableLabValue.Builder builder = LabTestFactory.builder().refLimitUp(50D);

        assertEquals(EvaluationResult.UNDETERMINED, LaboratoryUtil.evaluateVersusMaxULN(LabTestFactory.builder().build(), 2D).result());

        assertEquals(EvaluationResult.PASS, LaboratoryUtil.evaluateVersusMaxULN(builder.value(70D).build(), 2D).result());
        assertEquals(EvaluationResult.FAIL, LaboratoryUtil.evaluateVersusMaxULN(builder.value(120D).build(), 2D).result());
    }

    @Test
    public void canUseOverridesForRefLimitUp() {
        String firstCode = LaboratoryUtil.REF_LIMIT_UP_OVERRIDES.keySet().iterator().next();
        double overrideRefLimitUp = LaboratoryUtil.REF_LIMIT_UP_OVERRIDES.get(firstCode);

        LabValue value = LabTestFactory.builder().code(firstCode).value(1.8 * overrideRefLimitUp).build();
        assertEquals(EvaluationResult.PASS, LaboratoryUtil.evaluateVersusMaxULN(value, 2D).result());
    }

    @Test
    public void canEvaluateVersusMaxValue() {
        assertEquals(EvaluationResult.PASS, LaboratoryUtil.evaluateVersusMaxValue(1D, Strings.EMPTY, 2D).result());
        assertEquals(EvaluationResult.FAIL, LaboratoryUtil.evaluateVersusMaxValue(4D, Strings.EMPTY, 2D).result());

        assertEquals(EvaluationResult.PASS, LaboratoryUtil.evaluateVersusMaxValue(1D, LaboratoryUtil.SMALLER_THAN, 2D).result());
        assertEquals(EvaluationResult.FAIL, LaboratoryUtil.evaluateVersusMaxValue(4D, LaboratoryUtil.LARGER_THAN, 2D).result());

        assertEquals(EvaluationResult.UNDETERMINED, LaboratoryUtil.evaluateVersusMaxValue(4D, LaboratoryUtil.SMALLER_THAN, 2D).result());
        assertEquals(EvaluationResult.UNDETERMINED, LaboratoryUtil.evaluateVersusMaxValue(1D, LaboratoryUtil.LARGER_THAN, 2D).result());
    }
}