package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSufficientLVEF implements EvaluationFunction {

    private final double minLVEF;
    private final boolean passIfUnknown;

    public HasSufficientLVEF(final double minLVEF, final boolean passIfUnknown) {
        this.minLVEF = minLVEF;
        this.passIfUnknown = passIfUnknown;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Double lvef = record.clinical().clinicalStatus().lvef();
        if (lvef == null) {
            return passIfUnknown ? Evaluation.PASS : Evaluation.UNDETERMINED;
        }

        return Double.compare(lvef, minLVEF) >= 0 ? Evaluation.PASS : Evaluation.FAIL;
    }
}