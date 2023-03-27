package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ProteinHasExactExpressionByIHCTest {

    @Test
    public void canEvaluate() {
        String protein = "protein 1";
        ProteinHasExactExpressionByIHC exact = new ProteinHasExactExpressionByIHC(protein, 2);

        // No prior tests
        List<PriorMolecularTest> priorTests = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with no result
        priorTests.add(ihcBuilder(protein).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with too low result
        priorTests.add(ihcBuilder(protein).scoreValue(1D).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with too high result
        priorTests.add(ihcBuilder(protein).scoreValue(3D).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with exact result but with prefix
        priorTests.add(ihcBuilder(protein).scoreValuePrefix(">").scoreValue(2D).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with 'positive' result
        priorTests.add(ihcBuilder(protein).scoreText("Positive").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with exact result
        priorTests.add(ihcBuilder(protein).scoreValue(2D).build());
        assertEvaluation(EvaluationResult.PASS, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));
    }

    @NotNull
    private static ImmutablePriorMolecularTest.Builder ihcBuilder(@NotNull String protein) {
        return MolecularTestFactory.priorBuilder().test("IHC").item(protein);
    }
}