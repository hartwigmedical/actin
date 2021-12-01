package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownSymptomaticBrainMetastases implements EvaluationFunction {

    HasKnownSymptomaticBrainMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasSymptomaticBrainMetastases = record.clinical().tumor().hasSymptomaticBrainLesions();
        if (hasSymptomaticBrainMetastases == null) {
            return Evaluation.FAIL;
        }

        return hasSymptomaticBrainMetastases ? Evaluation.PASS : Evaluation.FAIL;
    }
}
