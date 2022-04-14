package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class IsHomologousRepairDeficientTest {

    @Test
    public void canEvaluate() {
        IsHomologousRepairDeficient function = new IsHomologousRepairDeficient();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withHomologousRepairDeficiency(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHomologousRepairDeficiency(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withHomologousRepairDeficiency(false)));
    }
}