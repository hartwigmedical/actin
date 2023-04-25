package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHadAnyCancerTreatment implements EvaluationFunction {

    HasHadAnyCancerTreatment() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.clinical().priorTumorTreatments().isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has not had any prior cancer treatments")
                    .addFailGeneralMessages("Has not had any cancer treatment")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("Patient has had prior cancer treatment")
                .addPassGeneralMessages("Had had any cancer treatment")
                .build();
    }
}
