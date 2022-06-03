package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLungMetastases implements EvaluationFunction {

    HasLungMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasLungMetastases = record.clinical().tumor().hasLungLesions();

        if (hasLungMetastases == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data regarding presence of lung metastases is missing")
                    .addUndeterminedGeneralMessages("Missing lung metastasis data")
                    .build();
        }

        EvaluationResult result = hasLungMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No lung metastases present");
            builder.addFailGeneralMessages("No lung metastases");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Lung metastases are present");
            builder.addPassGeneralMessages("Lung metastases");
        }

        return builder.build();
    }
}
