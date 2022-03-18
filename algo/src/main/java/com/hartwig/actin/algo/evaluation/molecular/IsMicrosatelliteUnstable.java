package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class IsMicrosatelliteUnstable implements EvaluationFunction {

    IsMicrosatelliteUnstable() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean isMicrosatelliteUnstable = record.molecular().isMicrosatelliteUnstable();

        if (isMicrosatelliteUnstable == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No microsatellite status is known")
                    .build();
        }

        EvaluationResult result = isMicrosatelliteUnstable ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor is microsatellite stable");
            builder.addFailGeneralMessages("Molecular requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor is microsatellite unstable");
            builder.addPassGeneralMessages("Molecular requirements");
        }

        return builder.build();
    }
}
