package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsWildtypeTest {

    @Test
    public void canEvaluate() {
        GeneIsWildtype function = new GeneIsWildtype("geneA");

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withWildtypeGene("geneB")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withWildtypeGene("geneA")));
    }
}