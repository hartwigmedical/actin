package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsWildTypeTest {

    @Test
    public void canEvaluate() {
        GeneIsWildType function = new GeneIsWildType("gene A");

        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withWildTypeGene("gene A")));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withWildTypeGene("gene B")));

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withUndeterminedWildTypes()));
    }
}