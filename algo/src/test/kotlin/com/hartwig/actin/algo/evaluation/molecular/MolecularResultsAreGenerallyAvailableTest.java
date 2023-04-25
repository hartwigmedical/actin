package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class MolecularResultsAreGenerallyAvailableTest {

    @Test
    public void canEvaluate() {
        MolecularResultsAreGenerallyAvailable function = new MolecularResultsAreGenerallyAvailable();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}