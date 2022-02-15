package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class IsHomologousRepairDeficientTest {

    @Test
    public void canEvaluate() {
        IsHomologousRepairDeficient function = new IsHomologousRepairDeficient();

        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withHomologousRepairDeficiency(null)));
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHomologousRepairDeficiency(true)));
        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withHomologousRepairDeficiency(false)));
    }
}