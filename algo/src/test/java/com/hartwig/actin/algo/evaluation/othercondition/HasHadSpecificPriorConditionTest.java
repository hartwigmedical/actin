package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasHadSpecificPriorConditionTest {

    @Test
    public void canEvaluate() {
        String doidToFind = "parent";
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(doidToFind, "child");
        HasHadSpecificPriorCondition function = new HasHadSpecificPriorCondition(doidModel, doidToFind);

        // Test empty doid
        List<PriorOtherCondition> priorOtherConditions = Lists.newArrayList();
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)).result());

        // Add a condition with no DOIDs
        priorOtherConditions.add(OtherConditionTestUtil.builder().build());
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)).result());

        // Add a condition with not the correct DOID
        priorOtherConditions.add(OtherConditionTestUtil.builder().addDoids("not the correct doid").build());
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)).result());

        // Add a condition with child DOID
        priorOtherConditions.add(OtherConditionTestUtil.builder().addDoids("child", "some other doid").build());
        assertEquals(EvaluationResult.PASS,
                function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)).result());

        // Also pass on the exact DOID
        PriorOtherCondition exact = OtherConditionTestUtil.builder().addDoids(doidToFind).build();
        assertEquals(EvaluationResult.PASS, function.evaluate(OtherConditionTestUtil.withPriorOtherCondition(exact)).result());
    }
}