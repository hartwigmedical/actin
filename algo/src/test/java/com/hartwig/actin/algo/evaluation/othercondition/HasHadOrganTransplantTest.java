package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasHadOrganTransplantTest {

    @Test
    public void canEvaluate() {
        HasHadOrganTransplant function = new HasHadOrganTransplant(null);

        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        conditions.add(OtherConditionTestFactory.builder().build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        conditions.add(OtherConditionTestFactory.builder().category(HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));
    }

    @Test
    public void canEvaluateWithMinYear() {
        HasHadOrganTransplant function = new HasHadOrganTransplant(2021);

        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        ImmutablePriorOtherCondition.Builder builder =
                OtherConditionTestFactory.builder().category(HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY);

        // Too long ago.
        conditions.add(builder.year(2020).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Unclear date
        conditions.add(builder.year(null).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Exact match
        conditions.add(builder.year(2021).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));
    }
}