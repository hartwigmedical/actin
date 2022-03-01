package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasHadOrganTransplantTest {

    @Test
    public void canEvaluate() {
        HasHadOrganTransplant function = new HasHadOrganTransplant();

        List<PriorOtherCondition> priorOtherConditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(priorOtherConditions)));

        priorOtherConditions.add(OtherConditionTestFactory.builder().build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(priorOtherConditions)));

        priorOtherConditions.add(OtherConditionTestFactory.builder().category(HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(priorOtherConditions)));
    }
}