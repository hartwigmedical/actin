package com.hartwig.actin.algo.evaluation.reproduction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.jetbrains.annotations.NotNull;

public class IsBreastfeeding implements EvaluationFunction {

    IsBreastfeeding() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.clinical().patient().gender() == Gender.MALE) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient is male thus won't be breastfeeding")
                    .addFailGeneralMessages("No breastfeeding")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.NOT_EVALUATED)
                    .addPassSpecificMessages("It is assumed that patient won't be breastfeeding")
                    .addPassGeneralMessages("Assumed no breastfeeding")
                    .build();
        }
    }
}
