package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

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
        List<PriorMolecularTest> ihcTests = PriorMolecularTestFunctions.allIHCTestsForGene(record.clinical().priorMolecularTests(), gene);
        for (PriorMolecularTest ihcTest : ihcTests) {
            boolean isExpressed = false;

            String scoreText = ihcTest.scoreText();
            if (scoreText != null && scoreText.equalsIgnoreCase("positive")) {
                isExpressed = true;
            }

            Double scoreValue = ihcTest.scoreValue();
            if (scoreValue != null && scoreValue > 0) {
                isExpressed = true;
            }

            if (isExpressed) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassMessages("Gene " + gene + " has been determined to be expressed (by IHC)")
                        .build();
            }
        }

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(EvaluationResult.FAIL);

        if (!ihcTests.isEmpty()) {
            builder.addFailMessages("No expression of gene " + gene + " detected by prior IHC test(s)");
        } else {
            builder.addFailMessages("No test result found; gene " + gene + " has not been tested by IHC");
        }

        return builder.build();
    }
}