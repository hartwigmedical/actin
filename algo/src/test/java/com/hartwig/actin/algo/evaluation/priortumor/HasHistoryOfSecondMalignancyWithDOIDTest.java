package com.hartwig.actin.algo.evaluation.priortumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasHistoryOfSecondMalignancyWithDOIDTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");
        HasHistoryOfSecondMalignancyWithDOID function = new HasHistoryOfSecondMalignancyWithDOID(doidModel, "100");

        // No prior tumors.
        List<PriorSecondPrimary> priorTumors = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)));

        // Wrong doid
        priorTumors.add(PriorTumorTestFactory.builder().addDoids("300").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)));

        // Right doid
        priorTumors.add(PriorTumorTestFactory.builder().addDoids("200").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)));
    }
}