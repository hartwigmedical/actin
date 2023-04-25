package com.hartwig.actin.algo.evaluation.reproduction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.junit.Test;

public class IsPregnantTest {

    @Test
    public void canEvaluate() {
        IsPregnant function = new IsPregnant();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(ReproductionTestFactory.withGender(Gender.FEMALE)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ReproductionTestFactory.withGender(Gender.MALE)));
    }
}