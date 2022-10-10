package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class GeneIsExpressedByIHCTest {

    @Test
    public void canEvaluate() {
        String gene = "gene 1";
        GeneIsExpressedByIHC function = new GeneIsExpressedByIHC(gene);

        // No prior tests
        List<PriorMolecularTest> priorTests = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with no result
        priorTests.add(ihcBuilder(gene).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with negative result
        priorTests.add(ihcBuilder(gene).scoreText("negative").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with positive result
        priorTests.add(ihcBuilder(gene).scoreValue(2D).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Also works for score texts.
        List<PriorMolecularTest> otherPriorTests = Lists.newArrayList(ihcBuilder(gene).scoreText("positive").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorMolecularTests(otherPriorTests)));
    }

    @NotNull
    private static ImmutablePriorMolecularTest.Builder ihcBuilder(@NotNull String gene) {
        return MolecularTestFactory.priorBuilder().test("IHC").item(gene);
    }
}