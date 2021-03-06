package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.molecular.ManufacturedTCellsWithinShelfLife;

import org.junit.Test;

public class ManufacturedTCellsWithinShelfLifeTest {

    @Test
    public void canEvaluate() {
        ManufacturedTCellsWithinShelfLife function = new ManufacturedTCellsWithinShelfLife();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}