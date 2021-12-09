package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasLabValueWithinRef implements LabEvaluationFunction {

    HasLabValueWithinRef() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull LabValue value) {
        Boolean isOutsideRef = value.isOutsideRef();
        if (isOutsideRef == null) {
            return Evaluation.UNDETERMINED;
        }

        return isOutsideRef ? Evaluation.FAIL : Evaluation.PASS;
    }
}
