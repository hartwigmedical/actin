package com.hartwig.actin.algo.evaluation;

import static org.junit.Assert.assertTrue;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.junit.Test;

public class EvaluationFunctionFactoryTest {

    @Test
    public void hasCreatorForEveryEligibilityRule() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            assertTrue(EvaluationFunctionFactory.FUNCTION_CREATOR_MAP.containsKey(rule));
        }
    }
}