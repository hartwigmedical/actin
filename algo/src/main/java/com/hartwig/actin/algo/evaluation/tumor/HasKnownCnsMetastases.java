package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownCnsMetastases implements EvaluationFunction {

    //TODO: Implement according to README
    HasKnownCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownCnsMetastases = record.clinical().tumor().hasCnsLesions();

        if (hasKnownCnsMetastases == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Data regarding presence of CNS metastases is missing, assuming there are none")
                    .addFailGeneralMessages("Assuming no known CNS metastases")
                    .build();
        }

        EvaluationResult result = hasKnownCnsMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No known CNS metastases present");
            builder.addFailGeneralMessages("No known CNS metastases");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("CNS metastases are present");
            builder.addPassGeneralMessages("CNS metastases");
        }

        return builder.build();
    }
}
