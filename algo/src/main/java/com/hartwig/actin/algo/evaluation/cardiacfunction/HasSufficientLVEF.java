package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
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
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Double lvef = record.clinical().clinicalStatus().lvef();
        if (lvef == null) {
            if (passIfUnknown) {
                return ImmutableEvaluation.builder().result(EvaluationResult.PASS).addPassSpecificMessages("No LVEF known").build();
            } else {
                return ImmutableEvaluation.builder().result(EvaluationResult.UNDETERMINED).addUndeterminedSpecificMessages("No LVEF known").build();
            }
        }

        EvaluationResult result = Double.compare(lvef, minLVEF) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("LVEF of " + lvef + " is below minimum LVEF of " + minLVEF);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("LVEF of " + lvef + " exceeds minimum LVEF required ");
        }

        return builder.build();
    }
}
