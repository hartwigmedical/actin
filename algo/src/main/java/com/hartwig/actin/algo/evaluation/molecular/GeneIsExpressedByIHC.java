package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Gene " + gene + " has been determined to be expressed (by IHC)")
                        .addPassGeneralMessages(gene + " expression by IHC")
                        .build();
            }
        }

        if (!ihcTests.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("No expression of gene " + gene + " detected by prior IHC test(s)")
                    .addFailGeneralMessages("No " + gene + " expression by IHC")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No test result found; gene " + gene + " has not been tested by IHC")
                    .addUndeterminedGeneralMessages("No " + gene + " IHC test result")
                    .build();
        }
    }
}
