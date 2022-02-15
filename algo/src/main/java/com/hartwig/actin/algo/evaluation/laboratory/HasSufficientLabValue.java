package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasSufficientLabValue implements LabEvaluationFunction {

    private final double minValue;

    HasSufficientLabValue(final double minValue) {
        this.minValue = minValue;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        return LaboratoryUtil.evaluateVersusMinValue(labValue.value(), labValue.comparator(), minValue);
    }
}
