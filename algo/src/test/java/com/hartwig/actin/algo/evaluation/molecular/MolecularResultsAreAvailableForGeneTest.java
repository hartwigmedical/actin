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

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.WGS, true)));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.WGS, false)));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.PANEL, true)));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndPriorTest(ExperimentType.PANEL, createPrior("gene 1", false))));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndPriorTest(ExperimentType.PANEL, createPrior("gene 1", true))));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndPriorTest(ExperimentType.PANEL, createPrior("gene 2", false))));
    }

    @NotNull
    private static PriorMolecularTest createPrior(@NotNull String gene, boolean impliesPotentialDeterminateStatus) {
        return MolecularTestFactory.priorBuilder()
                .item(gene)
                .impliesPotentialIndeterminateStatus(impliesPotentialDeterminateStatus)
                .build();
    }
}