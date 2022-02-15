package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasLimitedLabValueULN implements LabEvaluationFunction {

    private final double maxULN;

    HasLimitedLabValueULN(final double maxULN) {
        this.maxULN = maxULN;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        return LaboratoryUtil.evaluateVersusMaxULN(labValue, maxULN);
    }
}
