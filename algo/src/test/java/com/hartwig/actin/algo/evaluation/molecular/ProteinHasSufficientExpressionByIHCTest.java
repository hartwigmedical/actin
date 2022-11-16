package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.util.ValueComparison;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ProteinHasSufficientExpressionByIHCTest {

    @Test
    public void canEvaluate() {
        String gene = "gene 1";
        ProteinHasSufficientExpressionByIHC exact = new ProteinHasSufficientExpressionByIHC(gene, 2);

        // No prior tests
        List<PriorMolecularTest> priorTests = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with no result
        priorTests.add(ihcBuilder(gene).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with too low result
        priorTests.add(ihcBuilder(gene).scoreValue(1D).build());
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with too low result but a suitable comparator
        priorTests.add(ihcBuilder(gene).scoreValuePrefix(ValueComparison.LARGER_THAN).scoreValue(1D).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Add test with valid result
        priorTests.add(ihcBuilder(gene).scoreValue(3D).build());
        assertEvaluation(EvaluationResult.PASS, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)));

        // Test with unclear result
        assertEvaluation(EvaluationResult.UNDETERMINED,
                exact.evaluate(MolecularTestFactory.withPriorTests(Lists.newArrayList(ihcBuilder(gene).scoreText("Negative")
                        .build()))));
    }

    @NotNull
    private static ImmutablePriorMolecularTest.Builder ihcBuilder(@NotNull String gene) {
        return MolecularTestFactory.priorBuilder().test("IHC").item(gene);
    }
}