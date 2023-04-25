package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasHadPriorConditionWithNameTest {

    @Test
    public void canEvaluate() {
        String nameToFind = "severe condition";
        HasHadPriorConditionWithName function = new HasHadPriorConditionWithName(nameToFind);

        // Test empty doid
        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with wrong name
        conditions.add(OtherConditionTestFactory.builder().name("benign condition").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with right name
        conditions.add(OtherConditionTestFactory.builder().name("very severe condition").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));
    }
}