package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class HasGilbertDiseaseTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        HasGilbertDisease function = new HasGilbertDisease(doidModel);

        assertEquals(Evaluation.FAIL, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(Lists.newArrayList())));

        PriorOtherCondition match = OtherConditionTestUtil.builder().addDoids(HasGilbertDisease.GILBERT_DISEASE_DOID).build();
        assertEquals(Evaluation.PASS, function.evaluate(OtherConditionTestUtil.withPriorOtherConditions(Lists.newArrayList(match))));
    }
}