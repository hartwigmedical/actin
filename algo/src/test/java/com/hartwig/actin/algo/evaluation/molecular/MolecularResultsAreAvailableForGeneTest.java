package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.molecular.datamodel.ExperimentType;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularResultsAreAvailableForGeneTest {

    @Test
    public void canEvaluate() {
        MolecularResultsAreAvailableForGene function = new MolecularResultsAreAvailableForGene("gene 1");

        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withExperimentType(ExperimentType.WGS)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withExperimentType(ExperimentType.PANEL)));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndPriorTest(ExperimentType.PANEL, createForGene("gene 1"))));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withExperimentTypeAndPriorTest(ExperimentType.PANEL, createForGene("gene 2"))));
    }

    @NotNull
    private static PriorMolecularTest createForGene(@NotNull String gene) {
        return ImmutablePriorMolecularTest.builder().test(Strings.EMPTY).item(gene).build();
    }
}