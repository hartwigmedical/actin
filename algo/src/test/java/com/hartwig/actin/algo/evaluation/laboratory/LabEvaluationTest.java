package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.junit.Test;

public class LabEvaluationTest {

    @Test
    public void canEvaluateVersusMinLLN() {
        ImmutableLabValue.Builder builder = LabTestFactory.builder().refLimitLow(30D);

        assertEquals(EvaluationResult.UNDETERMINED, LabEvaluation.evaluateVersusMinLLN(LabTestFactory.builder().build(), 2D));

        assertEquals(EvaluationResult.PASS, LabEvaluation.evaluateVersusMinLLN(builder.value(80D).build(), 2D));
        assertEquals(EvaluationResult.FAIL, LabEvaluation.evaluateVersusMinLLN(builder.value(50D).build(), 2D));
    }

    @Test
    public void canEvaluateVersusMinULN() {
        ImmutableLabValue.Builder builder = LabTestFactory.builder().refLimitUp(50D);

        assertEquals(EvaluationResult.UNDETERMINED, LabEvaluation.evaluateVersusMinULN(LabTestFactory.builder().build(), 2D));

        assertEquals(EvaluationResult.PASS, LabEvaluation.evaluateVersusMinULN(builder.value(40D).build(), 0.5));
        assertEquals(EvaluationResult.FAIL, LabEvaluation.evaluateVersusMinULN(builder.value(20D).build(), 0.5));
    }

    @Test
    public void canEvaluateVersusMaxULN() {
        ImmutableLabValue.Builder builder = LabTestFactory.builder().refLimitUp(50D);

        assertEquals(EvaluationResult.UNDETERMINED, LabEvaluation.evaluateVersusMaxULN(LabTestFactory.builder().build(), 2D));

        assertEquals(EvaluationResult.PASS, LabEvaluation.evaluateVersusMaxULN(builder.value(70D).build(), 2D));
        assertEquals(EvaluationResult.FAIL, LabEvaluation.evaluateVersusMaxULN(builder.value(120D).build(), 2D));
    }

    @Test
    public void canUseOverridesForRefLimitUp() {
        String firstCode = LabEvaluation.REF_LIMIT_UP_OVERRIDES.keySet().iterator().next();
        double overrideRefLimitUp = LabEvaluation.REF_LIMIT_UP_OVERRIDES.get(firstCode);

        LabValue value = LabTestFactory.builder().code(firstCode).value(1.8 * overrideRefLimitUp).build();
        assertEquals(EvaluationResult.PASS, LabEvaluation.evaluateVersusMaxULN(value, 2D));
    }
}