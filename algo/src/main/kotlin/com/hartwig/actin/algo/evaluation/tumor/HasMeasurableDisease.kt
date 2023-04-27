package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasMeasurableDisease implements EvaluationFunction {

    HasMeasurableDisease() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasMeasurableDisease = record.clinical().tumor().hasMeasurableDisease();

        if (hasMeasurableDisease == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data regarding measurable disease is missing, unknown if measurable")
                    .addUndeterminedGeneralMessages("Undetermined measurable disease")
                    .build();
        }

        EvaluationResult result = hasMeasurableDisease ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no measurable disease");
            builder.addFailGeneralMessages("No measurable disease");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has measurable disease");
            builder.addPassGeneralMessages("Measurable disease");
        }

        return builder.build();
    }
}
