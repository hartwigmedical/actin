package com.hartwig.actin.algo.evaluation.infection;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class CanAdhereToAttenuatedVaccineUse implements EvaluationFunction {

    CanAdhereToAttenuatedVaccineUse() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder()
                .result(EvaluationResult.NOT_EVALUATED)
                .addPassSpecificMessages("Adherence to protocol for attenuated vaccine use is currently not evaluated")
                .build();
    }
}
