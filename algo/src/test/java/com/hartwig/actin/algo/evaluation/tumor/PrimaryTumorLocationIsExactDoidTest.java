package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;

import org.junit.Test;

public class PrimaryTumorLocationIsExactDoidTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationIsExactDoid function = new PrimaryTumorLocationIsExactDoid(doidModel, "100");

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids("100")));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("200")));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("50", "250")));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids((List<String>) null)));
    }
}