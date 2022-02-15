package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasBoneMetastases implements EvaluationFunction {

    HasBoneMetastases() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        Boolean hasBoneMetastases = record.clinical().tumor().hasBoneLesions();
        if (hasBoneMetastases == null) {
            return EvaluationResult.UNDETERMINED;
        }

        return hasBoneMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }
}
