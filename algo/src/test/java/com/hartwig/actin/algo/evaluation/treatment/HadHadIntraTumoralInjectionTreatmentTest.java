package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HadHadIntraTumoralInjectionTreatmentTest {

    @Test
    public void canEvaluate() {
        HadHadIntraTumoralInjectionTreatment function = new HadHadIntraTumoralInjectionTreatment();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}