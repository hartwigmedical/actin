package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasSufficientLabValueLLN implements LabEvaluationFunction {

    private final double minLLN;

    HasSufficientLabValueLLN(final double minLLN) {
        this.minLLN = minLLN;
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        return LaboratoryUtil.evaluateVersusMinULN(labValue, minLLN);
    }
}
