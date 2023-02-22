package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import org.junit.Test;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;
import static org.junit.Assert.assertTrue;

public class TumorMetastasisEvaluatorTest {
    private static final String metastasisType = "bone";

    @Test
    public void shouldBeUndeterminedWhenBooleanIsNull() {
        Evaluation undetermined = TumorMetastasisEvaluator.evaluate(null, metastasisType);
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined);
        assertTrue(undetermined.undeterminedSpecificMessages().contains(
                "Data regarding presence of bone metastases is missing"));
        assertTrue(undetermined.undeterminedGeneralMessages().contains("Missing bone metastasis data"));
    }

    @Test
    public void shouldPassWhenBooleanIsTrue() {
        Evaluation pass = TumorMetastasisEvaluator.evaluate(Boolean.TRUE, metastasisType);
        assertEvaluation(EvaluationResult.PASS, pass);
        assertTrue(pass.passSpecificMessages().contains("Bone metastases are present"));
        assertTrue(pass.passGeneralMessages().contains("Bone metastases"));
    }

    @Test
    public void shouldFailWhenBooleanIsFalse() {
        Evaluation fail = TumorMetastasisEvaluator.evaluate(Boolean.FALSE, metastasisType);
        assertEvaluation(EvaluationResult.FAIL, fail);
        assertTrue(fail.failSpecificMessages().contains("No bone metastases present"));
        assertTrue(fail.failGeneralMessages().contains("No bone metastases"));
    }
}