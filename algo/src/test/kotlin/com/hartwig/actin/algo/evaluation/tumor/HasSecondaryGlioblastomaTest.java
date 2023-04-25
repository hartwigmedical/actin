package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Set;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasSecondaryGlioblastomaTest {

    @Test
    public void canEvaluate() {
        HasSecondaryGlioblastoma function = new HasSecondaryGlioblastoma(TestDoidModelFactory.createMinimalTestDoidModel());

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((Set<String>) null)));
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoids(DoidConstants.GLIOBLASTOMA_DOID)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("arbitrary doid")));
    }
}