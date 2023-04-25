package com.hartwig.actin.algo.evaluation.infection;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class MeetsCovid19InfectionRequirementsTest {

    @Test
    public void canEvaluate() {
        MeetsCovid19InfectionRequirements function = new MeetsCovid19InfectionRequirements();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}