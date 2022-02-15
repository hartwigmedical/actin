package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSufficientLVEF implements EvaluationFunction {

    private final double minLVEF;
    private final boolean passIfUnknown;

    HasSufficientLVEF(final double minLVEF, final boolean passIfUnknown) {
        this.minLVEF = minLVEF;
        this.passIfUnknown = passIfUnknown;
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        Double lvef = record.clinical().clinicalStatus().lvef();
        if (lvef == null) {
            return passIfUnknown ? EvaluationResult.PASS : EvaluationResult.UNDETERMINED;
        }

        return Double.compare(lvef, minLVEF) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }
}
