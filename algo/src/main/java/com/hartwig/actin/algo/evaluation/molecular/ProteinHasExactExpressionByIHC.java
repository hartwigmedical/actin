package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

public class ProteinHasExactExpressionByIHC implements EvaluationFunction {

    @NotNull
    private final String gene;
    private final int expressionLevel;

    public ProteinHasExactExpressionByIHC(@NotNull final String gene, final int expressionLevel) {
        this.gene = gene;
        this.expressionLevel = expressionLevel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<PriorMolecularTest> ihcTests = PriorMolecularTestFunctions.allIHCTestsForGene(record.clinical().priorMolecularTests(), gene);
        boolean hasPositiveOrNegativeResult = false;
        for (PriorMolecularTest ihcTest : ihcTests) {
            Double scoreValue = ihcTest.scoreValue();
            if (scoreValue != null) {
                String scoreValuePrefix = ihcTest.scoreValuePrefix();
                // We assume IHC prior molecular tests always have integer score values.
                if (expressionLevel == Math.round(scoreValue) && (scoreValuePrefix == null || scoreValuePrefix.isEmpty())) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Gene " + gene + " has exact expression level " + expressionLevel + " (by IHC)")
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
                            "Unknown if gene " + gene + " expression level is exactly " + expressionLevel + " (by IHC)")
                    .addUndeterminedGeneralMessages("Unknown " + gene + " IHC test result")
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
                .addFailSpecificMessages("Gene " + gene + " does not have exact expression level " + expressionLevel + " (by IHC)")
                .addFailGeneralMessages("No " + gene + " expression by IHC")
                .build();
    }
}