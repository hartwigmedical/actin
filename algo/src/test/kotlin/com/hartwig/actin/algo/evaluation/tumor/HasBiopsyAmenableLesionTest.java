package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.ExperimentType;

import org.junit.Test;

public class HasBiopsyAmenableLesionTest {

    @Test
    public void canEvaluate() {
        HasBiopsyAmenableLesion function = new HasBiopsyAmenableLesion();

        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withMolecularExperimentType(ExperimentType.PANEL)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withMolecularExperimentType(ExperimentType.WGS)));
    }
}