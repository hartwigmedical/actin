package com.hartwig.actin.algo.evaluation.infection;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class CanAdhereToAttenuatedVaccineUseTest {

    @Test
    public void canEvaluate() {
        CanAdhereToAttenuatedVaccineUse function = new CanAdhereToAttenuatedVaccineUse();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}