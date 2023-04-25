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

public class HasSufficientPDL1ByIHC implements EvaluationFunction {

    @NotNull
    private final String measure;
    private final double minPDL1;

    HasSufficientPDL1ByIHC(@NotNull final String measure, final double minPDL1) {
        this.measure = measure;
        this.minPDL1 = minPDL1;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<PriorMolecularTest> pdl1Tests = PriorMolecularTestFunctions.allPDL1Tests(record.clinical().priorMolecularTests(), measure);
        for (PriorMolecularTest ihcTest : pdl1Tests) {
            Double scoreValue = ihcTest.scoreValue();
            if (scoreValue != null) {
                EvaluationResult evaluation =
                        ValueComparison.evaluateVersusMinValue(Math.round(scoreValue), ihcTest.scoreValuePrefix(), minPDL1);

                if (evaluation == EvaluationResult.PASS) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages(
                                    "PD-L1 expression measured by " + measure + " meets at least desired level of " + minPDL1)
                            .addPassGeneralMessages("PD-L1 expression exceeds " + minPDL1)
                            .build();
                }
            }
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(EvaluationResult.FAIL);

        if (!pdl1Tests.isEmpty()) {
            builder.addFailSpecificMessages("No PD-L1 IHC test found where level exceeds desired level of " + minPDL1);
            builder.addFailGeneralMessages("PD-L1 expression does not exceed " + minPDL1);
        } else {
            builder.addFailSpecificMessages("No test result found; PD-L1 has not been tested by IHC");
            builder.addFailGeneralMessages("PD-L1 expression not tested by IHC");
        }

        return builder.build();
    }
}
