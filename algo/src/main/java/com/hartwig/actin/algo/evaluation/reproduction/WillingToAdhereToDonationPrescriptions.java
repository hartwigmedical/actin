package com.hartwig.actin.algo.evaluation.reproduction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class WillingToAdhereToDonationPrescriptions implements EvaluationFunction {

    WillingToAdhereToDonationPrescriptions() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.NOT_EVALUATED)
                .addPassSpecificMessages("Assumed that patient will adhere to relevant sperm/egg donation prescriptions")
                .addPassGeneralMessages("Assumed adherence to relevant sperm/egg donation prescriptions")
                .build();
    }
}
