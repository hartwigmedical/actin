package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularResultsAreAvailableForPromoterOfGeneTest {

    @Test
    public void canEvaluate() {
        MolecularResultsAreAvailableForPromoterOfGene function = new MolecularResultsAreAvailableForPromoterOfGene("gene 1");

        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorTest(create("gene 1 promoter", false))));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withPriorTest(create("gene 1 promoter", true))));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTest(create("gene 1 coding", false))));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTest(create("gene 2 promoter", false))));
    }

    @NotNull
    private static PriorMolecularTest create(@NotNull String gene, boolean impliesPotentialDeterminateStatus) {
        return MolecularTestFactory.priorBuilder()
                .item(gene)
                .impliesPotentialIndeterminateStatus(impliesPotentialDeterminateStatus)
                .build();
    }
}