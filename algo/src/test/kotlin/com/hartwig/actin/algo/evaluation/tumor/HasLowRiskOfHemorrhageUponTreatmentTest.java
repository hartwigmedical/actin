package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLowRiskOfHemorrhageUponTreatmentTest {

    @Test
    public void canEvaluate() {
        HasLowRiskOfHemorrhageUponTreatment function = new HasLowRiskOfHemorrhageUponTreatment();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}