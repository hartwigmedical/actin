package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.ValueComparison;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

public class ProteinIsExpressedByIHC implements EvaluationFunction {

    @NotNull
    private final String protein;

    ProteinIsExpressedByIHC(@NotNull final String protein) {
        this.protein = protein;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<PriorMolecularTest> ihcTests =
                PriorMolecularTestFunctions.allIHCTestsForProtein(record.clinical().priorMolecularTests(), protein);
        for (PriorMolecularTest ihcTest : ihcTests) {
            boolean isExpressed = false;

            String scoreText = ihcTest.scoreText();
            if (scoreText != null && scoreText.equalsIgnoreCase("positive")) {
                isExpressed = true;
            }

            Double scoreValue = ihcTest.scoreValue();
            if (scoreValue != null
                    && ValueComparison.evaluateVersusMinValue(scoreValue, ihcTest.scoreValuePrefix(), 0D) == EvaluationResult.PASS) {
                isExpressed = true;
            }

            if (isExpressed) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Protein " + protein + " has been determined to be expressed (by IHC)")
                        .addPassGeneralMessages(protein + " expression by IHC")
                        .build();
            }
        }

        if (!ihcTests.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("No expression of protein " + protein + " detected by prior IHC test(s)")
                    .addFailGeneralMessages("No " + protein + " expression by IHC")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No test result found; protein " + protein + " has not been tested by IHC")
                    .addUndeterminedGeneralMessages("No " + protein + " IHC test result")
                    .build();
        }
    }
}
