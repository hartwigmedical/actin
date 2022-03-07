package com.hartwig.actin.algo.evaluation.infection;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasKnownHTLVInfectionTest {

    @Test
    public void canEvaluate() {
        HasKnownHTLVInfection function = new HasKnownHTLVInfection();

        // Test without conditions
        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));

        // Add with a wrong condition
        conditions.add(InfectionTestFactory.builder().name("this is nothing serious").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));

        // Test with htlv
        String realHTLV = HasKnownHTLVInfection.HTLV_TERMS.iterator().next();
        conditions.add(InfectionTestFactory.builder().name("This is real " + realHTLV).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));
    }
}