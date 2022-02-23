package com.hartwig.actin.algo.evaluation.general;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class PatientWillBeParticipatingInCountryTest {

    @Test
    public void canEvaluate() {
        PatientWillBeParticipatingInCountry netherlands = new PatientWillBeParticipatingInCountry("The Netherlands");
        assertEvaluation(EvaluationResult.PASS, netherlands.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        PatientWillBeParticipatingInCountry germany = new PatientWillBeParticipatingInCountry("Germany");
        assertEvaluation(EvaluationResult.FAIL, germany.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}