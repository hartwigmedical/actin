package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasCnsMetastases implements EvaluationFunction {

    HasCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasCnsMetastases = record.clinical().tumor().hasCnsLesions();
        if (hasCnsMetastases == null) {
            return Evaluation.UNDETERMINED;
        }

        return hasCnsMetastases ? Evaluation.PASS : Evaluation.FAIL;
    }
}
