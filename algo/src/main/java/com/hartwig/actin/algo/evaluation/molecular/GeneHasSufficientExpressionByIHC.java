package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

public class GeneHasSufficientExpressionByIHC implements EvaluationFunction {

    @NotNull
    private final String gene;
    private final int minExpressionLevel;

    GeneHasSufficientExpressionByIHC(@NotNull final String gene, final int minExpressionLevel) {
        this.gene = gene;
        this.minExpressionLevel = minExpressionLevel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<PriorMolecularTest> ihcTests = PriorMolecularTestFunctions.allIHCTestsForGene(record.clinical().priorMolecularTests(), gene);
        for (PriorMolecularTest ihcTest : ihcTests) {
            boolean hasSufficientExpression = false;

            Double scoreValue = ihcTest.scoreValue();
            if (scoreValue != null) {
                // We assume IHC prior molecular tests always have integer score values.
                hasSufficientExpression = Double.compare(Math.round(scoreValue), minExpressionLevel) >= 0;
            }

            if (hasSufficientExpression) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Gene " + gene + " has expression level of at least " + minExpressionLevel + " (by IHC)")
                        .build();
            }
        }

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(EvaluationResult.FAIL);

        if (!ihcTests.isEmpty()) {
            builder.addFailSpecificMessages("Gene " + gene + " does not meet required expression level " + minExpressionLevel + " (by IHC)");
        } else {
            builder.addFailSpecificMessages("No test result found; gene " + gene + " has not been tested by IHC");
        }

        return builder.build();
    }
}
