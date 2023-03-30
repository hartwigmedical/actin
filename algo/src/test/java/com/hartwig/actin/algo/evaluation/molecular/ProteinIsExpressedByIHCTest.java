package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ProteinIsExpressedByIHCTest {

    @Test
    public void canEvaluate() {
        String protein = "protein 1";
        ProteinIsExpressedByIHC function = new ProteinIsExpressedByIHC(protein);

        // No prior tests
        List<PriorMolecularTest> priorTests = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with no result
        priorTests.add(ihcBuilder(protein).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with negative result
        priorTests.add(ihcBuilder(protein).scoreText("negative").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with positive result
        priorTests.add(ihcBuilder(protein).scoreValue(2D).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Also works for score texts.
        List<PriorMolecularTest> otherPriorTests = Lists.newArrayList(ihcBuilder(protein).scoreText("positive").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorTests(otherPriorTests)));
    }

    @NotNull
    private static ImmutablePriorMolecularTest.Builder ihcBuilder(@NotNull String protein) {
        return MolecularTestFactory.priorBuilder().test("IHC").item(protein);
    }
}