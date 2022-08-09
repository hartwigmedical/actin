package com.hartwig.actin.algo.evaluation.reproduction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.jetbrains.annotations.NotNull;

//TODO: Check according to README
public class IsWomanOfChildBearingPotential implements EvaluationFunction {

    IsWomanOfChildBearingPotential() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.clinical().patient().gender() != Gender.FEMALE) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient is not a woman")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("It is currently not determined if patient is of child bearing potential")
                    .addUndeterminedGeneralMessages("Woman of child-bearing potential")
                    .build();
        }
    }
}
