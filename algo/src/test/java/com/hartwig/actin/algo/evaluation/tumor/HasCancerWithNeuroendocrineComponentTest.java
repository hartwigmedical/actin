package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasCancerWithNeuroendocrineComponentTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm("matching doid",
                HasCancerWithNeuroendocrineComponent.NEUROENDOCRINE_TERMS.iterator().next());
        HasCancerWithNeuroendocrineComponent function = new HasCancerWithNeuroendocrineComponent(doidModel);

        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().build())));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().addDoids("wrong").build())));

        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .addDoids(HasCancerWithSmallCellComponent.SMALL_CELL_DOIDS.iterator().next())
                        .build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().addDoids("matching doid").build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .addDoids(HasCancerWithNeuroendocrineComponent.NEUROENDOCRINE_DOIDS.iterator().next())
                        .build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .primaryTumorExtraDetails(
                                HasCancerWithNeuroendocrineComponent.NEUROENDOCRINE_EXTRA_DETAILS.iterator().next() + " tumor")
                        .build())));
    }
}