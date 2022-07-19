package com.hartwig.actin.algo.evaluation.surgery;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Implement according to README
public class HasHadSurgeryInPastMonths implements EvaluationFunction {

    HasHadSurgeryInPastMonths() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("It is currently not determined if patient has received surgery in past nr of months")
                .addUndeterminedGeneralMessages("Undetermined recent surgery")
                .build();
    }
}
