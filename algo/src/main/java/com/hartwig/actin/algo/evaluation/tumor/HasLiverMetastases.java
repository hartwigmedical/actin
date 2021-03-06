package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLiverMetastases implements EvaluationFunction {

    HasLiverMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasLiverMetastases = record.clinical().tumor().hasLiverLesions();

        if (hasLiverMetastases == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data regarding presence of liver metastases is missing")
                    .addUndeterminedGeneralMessages("Missing liver metastasis data")
                    .build();
        }

        EvaluationResult result = hasLiverMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No liver metastases present");
            builder.addFailGeneralMessages("No liver metastases");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Liver metastases are present");
            builder.addPassGeneralMessages("Liver metastases");
        }

        return builder.build();
    }
}
