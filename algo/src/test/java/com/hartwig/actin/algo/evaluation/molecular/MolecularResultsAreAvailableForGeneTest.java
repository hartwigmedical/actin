package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.molecular.datamodel.ExperimentType;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularResultsAreAvailableForGeneTest {

    @Test
    public void canEvaluate() {
        MolecularResultsAreAvailableForGene function = new MolecularResultsAreAvailableForGene("gene 1");

        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withExperimentType(ExperimentType.WGS)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withExperimentType(ExperimentType.PANEL)));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndPriorTest(ExperimentType.PANEL, create("gene 1", false))));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndPriorTest(ExperimentType.PANEL, create("gene 1", true))));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndPriorTest(ExperimentType.PANEL, create("gene 2", false))));
    }

    @NotNull
    private static PriorMolecularTest create(@NotNull String gene, boolean impliesPotentialDeterminateStatus) {
        return MolecularTestFactory.priorBuilder()
                .item(gene)
                .impliesPotentialIndeterminateStatus(impliesPotentialDeterminateStatus)
                .build();
    }
}