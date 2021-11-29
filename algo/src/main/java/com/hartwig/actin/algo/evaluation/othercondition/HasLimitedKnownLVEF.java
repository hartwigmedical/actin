package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLimitedKnownLVEF implements EvaluationFunction {

    private final double maxLVEF;

    HasLimitedKnownLVEF(final double maxLVEF) {
        this.maxLVEF = maxLVEF;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Double lvef = record.clinical().clinicalStatus().lvef();
        if (lvef == null) {
            return Evaluation.FAIL;
        }

        return Double.compare(lvef, maxLVEF) > 0 ? Evaluation.FAIL : Evaluation.PASS;
    }
}
