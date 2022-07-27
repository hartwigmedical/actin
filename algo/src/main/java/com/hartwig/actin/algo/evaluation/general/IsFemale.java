package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.jetbrains.annotations.NotNull;

public class IsFemale implements EvaluationFunction {

    IsFemale() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        EvaluationResult result = record.clinical().patient().gender() == Gender.FEMALE ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient is not female");
            builder.addFailGeneralMessages("Inadequate gender");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient is female");
            builder.addPassGeneralMessages("Adequate gender");
        }

        return builder.build();
    }
}
