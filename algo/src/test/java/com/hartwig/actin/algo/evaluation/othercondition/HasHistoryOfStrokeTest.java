package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasHistoryOfStrokeTest {

    @Test
    public void canEvaluate() {
        DoidEvaluator doidEvaluator = OtherConditionTestUtil.createTestDoidEvaluator();
        HasHistoryOfStroke function = new HasHistoryOfStroke(doidEvaluator);

        assertEquals(Evaluation.FAIL, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(Lists.newArrayList())));

        PriorOtherCondition match = OtherConditionTestUtil.builder().addDoids(HasHistoryOfStroke.STROKE_DOID).build();
        assertEquals(Evaluation.PASS, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(Lists.newArrayList(match))));
    }
}