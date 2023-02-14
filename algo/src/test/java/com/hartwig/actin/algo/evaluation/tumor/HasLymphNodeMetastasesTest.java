package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import org.junit.Test;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;
import static org.junit.Assert.assertTrue;

public class HasLymphNodeMetastasesTest {

    @Test
    public void canEvaluate() {
        HasLymphNodeMetastases function = new HasLymphNodeMetastases();

        Evaluation undetermined = function.evaluate(TumorTestFactory.withLymphNodeLesions(null));
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined);
        assertTrue(undetermined.undeterminedSpecificMessages().contains(
                "Data regarding presence of lymph node metastases is missing"));
        assertTrue(undetermined.undeterminedGeneralMessages().contains("Missing lymph node metastasis data"));

        Evaluation pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(true));
        assertEvaluation(EvaluationResult.PASS, pass);
        assertTrue(pass.passSpecificMessages().contains("Lymph node metastases are present"));
        assertTrue(pass.passGeneralMessages().contains("Lymph node metastases"));

        Evaluation fail = function.evaluate(TumorTestFactory.withLymphNodeLesions(false));
        assertEvaluation(EvaluationResult.FAIL, fail);
        assertTrue(fail.failSpecificMessages().contains("No lymph node metastases present"));
        assertTrue(fail.failGeneralMessages().contains("No lymph node metastases"));
    }
}