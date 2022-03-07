package com.hartwig.actin.algo.evaluation.infection;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasCovid19InfectionTest {

    @Test
    public void canEvaluate() {
        HasCovid19Infection function = new HasCovid19Infection();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}