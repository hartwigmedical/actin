package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasMaximumWHOStatus implements EvaluationFunction {

    private final int maximumWHO;

    HasMaximumWHOStatus(final int maximumWHO) {
        this.maximumWHO = maximumWHO;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer who = record.clinical().clinicalStatus().who();
        if (who == null) {
            return Evaluation.UNDETERMINED;
        }

        return who <= maximumWHO ? Evaluation.PASS : Evaluation.FAIL;
    }
}
