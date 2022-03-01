package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

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
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(priorOtherConditions)));

        // Add a condition with no DOIDs
        priorOtherConditions.add(OtherConditionTestFactory.builder().build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(priorOtherConditions)));

        // Add a condition with not the correct DOID
        priorOtherConditions.add(OtherConditionTestFactory.builder().addDoids("not the correct doid").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(priorOtherConditions)));

        // Add a condition with child DOID
        priorOtherConditions.add(OtherConditionTestFactory.builder().addDoids("child", "some other doid").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(priorOtherConditions)));

        // Also pass on the exact DOID
        PriorOtherCondition exact = OtherConditionTestFactory.builder().addDoids(doidToFind).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(exact)));
    }
}