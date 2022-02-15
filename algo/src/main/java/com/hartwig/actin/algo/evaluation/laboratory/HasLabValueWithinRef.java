package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasLabValueWithinRef implements LabEvaluationFunction {

    HasLabValueWithinRef() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        Boolean isOutsideRef = labValue.isOutsideRef();
        if (isOutsideRef == null) {
            return EvaluationResult.UNDETERMINED;
        }

        return isOutsideRef ? EvaluationResult.FAIL : EvaluationResult.PASS;
    }
}
