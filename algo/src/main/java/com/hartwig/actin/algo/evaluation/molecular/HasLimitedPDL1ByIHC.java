package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

public class HasLimitedPDL1ByIHC implements EvaluationFunction {

    private final int maxPDL1;

    HasLimitedPDL1ByIHC(final int maxPDL1) {
        this.maxPDL1 = maxPDL1;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorMolecularTest ihcTest : PriorMolecularTestFunctions.allPDL1TestsByCPS(record.clinical().priorMolecularTests())) {
            boolean hasLimitedPDL1 = false;

            Double scoreValue = ihcTest.scoreValue();
            if (scoreValue != null && scoreValue <= maxPDL1) {
                hasLimitedPDL1 = true;
            }

            if (hasLimitedPDL1) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassMessages("PD-L1 expression measured by CPS does not exceed maximum level of " + maxPDL1)
                        .build();
            }
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("No PD-L1 IHC test found where level does not exceed maximum level of " + maxPDL1)
                .build();
    }
}
