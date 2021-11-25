package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class OtherConditionEvaluationTest {

    @Test
    public void canDetectPresenceOfDoid() {
        String doidToFind = "parent";
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(doidToFind, "child");

        List<PriorOtherCondition> priorOtherConditions = Lists.newArrayList();
        // Test empty doid
        assertFalse(OtherConditionEvaluation.hasDoid(doidModel, priorOtherConditions, doidToFind));

        // Add a condition with no DOIDs
        priorOtherConditions.add(OtherConditionTestUtil.builder().build());
        assertFalse(OtherConditionEvaluation.hasDoid(doidModel, priorOtherConditions, doidToFind));

        // Add a condition with not the correct DOID
        priorOtherConditions.add(OtherConditionTestUtil.builder().addDoids("not the correct doid").build());
        assertFalse(OtherConditionEvaluation.hasDoid(doidModel, priorOtherConditions, doidToFind));

        // Add a condition with child DOID
        priorOtherConditions.add(OtherConditionTestUtil.builder().addDoids("child", "some other doid").build());
        assertTrue(OtherConditionEvaluation.hasDoid(doidModel, priorOtherConditions, doidToFind));

        // Also pass on the exact DOID
        PriorOtherCondition exact = OtherConditionTestUtil.builder().addDoids(doidToFind).build();
        assertTrue(OtherConditionEvaluation.hasDoid(doidModel, Lists.newArrayList(exact), doidToFind));
    }
}