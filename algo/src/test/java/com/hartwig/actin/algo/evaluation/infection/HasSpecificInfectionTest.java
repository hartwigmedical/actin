package com.hartwig.actin.algo.evaluation.infection;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasSpecificInfectionTest {

    @Test
    public void canEvaluate() {
        String doidToFind = "parent";
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(doidToFind, "child");
        HasSpecificInfection function = new HasSpecificInfection(doidModel, doidToFind);

        // Test empty doid
        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with no DOIDs
        conditions.add(InfectionTestFactory.builder().build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with not the correct DOID
        conditions.add(InfectionTestFactory.builder().addDoids("not the correct doid").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with child DOID
        conditions.add(InfectionTestFactory.builder().addDoids("child", "some other doid").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));

        // Also pass on the exact DOID
        PriorOtherCondition exact = InfectionTestFactory.builder().addDoids(doidToFind).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherCondition(exact)));
    }
}