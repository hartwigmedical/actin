package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasHadPriorConditionWithDoidTest {

    @Test
    public void canEvaluate() {
        String doidToFind = "parent";
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(doidToFind, "child");
        HasHadPriorConditionWithDoid function = new HasHadPriorConditionWithDoid(doidModel, doidToFind);

        // Test empty doid
        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with no DOIDs
        conditions.add(OtherConditionTestFactory.builder().build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with not the correct DOID
        conditions.add(OtherConditionTestFactory.builder().addDoids("not the correct doid").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with child DOID
        conditions.add(OtherConditionTestFactory.builder().addDoids("child", "some other doid").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Also pass on the exact DOID
        PriorOtherCondition exact = OtherConditionTestFactory.builder().addDoids(doidToFind).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(exact)));
    }
}