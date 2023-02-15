package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasBoneMetastases implements EvaluationFunction {

    HasBoneMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return TumorUtil.evaluateBooleanMetastasis(record.clinical().tumor().hasBoneLesions(), "bone");
    }
}
