package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class GeneHasExactExpressionByIHCTest {

    @Test
    public void canEvaluate() {
        String gene = "gene 1";
        GeneHasExactExpressionByIHC exact = new GeneHasExactExpressionByIHC(gene, 2);

        // No prior tests
        List<PriorMolecularTest> priorTests = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with no result
        priorTests.add(ihcBuilder(gene).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with too low result
        priorTests.add(ihcBuilder(gene).scoreValue(1D).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with too high result
        priorTests.add(ihcBuilder(gene).scoreValue(3D).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with exact result but with prefix
        priorTests.add(ihcBuilder(gene).scoreValuePrefix(">").scoreValue(2D).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with 'positive' result
        priorTests.add(ihcBuilder(gene).scoreText("Positive").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with exact result
        priorTests.add(ihcBuilder(gene).scoreValue(2D).build());
        assertEvaluation(EvaluationResult.PASS, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));
    }

    @NotNull
    private static ImmutablePriorMolecularTest.Builder ihcBuilder(@NotNull String gene) {
        return MolecularTestFactory.priorBuilder().test("IHC").item(gene);
    }
}