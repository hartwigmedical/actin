package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class GeneHasSufficientExpressionByIHCTest {

    @Test
    public void canEvaluate() {
        String gene = "gene 1";
        GeneHasSufficientExpressionByIHC exact = new GeneHasSufficientExpressionByIHC(gene, 2);

        // No prior tests
        List<PriorMolecularTest> priorTests = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with no result
        priorTests.add(ihcBuilder(gene).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with too low result
        priorTests.add(ihcBuilder(gene).scoreValue(1D).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with unclear result
        priorTests.add(ihcBuilder(gene).scoreText("Negative").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with valid result
        priorTests.add(ihcBuilder(gene).scoreValue(3D).build());
        assertEvaluation(EvaluationResult.PASS, exact.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));
    }

    @NotNull
    private static ImmutablePriorMolecularTest.Builder ihcBuilder(@NotNull String gene) {
        return ImmutablePriorMolecularTest.builder().test("IHC").item(gene);
    }
}