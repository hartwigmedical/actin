package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasMeasurableDiseaseRecistTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");
        HasMeasurableDiseaseRecist function = new HasMeasurableDiseaseRecist(doidModel);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withMeasurableDisease(null)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withMeasurableDisease(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withMeasurableDisease(false)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withMeasurableDiseaseAndDoid(true, "random")));
        assertEvaluation(EvaluationResult.PASS_BUT_WARN,
                function.evaluate(TumorTestFactory.withMeasurableDiseaseAndDoid(true,
                        HasMeasurableDiseaseRecist.NON_RECIST_TUMOR_DOIDS.iterator().next())));
    }

}