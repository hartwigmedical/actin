package com.hartwig.actin.algo.evaluation.medication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class CurrentlyGetsDiseaseModifyingAgentsTest {

    @Test
    public void canEvaluate() {
        CurrentlyGetsDiseaseModifyingAgents function = new CurrentlyGetsDiseaseModifyingAgents();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}