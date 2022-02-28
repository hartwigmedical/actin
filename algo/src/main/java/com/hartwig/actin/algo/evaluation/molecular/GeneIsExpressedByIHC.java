package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

public class GeneIsExpressedByIHC implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsExpressedByIHC(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorMolecularTest priorMolecularTest : IHCFunctions.allIHCTestsForGene(record.clinical().priorMolecularTests(), gene)) {
            boolean isExpressed = false;
            String scoreText = priorMolecularTest.scoreText();
            if (scoreText != null && scoreText.equalsIgnoreCase("positive")) {
                isExpressed = true;
            }

            Double scoreValue = priorMolecularTest.scoreValue();
            if (scoreValue != null && scoreValue > 0) {
                isExpressed = true;
            }

            if (isExpressed) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassMessages("Gene " + gene + " has been determined to expressed (by IHC)")
                        .build();
            }
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("Gene " + gene + " has not been determined to expressed (by IHC)")
                .build();
    }
}