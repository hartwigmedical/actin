package com.hartwig.actin.algo.evaluation.molecular;

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
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        for (PriorMolecularTest priorMolecularTest : IHCFunctions.allIHCTestsForGene(record.clinical().priorMolecularTests(), gene)) {
            boolean hasSufficientExpression = false;

            Double scoreValue = priorMolecularTest.scoreValue();
            if (scoreValue != null) {
                // We assume IHC prior molecular tests always have integer score values.
                hasSufficientExpression = Math.round(scoreValue) >= minExpressionLevel;
            }

            if (hasSufficientExpression) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassMessages("Gene " + gene + " has expression level of at least " + minExpressionLevel + " (by IHC)")
                        .build();
            }
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("Gene " + gene + " does not have meet required expression level " + minExpressionLevel + " (by IHC)")
                .build();
    }
}
