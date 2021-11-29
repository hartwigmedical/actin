package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class DoidEvaluatorTest {

    @Test
    public void canDetectPresenceOfDoid() {
        String doidToFind = "parent";
        DoidEvaluator doidEvaluator = new DoidEvaluator(TestDoidModelFactory.createWithOneParentChild(doidToFind, "child"));

        List<PriorOtherCondition> priorOtherConditions = Lists.newArrayList();
        // Test empty doid
        assertFalse(doidEvaluator.hasDoid(priorOtherConditions, doidToFind));

        // Add a condition with no DOIDs
        priorOtherConditions.add(OtherConditionTestUtil.builder().build());
        assertFalse(doidEvaluator.hasDoid(priorOtherConditions, doidToFind));

        // Add a condition with not the correct DOID
        priorOtherConditions.add(OtherConditionTestUtil.builder().addDoids("not the correct doid").build());
        assertFalse(doidEvaluator.hasDoid(priorOtherConditions, doidToFind));

        // Add a condition with child DOID
        priorOtherConditions.add(OtherConditionTestUtil.builder().addDoids("child", "some other doid").build());
        assertTrue(doidEvaluator.hasDoid(priorOtherConditions, doidToFind));

        // Also pass on the exact DOID
        PriorOtherCondition exact = OtherConditionTestUtil.builder().addDoids(doidToFind).build();
        assertTrue(doidEvaluator.hasDoid(Lists.newArrayList(exact), doidToFind));
    }
}