package com.hartwig.actin.algo.evaluation.general;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.junit.Test;

public class IsFemaleTest {

    @Test
    public void canEvaluate() {
        IsFemale function = new IsFemale();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withGender(Gender.MALE)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withGender(Gender.FEMALE)));
    }
}