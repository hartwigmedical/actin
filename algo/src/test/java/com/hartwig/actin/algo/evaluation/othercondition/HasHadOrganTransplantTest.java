package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasHadOrganTransplantTest {

    @Test
    public void canEvaluate() {
        HasHadOrganTransplant function = new HasHadOrganTransplant();

        List<PriorOtherCondition> priorOtherConditions = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)));

        priorOtherConditions.add(OtherConditionTestUtil.builder().build());
        assertEquals(Evaluation.FAIL, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)));

        priorOtherConditions.add(OtherConditionTestUtil.builder().category(HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY).build());
        assertEquals(Evaluation.PASS, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(priorOtherConditions)));
    }
}