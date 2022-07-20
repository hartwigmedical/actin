package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSufficientQTCFTest {

    @Test
    public void canEvaluate() {
        HasSufficientQTCF function = new HasSufficientQTCF(450D);

        // No ECG info known
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(CardiacFunctionTestFactory.withECG(null)));

        // Wrong unit
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .qtcfValue(300)
                        .qtcfUnit("wrong unit")
                        .build())));

        // Above min threshold
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .qtcfValue(500)
                        .qtcfUnit(QTCFFunctions.EXPECTED_QTCF_UNIT)
                        .build())));

        // Below min threshold
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(CardiacFunctionTestFactory.withECG(CardiacFunctionTestFactory.builder()
                        .qtcfValue(300)
                        .qtcfUnit(QTCFFunctions.EXPECTED_QTCF_UNIT)
                        .build())));
    }
}
