package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableECGMeasure;

import org.junit.Before;
import org.junit.Test;

public class HasLimitedQTCFTest {

    private HasLimitedQTCF victim;

    @Before
    public void setUp() {
        victim = new HasLimitedQTCF(450D);
    }

    @Test
    public void evaluatesToUndeterminedWhenNoEcgPresent() {
        assertEvaluation(EvaluationResult.UNDETERMINED, victim.evaluate(CardiacFunctionTestFactory.withECG(null)));
    }

    @Test
    public void evaluatesToUndeterminedWhenWrongUnit() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
                victim.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(400).unit("wrong unit").build())
                        .build())));
    }

    @Test
    public void evaluatesToPassWhenBelowThreshold() {
        assertEvaluation(EvaluationResult.PASS,
                victim.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(300).unit(ECGUnits.MILLISECONDS).build())
                        .build())));
    }

    @Test
    public void evaluatesToPassWhenEqualThreshold() {
        assertEvaluation(EvaluationResult.PASS,
                victim.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(450).unit(ECGUnits.MILLISECONDS).build())
                        .build())));
    }

    @Test
    public void evaluatesToFailWhenAboveThreshold() {
        assertEvaluation(EvaluationResult.FAIL,
                victim.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(500).unit(ECGUnits.MILLISECONDS).build())
                        .build())));
    }
}