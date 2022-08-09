package com.hartwig.actin.algo.evaluation.reproduction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.junit.Test;

public class IsWomanOfChildBearingPotentialTest {

    @Test
    public void canEvaluate() {
        IsWomanOfChildBearingPotential function = new IsWomanOfChildBearingPotential();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ReproductionTestFactory.withGender(Gender.FEMALE)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ReproductionTestFactory.withGender(Gender.MALE)));
    }
}