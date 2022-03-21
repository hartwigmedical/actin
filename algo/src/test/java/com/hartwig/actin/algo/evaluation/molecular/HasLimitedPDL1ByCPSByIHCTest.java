package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasLimitedPDL1ByCPSByIHCTest {

    @Test
    public void canEvaluate() {
        HasLimitedPDL1ByCPSByIHC function = new HasLimitedPDL1ByCPSByIHC(2);

        // No prior tests
        List<PriorMolecularTest> priorTests = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with no result
        priorTests.add(pdl1Builder().build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with value too high
        priorTests.add(pdl1Builder().scoreValue(3D).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add test with right value
        priorTests.add(pdl1Builder().scoreValue(1D).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));
    }

    @NotNull
    private static ImmutablePriorMolecularTest.Builder pdl1Builder() {
        return ImmutablePriorMolecularTest.builder().test("IHC").item("PD-L1").measure("CPS");
    }
}