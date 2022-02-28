package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class MolecularResultsAreAvailableTest {

    @Test
    public void canEvaluate() {
        MolecularResultsAreAvailable function = new MolecularResultsAreAvailable();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}