package com.hartwig.actin.algo.evaluation.infection;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasKnownEBVInfectionTest {

    @Test
    public void canEvaluate() {
        HasKnownEBVInfection function = new HasKnownEBVInfection();

        // Test without conditions
        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));

        // Add with a wrong condition
        conditions.add(InfectionTestFactory.builder().name("this is nothing serious").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));

        // Test with ebv
        String realEBV = HasKnownEBVInfection.EBV_TERMS.iterator().next();
        conditions.add(InfectionTestFactory.builder().name("This is real " + realEBV).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)));
    }
}