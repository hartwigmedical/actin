package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Review below implementation and test
public class HasAssessableDisease implements EvaluationFunction {

    HasAssessableDisease() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasMeasurableDisease = record.clinical().tumor().hasMeasurableDisease();

        if (hasMeasurableDisease == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data regarding measurable/assessable disease is missing")
                    .addUndeterminedGeneralMessages("Undetermined assessable disease")
                    .build();
        }

        EvaluationResult result = hasMeasurableDisease ? EvaluationResult.PASS : EvaluationResult.UNDETERMINED;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("Patient has no measurable disease, but may be assessable?");
            builder.addUndeterminedGeneralMessages("Undetermined assessable disease");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has measurable disease, thus also assessable");
            builder.addPassGeneralMessages("Measurable (assessable) disease");
        }

        return builder.build();
    }
}


