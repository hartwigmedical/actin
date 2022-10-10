package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularResultsAreAvailableNotIndeterminateForPromoterOfGeneTest {

    @Test
    public void canEvaluate() {
        MolecularResultsAreAvailableNotIndeterminateForPromoterOfGene function =
                new MolecularResultsAreAvailableNotIndeterminateForPromoterOfGene("gene 1");

        List<PriorMolecularTest> priorTests = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add indeterminate test for gene 1 -> keep failing.
        priorTests.add(createForGeneAndIndeterminateStatus("gene 1", true));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add determinate test for gene 2 -> keep failing.
        priorTests.add(createForGeneAndIndeterminateStatus("gene 2", false));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));

        // Add determinate test for gene 1 -> Undetermined (promoter uncertain)
        priorTests.add(createForGeneAndIndeterminateStatus("gene 1", false));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withPriorMolecularTests(priorTests)));
    }

    @NotNull
    private static PriorMolecularTest createForGeneAndIndeterminateStatus(@NotNull String gene,
            boolean impliesPotentialIndeterminateStatus) {
        return MolecularTestFactory.priorBuilder()
                .test(Strings.EMPTY)
                .item(gene)
                .impliesPotentialIndeterminateStatus(impliesPotentialIndeterminateStatus)
                .build();
    }
}