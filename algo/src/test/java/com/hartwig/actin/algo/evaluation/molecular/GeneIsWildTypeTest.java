package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsWildTypeTest {

    @Test
    public void canEvaluate() {
        GeneIsWildType function = new GeneIsWildType("geneA");

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withWildTypeGene("geneB")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withWildTypeGene("geneA")));
    }
}