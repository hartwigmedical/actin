package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.jetbrains.annotations.NotNull;

public class SecondMalignanciesHaveBeenCuredRecently implements EvaluationFunction {

    SecondMalignanciesHaveBeenCuredRecently() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            if (priorSecondPrimary.isActive()) {
                return EvaluationResult.FAIL;
            }
        }
        return EvaluationResult.PASS;
    }
}
