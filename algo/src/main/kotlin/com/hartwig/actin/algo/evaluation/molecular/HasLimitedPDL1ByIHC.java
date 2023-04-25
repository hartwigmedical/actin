package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.ValueComparison;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

public class HasLimitedPDL1ByIHC implements EvaluationFunction {

    @NotNull
    private final String measure;
    private final double maxPDL1;

    public HasLimitedPDL1ByIHC(@NotNull final String measure, final double maxPDL1) {
        this.measure = measure;
        this.maxPDL1 = maxPDL1;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<PriorMolecularTest> pdl1Tests = PriorMolecularTestFunctions.allPDL1Tests(record.clinical().priorMolecularTests(), measure);
        for (PriorMolecularTest ihcTest : pdl1Tests) {
            Double scoreValue = ihcTest.scoreValue();
            if (scoreValue != null) {
                EvaluationResult evaluation =
                        ValueComparison.evaluateVersusMaxValue(Math.round(scoreValue), ihcTest.scoreValuePrefix(), maxPDL1);

                if (evaluation == EvaluationResult.PASS) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("PD-L1 expression measured by " + measure + " does not exceed maximum of " + maxPDL1)
                            .addPassGeneralMessages("PD-L1 expression below " + maxPDL1)
                            .build();
                }
            }
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(EvaluationResult.FAIL);

        if (!pdl1Tests.isEmpty()) {
            builder.addFailSpecificMessages(
                    "At least one PD-L1 IHC tests measured by " + measure + " found where level exceeds maximum of " + maxPDL1);
            builder.addFailGeneralMessages("PD-L1 expression exceeds " + maxPDL1);
        } else {
            builder.addFailSpecificMessages("No IHC test result found; PD-L1 has not been measured by " + measure);
            builder.addFailGeneralMessages("PD-L1 expression not tested by IHC");
        }

        return builder.build();
    }
}
