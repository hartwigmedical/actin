package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class IsTreatedInHospital implements EvaluationFunction {

    IsTreatedInHospital() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.NOT_EVALUATED)
                .addPassSpecificMessages("Currently exact hospital of treatment is not determined")
                .addPassGeneralMessages("Specific hospital not evaluated")
                .build();
    }
}