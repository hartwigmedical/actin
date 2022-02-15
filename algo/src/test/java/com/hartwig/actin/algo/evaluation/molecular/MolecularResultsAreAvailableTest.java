package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class MolecularResultsAreAvailableTest {

    @Test
    public void canEvaluate() {
        MolecularResultsAreAvailable function = new MolecularResultsAreAvailable();

        assertEquals(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}