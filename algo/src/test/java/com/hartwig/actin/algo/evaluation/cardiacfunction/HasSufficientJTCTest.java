package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ImmutableECGMeasure;

import org.junit.Before;
import org.junit.Test;

public class HasSufficientJTCTest {

    private EvaluationFunction victim;

    @Before
    public void setUp() {
        victim = ECGMeasureEvaluationFunction.hasSufficientJTc(450);
    }

    @Test
    public void evaluatesToUndeterminedWhenNoECGPresent() {
        assertEvaluation(EvaluationResult.UNDETERMINED, victim.evaluate(CardiacFunctionTestFactory.withECG(null)));
    }

    @Test
    public void evaluatesToUndeterminedWhenWrongUnit() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
                victim.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .jtcMeasure(ImmutableECGMeasure.builder().value(500).unit("wrong unit").build())
                        .build())));
    }

    @Test
    public void evaluatesToPassWhenAboveThreshold() {
        assertEvaluation(EvaluationResult.PASS,
                victim.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .jtcMeasure(ImmutableECGMeasure.builder().value(500).unit(ECGUnit.MILLISECONDS.getSymbol()).build())
                        .build())));
    }

    @Test
    public void evaluatesToPassWhenEqualThreshold() {
        assertEvaluation(EvaluationResult.PASS,
                victim.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .jtcMeasure(ImmutableECGMeasure.builder().value(450).unit(ECGUnit.MILLISECONDS.getSymbol()).build())
                        .build())));
    }

    @Test
    public void evaluatesToFailWhenBelowThreshold() {
        assertEvaluation(EvaluationResult.FAIL,
                victim.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .jtcMeasure(ImmutableECGMeasure.builder().value(300).unit(ECGUnit.MILLISECONDS.getSymbol()).build())
                        .build())));
    }
}
