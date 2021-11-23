package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSymptomaticCnsMetastases implements EvaluationFunction {

    HasSymptomaticCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasSymptomaticCnsMetastases = record.clinical().tumor().hasSymptomaticCnsLesions();
        if (hasSymptomaticCnsMetastases == null) {
            return Evaluation.UNDETERMINED;
        }

        return hasSymptomaticCnsMetastases ? Evaluation.PASS : Evaluation.FAIL;
    }
}
