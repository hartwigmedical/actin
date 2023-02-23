package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import org.junit.Test;

public class HasBoneMetastasesTest {

    private final HasBoneMetastases function = new HasBoneMetastases();

    @Test
    public void shouldBeUndeterminedWhenHasBoneLesionsIsNull() {
        Evaluation undetermined = function.evaluate(TumorTestFactory.withBoneLesions(null));
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined);
    }

    @Test
    public void shouldPassWhenHasBoneLesionsIsTrue() {
        Evaluation pass = function.evaluate(TumorTestFactory.withBoneLesions(true));
        assertEvaluation(EvaluationResult.PASS, pass);
    }

    @Test
    public void shouldFailWhenHasBoneLesionsIsFalse() {
        Evaluation fail = function.evaluate(TumorTestFactory.withBoneLesions(false));
        assertEvaluation(EvaluationResult.FAIL, fail);
    }
}