package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
        boolean hasPositiveOrNegativeResult = false;
        for (PriorMolecularTest ihcTest : ihcTests) {
            Double scoreValue = ihcTest.scoreValue();
            if (scoreValue != null) {
                // We assume IHC prior molecular tests always have integer score values.
                if (Double.compare(Math.round(scoreValue), minExpressionLevel) >= 0) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages(
                                    "Gene " + gene + " has expression level of at least " + minExpressionLevel + " (by IHC)")
                            .addPassGeneralMessages("Adequate " + gene + " IHC expression level")
                            .build();
                }
            }

            String scoreText = ihcTest.scoreText();
            if (scoreText != null && (scoreText.equalsIgnoreCase("positive") || scoreText.equalsIgnoreCase("negative"))) {
                hasPositiveOrNegativeResult = true;
            }
        }

        if (hasPositiveOrNegativeResult) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Unknown if gene " + gene + " expression level is at least " + minExpressionLevel + " (by IHC)")
                    .addUndeterminedGeneralMessages("Unknown " + gene + " exact IHC expression level")
                    .build();
        } else if (ihcTests.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No test result found; gene " + gene + " has not been tested by IHC")
                    .addUndeterminedGeneralMessages("No " + gene + " IHC test result")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Gene " + gene + " does not meet required expression level " + minExpressionLevel + " (by IHC)")
                .addFailGeneralMessages("Insufficient " + gene + "exact IHC expression level")
                .build();
    }
}