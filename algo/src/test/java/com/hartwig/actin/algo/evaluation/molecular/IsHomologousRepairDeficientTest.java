package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class IsHomologousRepairDeficientTest {

    @Test
    public void canEvaluate() {
        IsHomologousRepairDeficient function = new IsHomologousRepairDeficient();

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(MolecularTestFactory.withHomologousRepairDeficiency(null)));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withHomologousRepairDeficiency(true)));
        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withHomologousRepairDeficiency(false)));
    }
}