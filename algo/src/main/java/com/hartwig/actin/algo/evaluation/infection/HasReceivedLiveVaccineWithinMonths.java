package com.hartwig.actin.algo.evaluation.infection;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import org.jetbrains.annotations.NotNull;

public class HasReceivedLiveVaccineWithinMonths implements EvaluationFunction {

    HasReceivedLiveVaccineWithinMonths() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Unknown if patient has received a live vaccine within nr of months")
                .addUndeterminedGeneralMessages("Unknown live vaccine status")
                .build();
    }

}
