package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HadHadIntratumoralInjectionTreatmentTest2 {

    @Test
    public void canEvaluate() {
        HadHadIntratumoralInjectionTreatment2 function = new HadHadIntratumoralInjectionTreatment2();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}