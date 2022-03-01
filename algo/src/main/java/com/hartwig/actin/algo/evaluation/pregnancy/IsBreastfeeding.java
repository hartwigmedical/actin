package com.hartwig.actin.algo.evaluation.pregnancy;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.jetbrains.annotations.NotNull;

public class IsBreastfeeding implements EvaluationFunction {

    IsBreastfeeding() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.clinical().patient().gender() == Gender.MALE)
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.FAIL)
                    .addPassMessages("Patient is male thus won't be breastfeeding")
                    .build();
        else
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.NOT_EVALUATED)
                    .addPassMessages("It is assumed that patient won't be breastfeeding")
                    .build();
    }
}
