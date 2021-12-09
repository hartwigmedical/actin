package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasSufficientLabValueLLN implements LabEvaluationFunction {

    private final double minLLN;

    HasSufficientLabValueLLN(final double minLLN) {
        this.minLLN = minLLN;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final LabValue labValue) {
        return LaboratoryUtil.evaluateVersusMinULN(labValue, minLLN);
    }
}
