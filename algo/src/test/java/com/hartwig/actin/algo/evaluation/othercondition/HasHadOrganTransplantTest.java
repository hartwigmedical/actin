package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertEquals;

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
        assertEquals(EvaluationResult.FAIL, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)));

        priorOtherConditions.add(OtherConditionTestUtil.builder().build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)));

        priorOtherConditions.add(OtherConditionTestUtil.builder().category(HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY).build());
        assertEquals(EvaluationResult.PASS, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)));
    }
}