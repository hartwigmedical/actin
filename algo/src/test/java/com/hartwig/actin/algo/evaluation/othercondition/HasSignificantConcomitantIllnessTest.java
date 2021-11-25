package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasSignificantConcomitantIllnessTest {

    @Test
    public void canEvaluate() {
        HasSignificantConcomitantIllness function = new HasSignificantConcomitantIllness();

        // Empty list
        List<PriorOtherCondition> priorOtherConditions = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)));

        // Add any random condition
        priorOtherConditions.add(OtherConditionTestUtil.builder().build());
        assertEquals(Evaluation.PASS, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)));
    }
}