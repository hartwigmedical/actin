package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasHistoryOfAnaphylaxisTest {

    @Test
    public void canEvaluate() {
        HasHistoryOfAnaphylaxis function = new HasHistoryOfAnaphylaxis();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withAllergies(Lists.newArrayList())));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(ToxicityTestFactory.withAllergy(ToxicityTestFactory.allergy().build())));
    }
}